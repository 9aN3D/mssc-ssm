package guru.springframework.mssc.ssm.services;

import guru.springframework.mssc.ssm.domain.Payment;
import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static guru.springframework.mssc.ssm.domain.PaymentState.PRE_AUTH;
import static guru.springframework.mssc.ssm.domain.PaymentState.PRE_AUTH_ERROR;

@Slf4j
@SpringBootTest
class DefaultPaymentServiceTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .amount(BigDecimal.TEN)
                .build();
    }

    @Test
    @Transactional
    void preAuth() {

        Payment savedPayment = paymentService.create(payment);

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.preAuth(savedPayment.getId());

        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        log.debug("State machine state id {}", stateMachine.getState().getId());

        log.debug("preAuthPayment {}", preAuthPayment);

        Assertions.assertTrue(Set.of(PRE_AUTH, PRE_AUTH_ERROR).contains(stateMachine.getState().getId()));
        Assertions.assertTrue(Set.of(PRE_AUTH, PRE_AUTH_ERROR).contains(preAuthPayment.getState()));

    }

}
