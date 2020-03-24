/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import com.amazonaws.services.codeguruprofiler.AmazonCodeGuruProfilerClientBuilder;
import com.amazonaws.services.codeguruprofiler.model.DescribeProfilingGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.codeguruprofilerjavaagent.Profiler;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static String sqsQueueUrl;
    static String bucketName;

    static String sampleImagesFolder = "input-images/";
    static boolean withIssues = false;

    private static String getEnvironmentVariable(String key, String exampleValue) {
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalStateException("Environment variable " + key + " must be set, e.g. " + exampleValue);
        }
        return value;
    }

    static {
        sqsQueueUrl = getEnvironmentVariable("DEMO_APP_SQS_URL", "https://sqs.eu-west-2.amazonaws.com/123456789000/ImageQueue");
        bucketName = getEnvironmentVariable("DEMO_APP_BUCKET_NAME", "test-images-for-my-demo-app");
        if (Objects.equals(System.getProperty("withIssues"), "true")) {
            logger().info("Running with performance issues.");
            withIssues = true;
        } else {
            logger().info("Running without performance issues.");
        }
    }

    static boolean reuseMapper;
    static boolean reuseLogger;
    static boolean reuseExecutor;

    static {
        /*
          This demo application can be configured to demonstrate some common performance issues,
          for example expensive logging or forgetting to re-use serializers.
         */
        if (withIssues) {
            reuseMapper = false;
            reuseLogger = false;
        } else {
            reuseMapper = true;
            reuseLogger = true;
        }
    }

    static AmazonS3 sharedS3 = s3Client();
    static AmazonSQS sharedSqs = sqsClient();
    static ObjectMapper sharedObjectMapper = objectMapper();
    static Logger sharedLogger = logger();
    static ExecutorService sharedExecutor = executor();

    public static void main(String[] args) throws Exception {
        startProfiler();

        // Publisher
        ScheduledExecutorService publisherScheduler = Executors.newScheduledThreadPool(1);
        publisherScheduler.scheduleWithFixedDelay(() -> TaskPublisher.publishImageTransformTask(10), 0, 5, TimeUnit.SECONDS);

        // Listener
        ImageProcessor imageProcessor = new ImageProcessor();

        while (true) {
            executor().submit(imageProcessor::run).get();
        }
    }

    private static void startProfiler() {
        String profilingGroupName;
        if (withIssues) {
            profilingGroupName = "DemoApplication-WithIssues";
        } else {
            profilingGroupName = "DemoApplication-WithoutIssues";
        }

        // ensure the profiling group exists, or fail early with a reasonable error message
        AmazonCodeGuruProfilerClientBuilder.defaultClient().describeProfilingGroup(new DescribeProfilingGroupRequest().withProfilingGroupName(profilingGroupName));

        Profiler profiler = new Profiler.Builder().profilingGroupName(profilingGroupName).build();
        profiler.start();
    }

    static AmazonS3 s3Client() {
        if (sharedS3 != null) {
            return sharedS3;
        } else {
            return AmazonS3ClientBuilder.defaultClient();
        }
    }

    static AmazonSQS sqsClient() {
        return AmazonSQSClientBuilder.defaultClient();
    }

    static ObjectMapper objectMapper() {
        if (reuseMapper && sharedObjectMapper != null) {
            return sharedObjectMapper;
        } else {
            return new ObjectMapper();
        }
    }

    static Logger logger() {
        if (reuseLogger && sharedLogger != null) {
            return sharedLogger;
        } else {
            return LogManager.getLogger(Main.class);
        }
    }

    static ExecutorService executor() {
        if (sharedExecutor != null) {
            return sharedExecutor;
        } else {
            return Executors.newCachedThreadPool();
        }
    }
}
