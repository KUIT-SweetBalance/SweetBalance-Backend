package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.common.Status;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beverage_logs")
@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BeverageLog extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private BeverageSize beverageSize;

    @Column(name = "syrup_name", length = 50)
    private String syrupName;

    @Column(name = "syrup_count")
    private int syrupCount;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Column(name = "additional_sugar")
    private double additionalSugar;


    @Column(name = "read_by_user")
    private Boolean readByUser;

    /**
     * 음료 기록 수정
     */
    public void updateRecord(BeverageSize beverageSize, String syrupName, int syrupCount, double additionalSugar) {
        this.beverageSize = beverageSize;
        this.syrupName = syrupName;
        this.syrupCount = syrupCount;
        this.additionalSugar = additionalSugar;
        this.readByUser = false;
    }

    /**
     * 음료 기록을 삭제(논리적으로)
     */
    public void markDeleted() {
        this.status = Status.DELETED;
    }

}
