package com.kiyotagbangers.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author KIYOTA, Takeshi
 */
@RestController
@RequestMapping("/")
public class HelloWorldController {

    @GetMapping
    public String hello(){
        return "Hello World";
    }
}
