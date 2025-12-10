package com.nttdata.bootcamp.controller;

import com.nttdata.bootcamp.entity.Withdrawal;
import com.nttdata.bootcamp.entity.dto.WithdrawalDto;
import com.nttdata.bootcamp.service.WithdrawalService;
import com.nttdata.bootcamp.util.Constant;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/withdrawal")
public class WithdrawalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WithdrawalController.class);

    private final WithdrawalService withdrawalService;

    public WithdrawalController(WithdrawalService withdrawalService) {
        this.withdrawalService = withdrawalService;
    }

    // ===============================
    // FIND ALL
    // ===============================
    @GetMapping("/findAllWithdrawal")
    public Flux<Withdrawal> findAllWithdrawal() {
        return withdrawalService.findAll()
                .doOnSubscribe(x -> LOGGER.info("Buscando todos los withdrawals"))
                .doOnNext(w -> LOGGER.info("Withdrawal encontrado: {}", w));
    }

    // ===============================
    // FIND BY ACCOUNT NUMBER
    // ===============================
    @GetMapping("/findAllWithdrawalByAccountNumber/{accountNumber}")
    public Flux<Withdrawal> findAllByAccountNumber(
            @PathVariable String accountNumber) {

        return withdrawalService.findByAccountNumber(accountNumber)
                .doOnSubscribe(s -> LOGGER.info("Buscando withdrawals por cuenta {}", accountNumber))
                .doOnNext(w -> LOGGER.info("Withdrawal: {}", w));
    }

    // ===============================
    // FIND BY WITHDRAWAL NUMBER
    // ===============================
    @CircuitBreaker(name = "withdrawal", fallbackMethod = "fallBackGetWithdrawal")
    @GetMapping("/findByWithdrawalNumber/{number}")
    public Mono<Withdrawal> findByWithdrawalNumber(@PathVariable String number) {
        return withdrawalService.findByNumber(number)
                .doOnSubscribe(s -> LOGGER.info("Buscando withdrawal por número {}", number));
    }

    // ===============================
    // SAVE (TOTALMENTE REACTIVO)
    // ===============================
    @CircuitBreaker(name = "withdrawal", fallbackMethod = "fallBackGetWithdrawal")
    @PostMapping("/saveWithdrawal")
    public Mono<Withdrawal> saveWithdrawal(@RequestBody WithdrawalDto dto) {

        return getCountWithdrawals(dto.getAccountNumber())
                .map(count -> {

                    Withdrawal w = new Withdrawal();
                    w.setDni(dto.getDni());
                    w.setAccountNumber(dto.getAccountNumber());
                    w.setWithdrawalNumber(dto.getWithdrawalNumber());
                    w.setAmount(dto.getAmount());
                    w.setStatus(Constant.STATUS_ACTIVE);
                    w.setTypeAccount(Constant.TYPE_ACCOUNT);
                    w.setCreationDate(new Date());
                    w.setModificationDate(new Date());
                    w.setCommission(count > Constant.COUNT_TRANSACTION
                            ? Constant.COMISSION
                            : 0.00);

                    return w;
                })
                .flatMap(withdrawalService::saveWithdrawal)
                .doOnSuccess(w -> LOGGER.info("Withdrawal registrado {}", w))
                .doOnError(e -> LOGGER.error("Error registrando withdrawal: {}", e.getMessage()));
    }

    // ===============================
    // UPDATE
    // ===============================
    @CircuitBreaker(name = "withdrawal", fallbackMethod = "fallBackGetWithdrawal")
    @PutMapping("/updateWithdrawal/{number}")
    public Mono<Withdrawal> updateWithdrawal(
            @PathVariable String number,
            @RequestBody Withdrawal data) {

        data.setWithdrawalNumber(number);
        data.setModificationDate(new Date());

        return withdrawalService.updateWithdrawal(data)
                .doOnSuccess(w -> LOGGER.info("Withdrawal actualizado {}", w))
                .doOnError(e -> LOGGER.error("Error actualizando withdrawal: {}", e.getMessage()));
    }

    // ===============================
    // DELETE
    // ===============================
    @CircuitBreaker(name = "withdrawal", fallbackMethod = "fallBackGetWithdrawalVoid")
    @DeleteMapping("/deleteWithdrawal/{number}")
    public Mono<Void> deleteWithdrawal(@PathVariable String number) {
        return withdrawalService.deleteWithdrawal(number)
                .doOnSuccess(v -> LOGGER.info("Withdrawal eliminado {}", number))
                .doOnError(e -> LOGGER.error("Error eliminando withdrawal: {}", e.getMessage()));
    }

    // ===============================
    // COUNT
    // ===============================
    @GetMapping("/getCountDeposits/{accountNumber}")
    public Mono<Long> getCountWithdrawals(@PathVariable String accountNumber) {
        return withdrawalService.findByAccountNumber(accountNumber)
                .count()
                .doOnSuccess(c -> LOGGER.info("Cantidad de withdrawals: {}", c));
    }

    // ===============================
    // GET COMMISSIONS
    // ===============================
    @GetMapping("/getCommissionsWithdrawal/{accountNumber}")
    public Flux<Withdrawal> getCommissionsWithdrawal(@PathVariable String accountNumber) {
        return withdrawalService.findByCommission(accountNumber)
                .doOnSubscribe(s -> LOGGER.info("Buscando comisiones de cuenta {}", accountNumber))
                .doOnNext(w -> LOGGER.info("Withdrawal con comisión {}", w));
    }

    // ===============================
    // FALLBACKS
    // ===============================
    private Mono<Withdrawal> fallBackGetWithdrawal(Exception e) {
        LOGGER.error("Fallback ejecutado: {}", e.getMessage());
        return Mono.just(new Withdrawal());
    }

    private Mono<Void> fallBackGetWithdrawalVoid(Exception e) {
        LOGGER.error("Fallback ejecutado delete: {}", e.getMessage());
        return Mono.empty();
    }

}
