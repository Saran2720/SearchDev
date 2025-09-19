package com.searchDev.SearchDev.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class greet {
    @RequestMapping("/")
    public String greet(){
        return "helo";
    }
}
