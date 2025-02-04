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

    @Column(name = "read_by_user")
    private Boolean readByUser;

    public Alarm(BeverageLog log, String content, boolean b) {
        this.log = log;
        this.content = content;
        this.readByUser = b;
    }

    public static Alarm of(BeverageLog log, String content) {
        return new Alarm(log,content,false);
    }

    public void readAlarm(){
        this.readByUser = true;
    }
}