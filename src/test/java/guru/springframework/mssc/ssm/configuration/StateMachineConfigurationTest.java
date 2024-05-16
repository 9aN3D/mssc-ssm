package guru.springframework.mssc.ssm.configuration;

import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

import java.util.UUID;

import static guru.springframework.mssc.ssm.domain.PaymentEvent.PRE_AUTHORIZE;
import static guru.springframework.mssc.ssm.domain.PaymentEvent.PRE_AUTH_APPROVED;

@Slf4j
@SpringBootTest
class StateMachineConfigurationTest {

    @Autowired
    StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;

    @Test
    void testNewStateMachine() {
        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(UUID.randomUUID());

        stateMachine.start();

        log.debug("State machine started {sate: {}}", stateMachine.getState());

        stateMachine.sendEvent(PRE_AUTHORIZE);

        log.debug("State machine sent event PRE_AUTHORIZE {sate: {}}", stateMachine.getState());

        stateMachine.sendEvent(PRE_AUTH_APPROVED);

        log.debug("State machine sent event PRE_AUTH_APPROVED {sate: {}}", stateMachine.getState());
    }

}
