package com.ty.walletservice.service;

import com.ty.walletservice.entity.WalletNumberSequence;
import com.ty.walletservice.repository.WalletNumberSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class WalletNumberGenerator {

    private final WalletNumberSequenceRepository sequenceRepo;

    public WalletNumberGenerator(
            WalletNumberSequenceRepository sequenceRepo) {
        this.sequenceRepo = sequenceRepo;
    }

    @Transactional
    public String generateWalletNumber() {

        WalletNumberSequence sequence =
                sequenceRepo.findSequenceForUpdate()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Wallet number sequence not initialized"
                                ));

        Long currentNumber = sequence.getNextNumber();

        sequence.setNextNumber(currentNumber + 1);

        sequenceRepo.save(sequence);

        return String.format(
                "WLT-%d-%06d",
                Year.now().getValue(),
                currentNumber
        );
    }
}