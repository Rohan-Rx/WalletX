package com.ty.walletservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true, nullable = false)
    private String transactionId;
    private String walletId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String description;
    private Timestamp timestamp;
    @PrePersist
    protected void onCreate(){
        timestamp= new Timestamp(System.currentTimeMillis());
        if (transactionId == null) {
            transactionId = UUID.randomUUID().toString();
        }
    }

}
