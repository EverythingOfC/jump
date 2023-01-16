package com.example.jump.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller // MainController클래스가 컨트롤러 기능을 담당한다는 것을 컴파일러에게 알려줌.
public class MainController {

    @GetMapping("jump")
    @ResponseBody
    public String index(){
        return "index";
    }
}
