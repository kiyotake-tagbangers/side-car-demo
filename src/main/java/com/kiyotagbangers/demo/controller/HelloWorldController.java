package com.kiyotagbangers.demo.controller;

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

    @GetMapping
    public String hello() {
        logger.info("side-car-spring");
        return "Hello World";
    }
}
