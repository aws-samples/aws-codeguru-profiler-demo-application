/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import software.amazon.awssdk.regions.Region;
import software.amazon.codeguruprofilerjavaagent.Profiler;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.regions.Regions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    static String roleArn;
    static String sqsQueueUrl;
    static String bucketName;
    static String sessionName = "codeguru-session";
    static String profileGroupName;
    static Region awsRegion = Region.EU_WEST_1;
    static Regions awsRegions = Regions.EU_WEST_1;


    static String sampleImagesFolder = "input-images/";
    static boolean withIssues;
    static boolean reuseMapper;
    static boolean reuseLogger;

    static AmazonS3 sharedS3 = s3Client();
    static AmazonSQS sharedSqs = sqsClient();
    static ObjectMapper sharedObjectMapper = objectMapper();
    static Logger sharedLogger = logger();
    static ExecutorService sharedExecutor = executor();

    public static void main(String[] args) throws Exception {
        /*
          This demo application can be configured to demonstrate some common performance issues,
          for example expensive logging or forgetting to re-use serializers.
         */
    	
       	Main.roleArn = args[1];
    	Main.sqsQueueUrl = args[2];
    	Main.bucketName = args[3];
    	
        if (args.length > 0 && args[0].equals("with-issues")) {
            logger().info("Running with performance issues.");

            withIssues = true;
            reuseMapper = false;
            reuseLogger = false;
            profileGroupName = "codeguru-java-app-with-problems";
        } else if (args.length > 0 && args[0].equals("without-issues")) {
            logger().info("Running without performance issues.");

            withIssues = false;
            reuseMapper = true;
            reuseLogger = true;
            profileGroupName = "codeguru-java-app-without-problems";
            
        } else {
            logger().error("Invalid arguments: '" + String.join(" ", args) + "'. Valid arguments are: with-issues or without-issues.");
            System.exit(-1);
        }
        
        Profiler.builder()
        .profilingGroupName(profileGroupName)
        .awsCredentialsProvider(AwsCredsProvider.getCredentials(roleArn, sessionName))
        .awsRegionToReportTo(awsRegion)
        .build()
        .start();

        // Publisher
        ScheduledExecutorService publisherScheduler = Executors.newScheduledThreadPool(1);
        publisherScheduler.scheduleWithFixedDelay(() -> TaskPublisher.publishImageTransformTask(10), 0, 5, TimeUnit.SECONDS);

        // Listener
        ImageProcessor imageProcessor = new ImageProcessor();

        while (true) {
            executor().submit(imageProcessor::run).get();
        }
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
