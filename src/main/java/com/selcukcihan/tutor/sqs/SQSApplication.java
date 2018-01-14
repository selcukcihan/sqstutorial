package com.selcukcihan.tutor.sqs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.selcukcihan.tutor.sqs.business.SQSDriver;
import com.selcukcihan.tutor.sqs.configuration.SQSModule;

public class SQSApplication {
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new SQSModule(args));
        injector.getInstance(SQSDriver.class).loop();
    }
}
