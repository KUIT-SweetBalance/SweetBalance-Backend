package com.sweetbalance.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "alarms")
@NoArgsConstructor
@AllArgsConstructor
public class Alarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "log_id", nullable = false)
    private BeverageLog log;

    @Column(name = "content", nullable = false)
    private String content;


    public Alarm(BeverageLog log, String content) {
        this.log = log;
        this.content = content;
    }

    public static Alarm of(BeverageLog log, String content) {
        return new Alarm(log,content);
    }

}