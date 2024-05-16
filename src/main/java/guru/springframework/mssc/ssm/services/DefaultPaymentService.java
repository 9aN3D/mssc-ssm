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
import org.springframework.transaction.annotation.Transactional;

import static guru.springframework.mssc.ssm.domain.PaymentEvent.AUTHORIZE;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.AUTH_APPROVED;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.AUTH_DECLINED;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.PRE_AUTH_APPROVED;
import static guru.springframework.mssc.ssm.domain.PaymentState.NEW;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
class DefaultPaymentService implements PaymentService {

    private final PaymentRepository repository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Transactional
    @Override
    public Payment create(Payment payment) {
        payment.setState(NEW);
        return repository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, PRE_AUTH_APPROVED);

        return stateMachine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorize(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, AUTHORIZE);

        return stateMachine;
    }

    @Deprecated
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);

        sendEvent(paymentId, stateMachine, AUTH_DECLINED);

        return stateMachine;
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId) {
        Payment payment = repository.getOne(paymentId);

        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(Long.toString(paymentId));

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
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
