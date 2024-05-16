package guru.springframework.mssc.ssm.configuration;

import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH;
import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH_ERROR;
import static guru.springframework.mssc.ssm.domain.PaymentState.NEW;
import static guru.springframework.mssc.ssm.domain.PaymentState.PRE_AUTH;
import static guru.springframework.mssc.ssm.domain.PaymentState.PRE_AUTH_ERROR;
import static java.util.Objects.nonNull;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class StateMachineConfiguration extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(AUTH)
                .end(PRE_AUTH_ERROR)
                .end(AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(NEW).target(NEW).event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction).guard(paymentGuard())
                .and()
                .withExternal().source(NEW).target(PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED).action(preAuthApprovedAction)
                .and()
                .withExternal().source(NEW).target(PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .and()
                .withExternal().source(PRE_AUTH).target(PRE_AUTH).event(PaymentEvent.AUTHORIZE).action(authAction)
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH).event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PRE_AUTH).target(AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info("State changed from {} , {}", from, to);
            }
        };
        config.withConfiguration().listener(adapter);
    }

    private Guard<PaymentState, PaymentEvent> paymentGuard() {
        return context -> nonNull(context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER));
    }

}
