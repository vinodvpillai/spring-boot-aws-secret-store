package com.vinod.aws.secret.store.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Log4j2
public class UserController {

    @Value("${username}")
    private String username;
    @Value("${password}")
    private String password;

    @GetMapping
    public String getResult() {
        log.debug("Request came to fetch the secret store value");
        String result = "AWS secret store property Username: " + username +" Password: "+password;
        log.debug("Parameter store value for username is: {}, password: {}",username, password);
        return result;
    }
}
