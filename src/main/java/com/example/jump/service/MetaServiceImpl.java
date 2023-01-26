package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import com.example.jump.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service    // 해당 클래스를 스프링의 서비스로 인식
public class MetaServiceImpl implements MetaService{

    private final MetaRepository metaRepository;    // 레퍼지토리 객체를 생성자 주입

    public List<MetaApi> getList(){ // 전체 조회
        return this.metaRepository.findAll();
    }
    public MetaApi getView(Long id){    // 상세
        Optional<MetaApi> ID = this.metaRepository.findById(id);
        return ID.isPresent()?ID.get():null;       // 값이 있으면 불러옴
    }
    public void delete(Long id){    // 삭제
        Optional<MetaApi> ID = this.metaRepository.findById(id);
        if(ID.isPresent())  // 값이 있다면
            this.metaRepository.delete(ID.get());   //  해당 객체 삭제
    }

    public void save(MetaApi meta){     // 해당 객체 수정
        this.metaRepository.save(meta);
    }
}
