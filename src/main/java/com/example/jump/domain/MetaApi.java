package com.example.jump.domain;

// ORM 사용 시 내부에서 SQL쿼리를 자동으로 생성해준다.
// JPA: ORM의 기술 표준으로 사용하는 인터페이스의 모임( 스프링부트에서 데이터베이스를 처리하기 위한 인터페이스)
// JPA의 대표적인 구현클래스: 하이버네이트
// Java Persistence API
// H2 데이터베이스: 개발용, 소규모 프로젝트에서 사용하는 경량 데이터베이스

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.LastModifiedDate;


import javax.persistence.*;


@NoArgsConstructor
@DynamicInsert  // 값이 null인 필드를 제외하고 insert함 (default값이 적용됨)
@Entity         // 데이터를 관리하는데 사용하는 ORM 클래스 ( 서비스 <--> 레퍼지토리 )
@Getter
public class MetaApi {  // 메타 데이터 테이블

    @Id
    private Long metaId;
    @Column(columnDefinition = "TEXT")  // 글자 수를 제한할 수 없는 경우에 사용함
    private String metaClassifications; // 분야
    @Column(columnDefinition = "TEXT", nullable = false) // null이면 안 됨.
    private String metaType;    // 유형
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaTitle;   // 제목
    @Column(columnDefinition = "TEXT")
    private String metaSubjects;    // 주제어
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaDescription; // 설명
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaPublisher;   // 발행기관(없다면 출처(연계시스템) 등록)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaContributor; // 원작자

    @LastModifiedDate   // 마지막에 수정된 값으로
    @Column(name = "metaDate")
    private String metaDate;  // 날짜
    @Column(columnDefinition = "TEXT")
    private String metaLanguage;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String metaIdentifier;  // 식별자
    @Column(columnDefinition = "TEXT")
    private String metaFormat;  // 형식
    @Column(columnDefinition = "TEXT")
    private String metaRelation;    // 관련자원
    @Column(columnDefinition = "TEXT")
    private String metaCoverage;    // 내용범위
    @Column(columnDefinition = "TEXT")
    private String metaRight;   // 이용조건

    // @Transient: 엔티티의 속성으로 사용하고 싶지 않을 때 사용


    public MetaApi(Long metaId, String metaClassifications, String metaType, String metaTitle, String metaSubjects, String metaDescription, String metaPublisher, String metaContributor, String metaDate, String metaLanguage, String metaIdentifier, String metaFormat, String metaRelation, String metaCoverage, String metaRight) {
        this.metaId = metaId;
        this.metaClassifications = metaClassifications;
        this.metaType = metaType;
        this.metaTitle = metaTitle;
        this.metaSubjects = metaSubjects;
        this.metaDescription = metaDescription;
        this.metaPublisher = metaPublisher;
        this.metaContributor = metaContributor;
        this.metaDate = metaDate;
        this.metaLanguage = metaLanguage;
        this.metaIdentifier = metaIdentifier;
        this.metaFormat = metaFormat;
        this.metaRelation = metaRelation;
        this.metaCoverage = metaCoverage;
        this.metaRight = metaRight;
    }

}
