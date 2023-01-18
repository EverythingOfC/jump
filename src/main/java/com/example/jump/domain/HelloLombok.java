package com.example.jump.domain;


// lombok: getter,setter,생성자 등을 자동으로 만들어주는 도구
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor    // final 필드를 초기화하는 생성자를 만들어줌.
@Getter
public class HelloLombok {

    private final String hello;
    private final int lombox;

    public static void main(String[] args) {
        HelloLombok helloLombok = new HelloLombok("헬로",5);  // lombok에 의해 만들어진 생성자
        System.out.println(helloLombok.getHello());
        System.out.println(helloLombok.getLombox());

    }
}
