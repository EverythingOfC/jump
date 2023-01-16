package com.example.jump.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller    // HelloController 클래스가 컨트롤러 기능을 담당한다는 것을 컴파일러에게 알려줌.
public class HelloController {

    @GetMapping("/hello")   // 지정된 문자열로 get 방식의 url 요청이 발생하면 hello()를 실행한다.
    @ResponseBody   // hello()의 리턴값이 문자열 그 자체임을 나타냄.
    public String hello(){
        return "hello world";
    }
}
