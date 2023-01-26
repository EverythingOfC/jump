package com.example.jump.service;

import com.example.jump.domain.MetaApi;
import org.springframework.ui.Model;

import java.util.List;

public interface MetaService {  // 서비스 인터페이스

    List<MetaApi> getList();    // 모든 api 리스트 반환
    MetaApi getView(Long id);   // api 상세
    void delete(Long id);       // api 삭제
    void save(MetaApi meta);    // api 저장

    void getApi(String serviceKey, String startDate, String endDate,String submit, Model model);     // 요청에 맞는 api 출력
}
