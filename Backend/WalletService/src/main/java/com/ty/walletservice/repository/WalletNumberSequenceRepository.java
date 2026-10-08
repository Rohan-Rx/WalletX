package com.ty.walletservice.repository;

import com.ty.walletservice.entity.WalletNumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WalletNumberSequenceRepository extends JpaRepository<WalletNumberSequence,Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM WalletNumberSequence s WHERE s.id = 1")
    Optional<WalletNumberSequence> findSequenceForUpdate();
}
