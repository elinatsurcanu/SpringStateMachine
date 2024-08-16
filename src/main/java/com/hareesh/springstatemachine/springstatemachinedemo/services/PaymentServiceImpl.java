package com.hareesh.springstatemachine.springstatemachinedemo.services;

import com.hareesh.springstatemachine.springstatemachinedemo.domain.Account;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.Payment;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentEvent;
import com.hareesh.springstatemachine.springstatemachinedemo.domain.PaymentState;
import com.hareesh.springstatemachine.springstatemachinedemo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository repository;

    private final StateMachine<PaymentState, PaymentEvent> stateMachine;

    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment getPaymentById(Long paymentId) {
        return repository.getPaymentById(paymentId);
    }

    @Override
    public Payment createNewPayment(BigDecimal amount) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setState(PaymentState.INITIAL);
        Payment createdPayment = repository.save(payment);

        StateMachine<PaymentState, PaymentEvent> sm = build(payment.getId());
        preAuth(createdPayment.getId(), amount, sm);

        payment.setState(sm.getState().getId());
        return repository.save(payment);
    }

    @Override
    public Payment processPayment(Long paymentId) {
        Payment payment = repository.getPaymentById(paymentId);
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        if (payment.getAmount().compareTo(Account.accountBalance) < 0) {
            Account.accountBalance = Account.accountBalance.subtract(payment.getAmount());
            sendEvent(paymentId, sm, PaymentEvent.SUBTRACT_MONEY);
            payment.setState(sm.getState().getId());
        } else {
            sendEvent(paymentId, sm, PaymentEvent.DECLINE_PAYMENT);
            payment.setState(sm.getState().getId());
            throw new RuntimeException("Not enough balance");
        }
        return repository.save(payment);
    }

    @Override
    public List<Payment> getAllPayments() {
        return repository.getPayments();
    }


    @Override
    public void preAuth(Long paymentId, BigDecimal amount, StateMachine<PaymentState, PaymentEvent> sm) {
        sm.getExtendedState().getVariables().put("amount", amount);
        sm.getExtendedState().getVariables().put("paymentId", paymentId);
        if(amount.compareTo(BigDecimal.valueOf(50000L)) < 0) {
            sendEvent(paymentId, sm, PaymentEvent.CREATE_PAYMENT);
        } else {
            sendEvent(paymentId, sm, PaymentEvent.DECLINE_PAYMENT);
        }
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(Long paymentId) {
        return null;
    }


    @Transactional
    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuth(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.DECLINE_PAYMENT);

        return sm;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){
        Message msg = MessageBuilder.withPayload(event)
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
