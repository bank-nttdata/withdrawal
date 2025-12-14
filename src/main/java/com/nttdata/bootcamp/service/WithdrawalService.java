package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Withdrawal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//Interface Service
public interface WithdrawalService {

    public Flux<Withdrawal> findAll();
    public Flux<Withdrawal> findByAccountNumber(String accountNumber);

    public Mono<Withdrawal> findByNumber(String number);
    public Mono<Withdrawal> saveWithdrawal(Withdrawal withdrawal);
    //public Mono<Withdrawal> updateWithdrawal(Withdrawal withdrawal);
    public Mono<Void> deleteWithdrawal(String accountNumber);

    public Flux<Withdrawal> findByCommission(String accountNumber);
    public Mono<Withdrawal> updateWithdrawalStatus(String withdrawalNumber, String status);
}
