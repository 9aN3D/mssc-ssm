package guru.springframework.mssc.ssm.services;

import guru.springframework.mssc.ssm.domain.Payment;
import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import static guru.springframework.mssc.ssm.domain.PaymentState.NEW;

public interface PaymentService {

    Payment create(Payment payment);

    StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId);

    StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId);

    @Slf4j
    @Service
    @RequiredArgsConstructor
    class DefaultPaymentService implements PaymentService {

        private final PaymentRepository repository;
        private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

        @Override
        public Payment create(Payment payment) {
            payment.setState(NEW);
            return repository.save(payment);
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
            return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
            return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
            return null;
        }

    }

}
