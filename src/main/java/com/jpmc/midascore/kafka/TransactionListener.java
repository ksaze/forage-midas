package com.jpmc.midascore.kafka;

import com.jpmc.midascore.component.TransactionProcessor;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {
  private final TransactionProcessor transactionProcessor;

  public TransactionListener(TransactionProcessor transactionProcessor) {
    this.transactionProcessor = transactionProcessor;
  }

  @KafkaListener(topics = "${general.kafka-topic}")
  public void onTransaction(Transaction transaction) {
    transactionProcessor.process(transaction);
  }
}
