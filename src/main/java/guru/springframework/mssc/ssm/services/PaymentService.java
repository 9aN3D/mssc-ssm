package guru.springframework.mssc.ssm.services;

import guru.springframework.mssc.ssm.domain.Payment;
import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import static guru.springframework.mssc.ssm.domain.PaymentEvent.AUTH_APPROVED;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.AUTH_DECLINED;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.PRE_AUTHORIZE;
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

        private static final String PAYMENT_ID_HEADER = "payment_id";

        private final PaymentRepository repository;
        private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

        @Override
        public Payment create(Payment payment) {
            payment.setState(NEW);
            return repository.save(payment);
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
            StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

            sendEvent(paymentId, stateMachine, PRE_AUTHORIZE);

            return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
            StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

            sendEvent(paymentId, stateMachine, AUTH_APPROVED);

            return null;
        }

        @Override
        public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
            StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

            sendEvent(paymentId, stateMachine, AUTH_DECLINED);

            return null;
        }

        private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
            Payment payment = repository.getOne(paymentId);

            StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(Long.toString(paymentId));

            stateMachine.stop();

            stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(sma -> {
                        sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                    });

            stateMachine.start();
            return stateMachine;
        }

        private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event) {
            stateMachine.sendEvent(MessageBuilder.withPayload(event)
                    .setHeader(PAYMENT_ID_HEADER, paymentId)
                    .build()
            );
        }

    }

}
