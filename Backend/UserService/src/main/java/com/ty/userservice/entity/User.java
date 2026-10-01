package com.ty.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "phone")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userid;

   private String username;
    private String email;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    private String password;
    private String role="USER";
    private String status="Active";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp created_at;

    @Column(name = "updated_at")
    private Timestamp updated_at;

    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        created_at = now;
        updated_at = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = new Timestamp(System.currentTimeMillis());
    }
}