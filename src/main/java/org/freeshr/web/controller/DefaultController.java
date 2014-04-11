package org.freeshr.web.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
@RequestMapping("/patient")
public class DefaultController {


    @RequestMapping("/message")
    public String simpleMessage() {
        return "Hello world";
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(DefaultController.class, args);
    }
}
