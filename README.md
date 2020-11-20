# Run Locally

```shell
$ export JAVA_HOME=`/usr/libexec/java_home -v 11`

$ java -version
openjdk version "11.0.2" 2019-01-15
OpenJDK Runtime Environment 18.9 (build 11.0.2+9)
OpenJDK 64-Bit Server VM 18.9 (build 11.0.2+9, mixed mode)

$ ./mvnw clean package
$ ./mvnw spring-boot:run
```

# Build Image

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

## Access

```shell
$ curl localhost:8081/
Hello World
```

## Deploy ECR

```shell
# side-car-spring は ecr に事前に作った repository
$ docker tag side-car-spring:0.0.1-SNAPSHOT AWS_ACCOUNTID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-spring:0.0.1-SNAPSHOT

$ docker push AWS_ACCOUNTID.dkr.ecr.ap-northeast-1.amazonaws.com/side-car-spring:0.0.1-SNAPSHOT
```