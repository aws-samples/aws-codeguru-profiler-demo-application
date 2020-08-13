/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.company.sample.application;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.codeguruprofilerjavaagent.Profiler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class SalesSystem {

    public static ConcurrentHashMap<Date, Order> orders = new ConcurrentHashMap<Date, Order>();

    public static void main(String[] args) {
        //Start the profiler
        Profiler systemProfiler =
            Profiler.builder().profilingGroupName("<Insert the profiling group name here>")
            .awsCredentialsProvider(DefaultCredentialsProvider.create())
            .build();

        systemProfiler.start();

        //Start create order thread
        CreateOrderThread createOrderThread = new CreateOrderThread();
        createOrderThread.start();

        //Start create Illegal order thread
        CreateIllegalOrderThread createIllegalOrderThread = new CreateIllegalOrderThread();
        createIllegalOrderThread.start();

        //Start list order thread
        ListOrderThread listOrderThread = new ListOrderThread();

        DateFormat currentDateFormat = new SimpleDateFormat("dd MMM yyyy");
        listOrderThread.setDate(currentDateFormat.format(new Date()));

        listOrderThread.start();
    }
}

