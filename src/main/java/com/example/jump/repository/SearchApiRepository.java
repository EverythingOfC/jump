package com.example.jump.repository;

import com.example.jump.domain.SearchApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchApiRepository extends JpaRepository<SearchApi,Long> {    // 검색 테이블에 접근하기 위한 레퍼지토리
    List<SearchApi> findByTitleContaining(String title);   // 제목으로 api 검색
}
