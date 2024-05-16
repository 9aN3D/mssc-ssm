package guru.springframework.mssc.ssm.configuration.actions;

import guru.springframework.mssc.ssm.domain.PaymentEvent;
import guru.springframework.mssc.ssm.domain.PaymentState;
import guru.springframework.mssc.ssm.services.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Random;

@Slf4j
@Component
public class PreAuthApprovedAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        log.debug("Send pre auth approved notification");
    }

}

