package com.example.jump.service;

import com.example.jump.domain.MetaApi;

import java.util.List;

public interface MetaService {  // 서비스 인터페이스

    List<MetaApi> getList();    // 모든 api 리스트 반환
}
