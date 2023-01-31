package com.example.jump.repository;


import com.example.jump.domain.MetaApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MetaApiRepository extends JpaRepository<MetaApi, Long> {   // 메타 테이블에 접근하기 위한 레퍼지토리

    Page<MetaApi> findAll(Pageable pageable);          // 페이징 처리
    List<MetaApi> findAllByMetaType(String type);      // 유형으로 데이터를 찾아옴.
}
