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
public class PreAuthAction implements Action<PaymentState, PaymentEvent> {

    @Override
    public void execute(StateContext<PaymentState, PaymentEvent> context) {
        log.debug("PreAuth was called!!!");

        if (new Random().nextInt(10) < 8) {
            log.debug("Approved");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                    .setHeader(PaymentService.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER))
                    .build());
        } else {
            log.debug("Declined! No Credit!!!!!!");
            context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                    .setHeader(PaymentService.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentService.PAYMENT_ID_HEADER))
                    .build());
        }
    }

}

