package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import com.sweetbalance.backend.enums.common.Status;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Enumerated(EnumType.STRING)
    private BeverageCategory category;

    @Column(nullable = false)
    private double sugar;

    @Column(nullable = false)
    private double calories;

    @Column(nullable = false)
    private double caffeine;

    @Column(nullable = false)
    private Integer consumeCount;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Builder.Default
    @OneToMany(mappedBy = "beverage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BeverageSize> sizes = new ArrayList<>();
}

