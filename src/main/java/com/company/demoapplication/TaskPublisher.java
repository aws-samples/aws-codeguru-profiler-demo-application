/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TaskPublisher {
    private static Random randomGenerator = new Random();

    static void publishImageTransformTask(Integer numOfTasks) {
        try {
            List<String> list = listImagesOnS3();

            if (list.isEmpty()) {
                Main.logger().info("No images in bucket. Uploading example image...");
                Main.sharedS3.putObject(Main.bucketName, Main.sampleImagesFolder + "example-image.png", new File("src/main/resources/example-image.png"));
                return;
            }

            Main.logger().debug("Starting...");
            Main.logger().debug(list);
            IntStream
                    .range(0, numOfTasks)
                    .mapToObj(i -> list.get(randomGenerator.nextInt(list.size())))
                    .forEach(key -> {
                        try {
                            Main.sharedSqs.sendMessage(createRequest(Main.sqsQueueUrl, key));
                        } catch (RuntimeException e) {
                            Main.logger().debug("Exception while sending task to SQS: " + e);
                        }
                        Main.logger().debug("Sent task to SQS.");
                    });
        } catch (Exception e) {
            // since this runs async, easiest way to catch errors is to fail hard!
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static SendMessageRequest createRequest(String sqsQueueUrl, String key) {
        return new SendMessageRequest()
                   .withMessageBody(new SqsMessage(key).serialize())
                   .withQueueUrl(sqsQueueUrl);
    }

    private static List<String> listImagesOnS3() {
        List<S3ObjectSummary> images = Main.s3Client().listObjectsV2(new ListObjectsV2Request().withBucketName(Main.bucketName).withPrefix(Main.sampleImagesFolder)).getObjectSummaries();
        return images.stream()
                   .filter(summary -> !summary.getKey().trim().equals(Main.sampleImagesFolder))
                   .map(summary -> summary.getKey().trim())
                   .collect(Collectors.toList());
    }
}
