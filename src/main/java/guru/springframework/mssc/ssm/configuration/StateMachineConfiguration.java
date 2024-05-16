package guru.springframework.mssc.ssm.configuration;

import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;

import java.util.EnumSet;

import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH;
import static guru.springframework.mssc.ssm.domain.PaymentState.AUTH_ERROR;
import static guru.springframework.mssc.ssm.domain.PaymentState.NEW;
import static guru.springframework.mssc.ssm.domain.PaymentState.PRE_AUTH_ERROR;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfiguration extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(AUTH)
                .end(PRE_AUTH_ERROR)
                .end(AUTH_ERROR);
    }

}
