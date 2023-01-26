package com.example.jump;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // 해당 클래스가 스프링부트 테스트 클래스임을 알려줌.
class JumpApplicationTests {

    @Autowired  // 해당 클래스에 MetaRepository객체 주입
    private MetaRepository metaRepository;

    @Test
    void testJpa() {

        MetaApi meta = new MetaApi();   // 새로운 객체를 생성한 것이므로, save()는 insert로 적용된다.
        meta.setMetaClassifications("운동");
        meta.setMetaType("휘트니스");
        meta.setMetaFormat("제이슨");
        meta.setMetaDate(LocalDate.now().toString());
        meta.setMetaContributor("서성준");
        meta.setMetaCoverage("100");
        meta.setMetaRight("메타빌드");
        meta.setMetaRelation("자바");
        meta.setMetaDescription("처음입니다.");
        meta.setMetaTitle("헬스클럽");
        meta.setMetaSubjects("운동기구");
        meta.setMetaIdentifier("식별자");
        meta.setMetaPublisher("문화체육관광부");
        this.metaRepository.save(meta);

    }
}