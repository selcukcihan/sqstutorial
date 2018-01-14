package com.selcukcihan.tutor.sqs.configuration;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.PrintStream;
import java.util.Scanner;

public class SQSModule extends AbstractModule {
    private final String[] args;

    public SQSModule(String[] args) {
        this.args = args.clone();
    }

    @Override
    protected void configure() {
        bind(Scanner.class).toInstance(new Scanner(System.in));
        bind(PrintStream.class).toInstance(System.out);
    }

    @Provides
    private AmazonSQS provideSQS() {
        AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
        return sqs;
    }
}
