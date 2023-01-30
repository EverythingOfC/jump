package com.example.jump.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import javax.persistence.*;


@NoArgsConstructor
@DynamicInsert  // 값이 null인 필드를 제외하고 insert함 (default값이 적용됨)
@Entity
@Getter
public class SearchApi {    // 검색 데이터 테이블
    @Id
    private long id;
    @Column(columnDefinition = "TEXT")
    private String title;
    public SearchApi(long id, String title){
        this.id = id;
        this.title = title;
    }
}
