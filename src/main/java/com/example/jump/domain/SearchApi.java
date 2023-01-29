package com.example.jump.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.persistence.*;


@NoArgsConstructor
@DynamicInsert  // 값이 null인 필드를 제외하고 insert함 (default값이 적용됨)
@Getter
@Setter
@Entity
public class SearchApi {
    @Id
    private long id;
    @Column(columnDefinition = "TEXT")
    private String title;
    public SearchApi(long id, String title){
        this.id = id;
        this.title = title;
    }
}
