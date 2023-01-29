package com.example.jump.repository;


import com.example.jump.domain.MetaApi;
import com.example.jump.domain.SearchApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface MetaRepository extends JpaRepository<MetaApi, Long> { // 엔티티와 엔티티의 pk를 적어줌.

    Page<MetaApi> findAll(Pageable pageable);   // 페이징 처리
}
