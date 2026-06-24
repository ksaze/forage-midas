package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {

  private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

  private final DatabaseConduit databaseConduit;
  private final IncentiveClient incentiveClient;

  public TransactionProcessor(DatabaseConduit databaseConduit, IncentiveClient incentiveClient) {
    this.databaseConduit = databaseConduit;
    this.incentiveClient = incentiveClient;
  }

  public boolean isValid(Transaction transaction, UserRecord sender, UserRecord recipient) {
    if (sender == null || recipient == null) {
      logger.info("Discarding transaction, invalid sender/recipient: {}", transaction);
      return false;
    }
    if (sender.getBalance() < transaction.getAmount()) {
      logger.info("Discarding transaction, insufficient funds: {}", transaction);
      return false;
    }
    return true;
  }

  public void process(Transaction transaction) {
    UserRecord sender = databaseConduit.findUserById(transaction.getSenderId());
    UserRecord recipient = databaseConduit.findUserById(transaction.getRecipientId());

    if (!isValid(transaction, sender, recipient)) {
      return;
    }

    Incentive incentive = incentiveClient.getIncentive(transaction);
    float incentiveAmount = incentive.getAmount();

    sender.setBalance(sender.getBalance() - transaction.getAmount());
    recipient.setBalance(recipient.getBalance() + transaction.getAmount() + incentiveAmount);

    databaseConduit.save(sender);
    databaseConduit.save(recipient);
    databaseConduit.save(new TransactionRecord(sender, recipient, transaction.getAmount(), incentiveAmount));

    logger.info("Processed. sender={} newBalance={}, recipient={} newBalance={}, incentive={}",
        sender.getName(), sender.getBalance(),
        recipient.getName(), recipient.getBalance(),
        incentiveAmount);
  }
}
