package com.selcukcihan.tutor.sqs.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SQSWrapperTest {
    @Test
    public void createQueue() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        CreateQueueResult createQueueResultMock = mock(CreateQueueResult.class);

        when(amazonSQSMock.createQueue("myqueue"))
                .thenReturn(createQueueResultMock);
        when(createQueueResultMock.getQueueUrl())
                .thenReturn("myqueueurl");

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");

        // then
        verify(amazonSQSMock).createQueue("myqueue");
        assertEquals("myqueueurl", sqsWrapper.getCurrentQueueUrl());
    }

    @Test (expected = SQSException.class)
    public void createQueueFails() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        AmazonSQSException amazonSQSExceptionMock = mock(AmazonSQSException.class);

        when(amazonSQSMock.createQueue("myqueue"))
                .thenThrow(amazonSQSExceptionMock);
        when(amazonSQSExceptionMock.getErrorCode())
                .thenReturn("TechnicalError");

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");
    }

    @Test
    public void createQueueExists() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        AmazonSQSException amazonSQSExceptionMock = mock(AmazonSQSException.class);
        GetQueueUrlResult getQueueUrlResultMock = mock(GetQueueUrlResult.class);

        when(amazonSQSMock.createQueue("myqueue"))
                .thenThrow(amazonSQSExceptionMock);
        when(amazonSQSMock.getQueueUrl("myqueue"))
                .thenReturn(getQueueUrlResultMock);
        when(amazonSQSExceptionMock.getErrorCode())
                .thenReturn("QueueAlreadyExists");
        when(getQueueUrlResultMock.getQueueUrl())
                .thenReturn("myqueueurl");

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");

        // then
        verify(amazonSQSMock).createQueue("myqueue");
        assertEquals("myqueueurl", sqsWrapper.getCurrentQueueUrl());
    }

    @Test
    public void enqueue() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        SendMessageResult sendMessageResultMock = mock(SendMessageResult.class);
        CreateQueueResult createQueueResultMock = mock(CreateQueueResult.class);

        when(amazonSQSMock.createQueue("myqueue"))
                .thenReturn(createQueueResultMock);
        when(createQueueResultMock.getQueueUrl())
                .thenReturn("myqueueurl");

        when(amazonSQSMock.sendMessage("myqueueurl", "payload"))
                .thenReturn(sendMessageResultMock);
        when(sendMessageResultMock.getMessageId()).thenReturn("myid");
        when(sendMessageResultMock.getSequenceNumber()).thenReturn("mysequence");

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");
        sqsWrapper.enqueue("payload");

        // then
        verify(amazonSQSMock).sendMessage("myqueueurl", "payload");
        verify(sendMessageResultMock).getMessageId();
        verify(sendMessageResultMock).getSequenceNumber();
    }

    @Test
    public void dequeue() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        ReceiveMessageResult receiveMessageResultMock = mock(ReceiveMessageResult.class);
        CreateQueueResult createQueueResultMock = mock(CreateQueueResult.class);
        Message messageMock = mock(Message.class);

        when(messageMock.getReceiptHandle())
                .thenReturn("receipthandle");
        when(messageMock.getBody())
                .thenReturn("messagebody");

        when(amazonSQSMock.createQueue("myqueue"))
                .thenReturn(createQueueResultMock);
        when(amazonSQSMock.receiveMessage("myqueueurl"))
                .thenReturn(receiveMessageResultMock);

        when(createQueueResultMock.getQueueUrl())
                .thenReturn("myqueueurl");

        when(receiveMessageResultMock.getMessages())
                .thenReturn(ImmutableList.of(messageMock));

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");
        Optional<String> message = sqsWrapper.dequeue();

        // then
        verify(amazonSQSMock).receiveMessage("myqueueurl");
        verify(amazonSQSMock).deleteMessage("myqueueurl", "receipthandle");
        verify(receiveMessageResultMock).getMessages();
        assertTrue(message.isPresent());
        assertEquals(message.get(), "messagebody");
    }

    @Test
    public void dequeueEmpty() throws Exception {
        // given
        AmazonSQS amazonSQSMock = mock(AmazonSQS.class);
        ReceiveMessageResult receiveMessageResultMock = mock(ReceiveMessageResult.class);
        CreateQueueResult createQueueResultMock = mock(CreateQueueResult.class);

        when(amazonSQSMock.createQueue("myqueue"))
                .thenReturn(createQueueResultMock);
        when(amazonSQSMock.receiveMessage("myqueueurl"))
                .thenReturn(receiveMessageResultMock);

        when(createQueueResultMock.getQueueUrl())
                .thenReturn("myqueueurl");

        when(receiveMessageResultMock.getMessages())
                .thenReturn(ImmutableList.of());

        SQSWrapper sqsWrapper = new SQSWrapper(amazonSQSMock);

        // when
        sqsWrapper.createQueue("myqueue");
        Optional<String> message = sqsWrapper.dequeue();

        // then
        verify(amazonSQSMock).receiveMessage("myqueueurl");
        verify(receiveMessageResultMock).getMessages();
        assertFalse(message.isPresent());
    }
}
