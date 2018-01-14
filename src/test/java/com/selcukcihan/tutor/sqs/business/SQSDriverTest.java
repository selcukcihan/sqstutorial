package com.selcukcihan.tutor.sqs.business;

import com.selcukcihan.tutor.sqs.service.SQSException;
import com.selcukcihan.tutor.sqs.service.SQSWrapper;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SQSDriverTest {
    @Mock
    private PrintStream printStreamMock;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void exit() {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine()).thenReturn("exit");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, printStreamMock);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock).nextLine();
    }

    @Test
    public void createQueue() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("create testqueue")
                .thenReturn("exit");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, printStreamMock);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();
        verify(sqsWrapperMock).createQueue("testqueue");
    }

    @Test
    public void createQueueFails() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("create testqueue")
                .thenReturn("exit");

        doThrow(new SQSException("exception"))
                .when(sqsWrapperMock).createQueue("testqueue");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, printStreamMock);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();
        verify(sqsWrapperMock).createQueue("testqueue");
    }

    @Test
    public void doPushPop() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("push myvalue")
                .thenReturn("pop")
                .thenReturn("exit");

        when(sqsWrapperMock.dequeue()).thenReturn(Optional.of("myvalue"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(3)).nextLine();
        verify(sqsWrapperMock).enqueue("myvalue");
        verify(sqsWrapperMock).dequeue();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("value popped: myvalue"));
    }

    @Test
    public void doEmptyPop() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("pop")
                .thenReturn("exit");

        when(sqsWrapperMock.dequeue()).thenReturn(Optional.empty());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();
        verify(sqsWrapperMock).dequeue();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("queue is empty"));
    }

    @Test
    public void doHelp() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("help")
                .thenReturn("exit");

        when(sqsWrapperMock.dequeue()).thenReturn(Optional.empty());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("create"));
        assertThat(logs, containsString("display"));
        assertThat(logs, containsString("push"));
        assertThat(logs, containsString("pop"));
        assertThat(logs, containsString("exit"));
    }

    @Test
    public void doDisplay() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("display")
                .thenReturn("exit");

        when(sqsWrapperMock.getCurrentQueueUrl()).thenReturn("myurl");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();
        verify(sqsWrapperMock).getCurrentQueueUrl();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("myurl"));
    }

    @Test
    public void doPushNoValue() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("push")
                .thenReturn("exit");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("push requires a value"));
    }

    @Test
    public void doPushNoQueue() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("push value")
                .thenReturn("exit");
        when(sqsWrapperMock.getCurrentQueueUrl())
                .thenReturn(null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();
        verify(sqsWrapperMock).getCurrentQueueUrl();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("create a queue first"));
    }

    @Test
    public void doCreateNoQueue() throws Exception {
        // given
        SQSWrapper sqsWrapperMock = mock(SQSWrapper.class);
        Scanner scannerMock = mock(Scanner.class);

        when(scannerMock.nextLine())
                .thenReturn("create")
                .thenReturn("exit");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos, true, "utf-8");

        SQSDriver sqsDriver = new SQSDriver(sqsWrapperMock, scannerMock, ps);

        // when
        sqsDriver.loop();

        // then
        verify(scannerMock, times(2)).nextLine();

        String logs = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        ps.close();
        assertThat(logs, containsString("create requires a queue name"));
    }
}
