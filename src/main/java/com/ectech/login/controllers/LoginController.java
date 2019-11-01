package com.ectech.login.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/oauth")
public class LoginController {

    @PostMapping(value = "/validate")
    public void validate() {
        System.out.print("VALIDATE");
    }
}
