# Run Locally

## Spring boot app

```shell
$ export JAVA_HOME=`/usr/libexec/java_home -v 11`

$ java -version
openjdk version "11.0.2" 2019-01-15
OpenJDK Runtime Environment 18.9 (build 11.0.2+9)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)

$ ./mvnw clean package
$ ./mvnw spring-boot:run

$ curl localhost:8081/
index

$ curl localhost:8081/hello
hello
```

## Build Image

- spring

```shell
$ ./mvnw spring-boot:build-image

$ docker image ls | grep spring
demo-spring-app                                                                    0.0.1-SNAPSHOT          f28972791845        40 years ago        257MB

$ docker run -p 8081:8081 demo-spring-app:0.0.1-SNAPSHOT
```

- cloud native build pack

```shell
$ ./mvnw clean package

$ pack build side-car-spring:$(./mvnw org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout) -p target/demo-spring-app-0.0.1-SNAPSHOT.jar --builder cloudfoundry/cnb:bionic
```

## Running side-car container pattern

```shell
$ docker-compose up -d

$ docker-compose ps
     Name                    Command               State           Ports
---------------------------------------------------------------------------------
reverse-proxy     /docker-entrypoint.sh ngin ...   Up      0.0.0.0:80->80/tcp
side-car-spring   /cnb/lifecycle/launcher          Up      0.0.0.0:8081->8081/tcp

$ curl http://localhost
nginx index page

$ curl http://localhost/spring
Index

$ curl http://localhost/spring/hello
Hello
```

---
# Run on ECS

## Requirement

- ALB

- Target Group
  - port:80
  - health-check-path: /

- NATGateway

## Deploy ECR

```shell
# side-car-spring は ecr に事前に作った repository
$ docker tag side-car-spring:0.0.1-SNAPSHOT AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-spring:0.0.1-SNAPSHOT

$ docker push AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-spring:0.0.1-SNAPSHOT

$ cd fargate-reverse-proxy
$ docker build -t .
$ docker tag side-car-nginx:0.0.1-SNAPSHOT AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-nginx:0.0.1-SNAPSHOT

# side-car-nginx は ecr に事前に作った repository
$ docker push AWS_ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-nginx:0.0.1-SNAPSHOT
```

## Deploy ECS

### [Using ecs-cli](https://docs.aws.amazon.com/ja_jp/AmazonECS/latest/developerguide/cmd-ecs-cli.html)

```shell
# Amazon ECS CLI で使用する ECS クラスター名を設定
$ ecs-cli configure --region ap-northeast-1 --cluster side-car

# 設定の確認
$ \cat ~/.ecs/config
version: v1
default: default
clusters:
  default:
    cluster: side-car
    region: ap-northeast-1
    default_launch_type: ""


$ ecs-cli compose -f ./side-car.yml service up \
--launch-type FARGATE \
--target-groups \
"targetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-1:AWS_ACCOUNT_ID:targetgroup/side-car/abcdefghijklmnop,containerName=reverse-proxy,containerPort=80"

$ ecs-cli compose --project-name demo-spring-app service ps

arn:aws:elasticloadbalancing:ap-northeast-1:AWS_ACCOUNT_ID:targetgroup/side-car/abcdefghijklmnop

$ ecs-cli compose --project-name demo-spring-app service down
```

### Using awscli

```shell
# クラスタの確認
$ aws ecs list-clusters | jq '.clusterArns[]' -r
arn:aws:ecs:ap-northeast-1:AWS_ACCOUNT_ID:cluster/demo

# タスク定義の登録
$ aws ecs register-task-definition --cli-input-json file://./task-def.json

$ aws ecs list-task-definitions
{
    "taskDefinitionArns": [
        "arn:aws:ecs:ap-northeast-1:AWS_ACCOUNT_ID:task-definition/side-car:9"
    ]
}

# サービスの作成
$ aws ecs create-service \
    --cluster demo \
    --service-name side-car \
    --task-definition side-car:9 \
    --desired-count 1 \
    --launch-type FARGATE \
    --platform-version LATEST \
    --network-configuration "awsvpcConfiguration={subnets=[subnet-0dd108d70b7627789, subnet-0a3f62289c4ab3bbe],securityGroups=[sg-0f2ce7ffcf425fcf8],assignPublicIp=DISABLED}" \
    --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-1:AWS_ACCOUNT_ID:targetgroup/side-car/abcdefghijklmnop,containerName=reverse-proxy,containerPort=80"

# 削除
$ aws ecs delete-service --cluster demo --service side-car --force
```
