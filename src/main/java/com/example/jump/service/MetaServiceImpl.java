package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service    // 해당 클래스를 스프링의 서비스로 인식
public class MetaServiceImpl implements MetaService{

    private final MetaRepository metaRepository;    // 레퍼지토리 객체를 생성자 주입

    public List<MetaApi> getList(){
        return this.metaRepository.findAll();
    }

}
