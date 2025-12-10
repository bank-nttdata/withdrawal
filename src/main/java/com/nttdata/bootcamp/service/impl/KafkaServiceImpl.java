package com.nttdata.bootcamp.service.impl;

import com.nttdata.bootcamp.entity.Withdrawal;
import com.nttdata.bootcamp.entity.enums.EventType;
import com.nttdata.bootcamp.events.EventKafka;
import com.nttdata.bootcamp.events.WithdrawalCreatedEventKafka;
import com.nttdata.bootcamp.service.KafkaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.Date;
import java.util.UUID;

@Service
public class KafkaServiceImpl implements KafkaService {

    private final KafkaSender<String, EventKafka<?>> sender;

    @Value("${topic.withdrawal.name}")
    private String topicWithdrawal;

    public KafkaServiceImpl(KafkaSender<String, EventKafka<?>> sender) {
        this.sender = sender;
    }

    @Override
    public Mono<Void> publish(Withdrawal withdrawal) {

        WithdrawalCreatedEventKafka event = new WithdrawalCreatedEventKafka();
        event.setId(UUID.randomUUID().toString());
        event.setType(EventType.CREATED);
        event.setDate(new Date());
        event.setData(withdrawal);

        return sender.send(
                        Mono.just(
                                SenderRecord.create(
                                        topicWithdrawal,
                                        null,
                                        null,
                                        withdrawal.getWithdrawalNumber(),
                                        event,
                                        null
                                )
                        )
                )
                .doOnNext(result ->
                        System.out.println("Event sent to Kafka topic: " + topicWithdrawal)
                )
                .then(); // Mono<Void>
    }
}
