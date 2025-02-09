package com.sweetbalance.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beverage_sizes")
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @Setter
public class BeverageSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "size_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "beverage_id", nullable = false)
    private Beverage beverage;

    @Column(nullable = false)
    private String sizeType;

    @Column(nullable = false)
    private String sizeTypeDetail;

    @Column(nullable = false)
    private int volume;

    private double sugar;

    private double calories;

    private double caffeine;
}