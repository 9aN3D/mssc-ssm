package guru.springframework.mssc.ssm.services;

import guru.springframework.mssc.ssm.domain.Payment;
import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH;
import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH_ERROR;
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

        debug(stateMachine, preAuthPayment);

        Set<PaymentState> expectedStates = Set.of(PRE_AUTH, PRE_AUTH_ERROR);
        Assertions.assertTrue(expectedStates.contains(stateMachine.getState().getId()));
        Assertions.assertTrue(expectedStates.contains(preAuthPayment.getState()));

    }

    @Test
    @Transactional
    void auth() {

        payment.setState(PRE_AUTH);
        Payment savedPayment = paymentRepository.save(payment);

        StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.authorize(savedPayment.getId());

        Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());

        debug(stateMachine, preAuthPayment);

        Set<PaymentState> expectedStates = Set.of(AUTH, AUTH_ERROR);
        Assertions.assertTrue(expectedStates.contains(stateMachine.getState().getId()));
        Assertions.assertTrue(expectedStates.contains(preAuthPayment.getState()));

    }

    @RepeatedTest(10)
    @Transactional
    void preAuthWithAuth() {

        Payment savedPayment = paymentService.create(payment);

        StateMachine<PaymentState, PaymentEvent> preAuthStateMachine = paymentService.preAuth(savedPayment.getId());

        if (preAuthStateMachine.getState().getId() == PRE_AUTH) {
            StateMachine<PaymentState, PaymentEvent> stateMachine = paymentService.authorize(savedPayment.getId());

            Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());
            debug(stateMachine, preAuthPayment);

            Set<PaymentState> expectedStates = Set.of(AUTH, AUTH_ERROR);
            Assertions.assertTrue(expectedStates.contains(stateMachine.getState().getId()));
            Assertions.assertTrue(expectedStates.contains(preAuthPayment.getState()));
        } else {
            Payment preAuthPayment = paymentRepository.getOne(savedPayment.getId());
            debug(preAuthStateMachine, preAuthPayment);
            Assertions.assertEquals(PRE_AUTH_ERROR, preAuthStateMachine.getState().getId());
            Assertions.assertEquals(PRE_AUTH_ERROR, preAuthPayment.getState());
        }



    }

    private static void debug(StateMachine<PaymentState, PaymentEvent> stateMachine, Payment preAuthPayment) {
        log.debug("State machine state id {}", stateMachine.getState().getId());

        log.debug("preAuthPayment {}", preAuthPayment);
    }

}
