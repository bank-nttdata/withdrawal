package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Withdrawal;
import com.nttdata.bootcamp.repository.WithdrawalRepository;
import com.nttdata.bootcamp.service.KafkaService;
import com.nttdata.bootcamp.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Service implementation
@Service
public class WithdrawalServiceImpl implements WithdrawalService {

    @Autowired
    private WithdrawalRepository withdrawalRepository;

    @Autowired
    private KafkaService kafkaService;

    @Override
    public Flux<Withdrawal> findAll() {
        return withdrawalRepository.findAll();
    }

    @Override
    public Flux<Withdrawal> findByAccountNumber(String accountNumber) {
        return withdrawalRepository.findAll()
                .filter(x -> x.getAccountNumber().equals(accountNumber));
    }

    @Override
    public Mono<Withdrawal> findByNumber(String number) {
        return withdrawalRepository.findAll()
                .filter(x -> x.getWithdrawalNumber().equals(number))
                .next();
    }

    @Override
    public Mono<Withdrawal> saveWithdrawal(Withdrawal dataWithdrawal) {

        return findByNumber(dataWithdrawal.getWithdrawalNumber())
                .flatMap(existing ->
                        Mono.<Withdrawal>error(new Error(
                                "This withdrawal number " +
                                        dataWithdrawal.getWithdrawalNumber() + " exists"
                        ))
                )
                .switchIfEmpty(
                        withdrawalRepository.save(dataWithdrawal)
                                .flatMap(saved ->
                                        kafkaService.publish(saved).thenReturn(saved)
                                )
                );
    }

    @Override
    public Mono<Withdrawal> updateWithdrawal(Withdrawal dataWithdrawal) {
        return findByNumber(dataWithdrawal.getWithdrawalNumber())
                .switchIfEmpty(Mono.error(new Error(
                        "Withdrawal " + dataWithdrawal.getWithdrawalNumber() + " does not exist"
                )))
                .flatMap(existing -> {
                    dataWithdrawal.setDni(existing.getDni());
                    dataWithdrawal.setAmount(existing.getAmount());
                    dataWithdrawal.setCreationDate(existing.getCreationDate());
                    return withdrawalRepository.save(dataWithdrawal);
                });
    }

    @Override
    public Mono<Void> deleteWithdrawal(String number) { // VERFICAR
        return findByNumber(number)
                .switchIfEmpty(Mono.error(new Error(
                        "Withdrawal number " + number + " does not exist"
                )))
                .flatMap(withdrawalRepository::delete);
    }

    @Override
    public Flux<Withdrawal> findByCommission(String accountNumber) {
        return withdrawalRepository.findAll()
                .filter(x -> x.getCommission() > 0 &&
                        x.getAccountNumber().equals(accountNumber));
    }
}
