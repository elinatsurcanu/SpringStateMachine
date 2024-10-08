package com.hareesh.springstatemachine.springstatemachinedemo.payment.services;

import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.exception.InsufficientFundsException;
import com.hareesh.springstatemachine.springstatemachinedemo.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    final static Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository repository;

    private final StateMachine<PaymentState, PaymentEvent> stateMachine;

    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment getPaymentById(Long paymentId) {
        return repository.getPaymentById(paymentId);
    }

    @Override
    public Payment createNewPayment(BigDecimal amount) throws InsufficientFundsException {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setState(PaymentState.INITIAL);
        Payment createdPayment = repository.save(payment);

        StateMachine<PaymentState, PaymentEvent> sm = build(payment.getId());
        preAuth(createdPayment.getId(), amount, sm);

        return payment;
    }

    @Override
    public Payment processPayment(Long paymentId) throws InsufficientFundsException {
        Payment payment = getPaymentById(paymentId);
        if(payment == null) {
            LOGGER.error("Payment with id {} not found", paymentId);
            throw new RuntimeException("Payment with id " + paymentId + " not found");
        }
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        if (payment.getAmount().compareTo(Account.accountBalance) < 0) {
            Account.accountBalance = Account.accountBalance.subtract(payment.getAmount());
            sendEvent(paymentId, sm, PaymentEvent.SUBTRACT_MONEY);
        } else {
            sendEvent(paymentId, sm, PaymentEvent.DECLINE_PAYMENT);
            LOGGER.error("Payment with id {} has been declined. Not enough money", paymentId);
            throw new InsufficientFundsException("Not enough balance");
        }
        return payment;
    }

    @Override
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    @Override
    public void preAuth(Long paymentId, BigDecimal amount, StateMachine<PaymentState, PaymentEvent> sm) throws InsufficientFundsException {
        sm.getExtendedState().getVariables().put("amount", amount);
        sm.getExtendedState().getVariables().put("paymentId", paymentId);
        if(amount.compareTo(BigDecimal.valueOf(50000L)) < 0) {
            sendEvent(paymentId, sm, PaymentEvent.CREATE_PAYMENT);
        } else {
            sendEvent(paymentId, sm, PaymentEvent.DECLINE_PAYMENT);
            LOGGER.error("Payment with id {} has been declined. The amount {} is bigger than 50 000", paymentId, amount);
            throw new InsufficientFundsException("The amount is bigger than 50 000, payment is declined.");
        }
    }


    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){
        Message<PaymentEvent> msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
        Payment payment = repository.getOne(paymentId);

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;
    }
}
