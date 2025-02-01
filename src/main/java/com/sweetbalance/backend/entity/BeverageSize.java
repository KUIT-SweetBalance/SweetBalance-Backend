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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beverage_id", nullable = false)
    private Beverage beverage;

    @Column(nullable = false)
    private String sizeType;

    @Column(nullable = false)
    private int volume;

    private double sugar;

    private double calories;

    private double caffeine;

    public static BeverageSize fromBeverageAndVolume(Beverage beverage,
                                                     String sizeType,
                                                     int volume) {
        double sugar = beverage.getSugar() * volume / 100;
        double calories = beverage.getCalories() * volume / 100;
        double caffeine = beverage.getCaffeine() * volume / 100;

        return BeverageSize.builder()
                .beverage(beverage)
                .sizeType(sizeType)
                .volume(volume)
                .sugar(sugar)
                .calories(calories)
                .caffeine(caffeine)
                .build();
    }
}