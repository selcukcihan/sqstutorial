package com.selcukcihan.tutor.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SQSWrapper {
    private static final Logger LOGGER = Logger.getLogger(SQSWrapper.class.getName());
    private final AmazonSQS sqs;
    private String currentQueueUrl;

    @Inject
    public SQSWrapper(AmazonSQS sqs) {
        this.sqs = sqs;
    }

    public void createQueue(String queueName) throws SQSException {
        try {
            CreateQueueResult createQueueResult = sqs.createQueue(queueName);

            LOGGER.info(createQueueResult.toString());
            currentQueueUrl = createQueueResult.getQueueUrl();
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                throw new SQSException(String.format("Unable to create SQS queue %s: %s", queueName, e.getMessage()));
            }
            // Queue exists, set currentQueueUrl to the requested queue
            currentQueueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
        }
    }

    public void enqueue(String payload) {
        SendMessageResult sendMessageResult = sqs.sendMessage(currentQueueUrl, payload);
        LOGGER.info(String.format("messageid: %s, sequence: %s",
                sendMessageResult.getMessageId(), sendMessageResult.getSequenceNumber()));
    }

    public Optional<String> dequeue() {
        List<Message> messages = sqs.receiveMessage(currentQueueUrl).getMessages();
        if (messages.size() > 0) {
            sqs.deleteMessage(currentQueueUrl, messages.get(0).getReceiptHandle());
            return Optional.of(messages.get(0).getBody());
        }
        return Optional.empty();
    }

    public String getCurrentQueueUrl() {
        return this.currentQueueUrl;
    }
}
