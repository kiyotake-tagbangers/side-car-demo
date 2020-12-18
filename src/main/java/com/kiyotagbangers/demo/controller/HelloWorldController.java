package com.kiyotagbangers.demo.controller;

import com.kiyotagbangers.demo.MyData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author KIYOTA, Takeshi
 */
@RestController
@RequestMapping("/")
public class HelloWorldController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private MyData myData;

    public HelloWorldController(MyData myData) {
        this.myData = myData;
    }

    @GetMapping
    public MyData index() {
        logger.info("index");
        return this.myData;
    }

    @GetMapping("/hello")
    public String hello(){
        logger.info("hello");
        return "Hello";
    }
}
