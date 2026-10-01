package com.ty.walletservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
public class Wallet {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private Integer userid;
    @Column(nullable = false, unique = true)
    private String walletId;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false)
    private String currency="INR";
    @Column(nullable = false)
    private String status="Active";
    @Column(name = "created_at" ,nullable = false,updatable = false)
    Timestamp created_at;
    @Column(name = "updated_at")
    Timestamp updated_at;

    @PrePersist
    protected void onCreate(){
        Timestamp now =new Timestamp(System.currentTimeMillis());
        created_at=now;
        updated_at=now;
    }

    @PreUpdate
    protected void onUpdate(){
        updated_at=new Timestamp(System.currentTimeMillis());
    }

}
