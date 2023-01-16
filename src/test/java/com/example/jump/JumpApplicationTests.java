package com.example.jump;

import com.example.jump.domain.Meta;
import com.example.jump.repository.MetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JumpApplicationTests {

    @Autowired  // 해당 클래스에 MetaRepository객체 주입
    private MetaRepository metaRepository;

    @Test
    void testJpa() {
        Meta meta = new Meta();
    }

}
