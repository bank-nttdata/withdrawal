package com.nttdata.bootcamp.service;

import com.nttdata.bootcamp.entity.Withdrawal;
import reactor.core.publisher.Mono;

public interface KafkaService {
    Mono<Void> publish(Withdrawal withdrawal);
}
