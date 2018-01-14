package com.selcukcihan.tutor.sqs.business;

import com.google.inject.Inject;
import com.selcukcihan.tutor.sqs.service.SQSException;
import com.selcukcihan.tutor.sqs.service.SQSWrapper;

import java.io.PrintStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Logger;

public class SQSDriver {
    private static final Logger LOGGER = Logger.getLogger(SQSDriver.class.getName());

    private final SQSWrapper sqsWrapper;
    private final Scanner scanner;
    private final PrintStream output;

    @Inject
    public SQSDriver(SQSWrapper sqsWrapper, Scanner scanner, PrintStream output) {
        this.sqsWrapper = sqsWrapper;
        this.scanner = scanner;
        this.output = output;
    }

    public void loop() {
        String line;
        output.println("type 'help' for available commands");
        do {
            output.print("> ");
            line = scanner.nextLine();
        } while (process(line));
    }

    private boolean process(String line) {
        String[] tokens = line.split(" ", 2);
        switch (tokens[0]) {
            case "push":
                if (tokens.length != 2) {
                    output.println("push requires a value");
                } else {
                    push(tokens[1]);
                }
                break;
            case "pop":
                pop();
                break;
            case "create":
                createQueue(tokens[1]);
                break;
            case "display":
                display();
                break;
            case "exit":
                return false;
            default:
                printHelp();
                break;
        }
        return true;
    }

    private void push(String value) {
        sqsWrapper.enqueue(value);
    }

    private void pop() {
        Optional<String> value = sqsWrapper.dequeue();
        if (value.isPresent()) {
            output.println("value popped: " + value.get());
        } else {
            output.println("queue is empty");
        }
    }

    private void createQueue(String queueName) {
        try {
            sqsWrapper.createQueue(queueName);
        } catch (SQSException e) {
            LOGGER.warning(e.toString());
        }
    }

    private void display() {
        output.println(sqsWrapper.getCurrentQueueUrl());
    }

    private void printHelp() {
        output.println("Available commands are:");
        output.println("\tcreate <queue name>");
        output.println("\tdisplay");
        output.println("\tpush <value>");
        output.println("\tpop");
        output.println("\texit");
    }
}
