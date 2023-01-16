package com.example.jump.repository;


import com.example.jump.domain.Meta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;   // entity 테이블에 접근하는 메소드를 사용하기 위한 인터페이스
                                                    // CRUD를 어떻게 처리할 지 결정함.


public interface MetaRepository extends JpaRepository<Meta, Long> { // 엔티티와 엔티티의 pk를 적어줌.


}
