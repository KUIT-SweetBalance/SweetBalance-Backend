package com.sweetbalance.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "beverage_logs")
@Getter @Setter
public class BeverageLog extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private BeverageSize beverageSize;

    @Column(name = "syrup_name", length = 50)
    private String syrupName;

    @Column(name = "syrup_count")
    private int syrupCount;

}
