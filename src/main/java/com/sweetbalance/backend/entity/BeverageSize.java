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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    private Beverage beverage;

    @Column(nullable = false)
    private String sizeType;

    @Column(nullable = false)
    private int volume;

    public BeverageSize(Beverage beverage, String sizeType, int volume) {
        this.beverage = beverage;
        this.sizeType = sizeType;
        this.volume = volume;
    }
}
