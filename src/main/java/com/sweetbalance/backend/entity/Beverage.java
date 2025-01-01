package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import com.sweetbalance.backend.enums.beverage.BeverageSize;
import com.sweetbalance.backend.enums.common.Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beverages")
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @Setter
public class Beverage extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long beverageId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String brand;

    @Enumerated(EnumType.STRING)
    private BeverageCategory category;

    @Enumerated(EnumType.STRING)
    private BeverageSize size;

    /*
     * 음료 영양정보의 경우 소수점을 포함 할 수 있기에 long 타입으로 자료형 변환 고려
     */
    
    @Column(nullable = false)
    private int sugar;

    @Column(nullable = false)
    private int calories;

    @Column(nullable = false)
    private int caffeine;

    @Column(nullable = false)
    private int volume;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;
}

