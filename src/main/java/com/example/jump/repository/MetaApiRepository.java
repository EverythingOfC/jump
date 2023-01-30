package com.example.jump.repository;


import com.example.jump.domain.MetaApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MetaApiRepository extends JpaRepository<MetaApi, Long> {   // 메타 테이블에 접근하기 위한 레퍼지토리

    Page<MetaApi> findAll(Pageable pageable);   // 페이징 처리
}
