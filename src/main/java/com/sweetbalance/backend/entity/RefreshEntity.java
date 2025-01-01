package com.sweetbalance.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

// todo 스케줄러를 통한 만료 토큰 삭제 로직 추가요함
// 또는 Redis 적용 후 TTL(Time To Live) 기능 활용
@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
public class RefreshEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = false, length = 511)
    private String refresh;

    private String expiration;
}