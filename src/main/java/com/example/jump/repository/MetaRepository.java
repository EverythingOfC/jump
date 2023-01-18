package com.example.jump.repository;


import com.example.jump.domain.MetaApi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MetaRepository extends JpaRepository<MetaApi, Long> { // 엔티티와 엔티티의 pk를 적어줌.

    MetaApi findByMetaSubjects(String meta_subjects);   // 주제어로 데이터 조회
    MetaApi findByMetaSubjectsAndMetaTitle(String a,String b);  // 주제어와 제목으로 데이터 조회
    List<MetaApi> findByMetaSubjectsLike(String a);   // like로 데이터 조회
    MetaApi findByMetaSubjectsLikeOrderByMetaId(String a);  // 검색 결과를 MetaId순으로 정렬

}
