package com.example.jump;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaApiRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;

@SpringBootTest // 해당 클래스가 스프링부트 테스트 클래스임을 알려줌.
class JumpApplicationTests {

    @Autowired  // 해당 클래스에 MetaRepository객체 주입
    private MetaApiRepository metaApiRepository;

    @Test
    void testJpa() {

        for(int i=0;i<20;i++){
        MetaApi meta = new MetaApi();   // 새로운 객체를 생성한 것이므로, save()는 insert로 적용된다.

        this.metaApiRepository.save(meta);
        }

    }
}