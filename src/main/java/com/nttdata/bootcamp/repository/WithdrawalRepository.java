package com.nttdata.bootcamp.repository;

import com.nttdata.bootcamp.entity.Withdrawal;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

//Mongodb Repository
public interface WithdrawalRepository extends ReactiveCrudRepository<Withdrawal, String> {
}
