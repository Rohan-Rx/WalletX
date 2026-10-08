package com.ty.walletservice.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "wallet_number_sequence")
public class WalletNumberSequence {

    @Id
    private Integer id;

    private Long nextNumber;

}