/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

class ImageProcessor {

    private static final String bwFolder = "bw-images/";
    private static final String brightenFolder = "brighten-images/";

    void run() {
        try {
            List<Message> messages = extractTasks(Main.sqsQueueUrl);

            Main.logger().debug("Received " + messages.size() + " messages");

            for (Message message : messages) {
                Main.logger().debug("Processing message...");

                BWImageProcessor bwip = new BWImageProcessor();
                BrightenImageProcessor bip = new BrightenImageProcessor();

                String imageKey;
                try {
                    imageKey = SqsMessage.deserialize(message.getBody()).imageKey;
                } catch (RuntimeException e) {
                    Main.logger().debug("Could not parse: " + message.getBody());
                    continue;
                }

                String imageName = getNameFromKey(imageKey);
                Main.logger().debug("Image name: " + imageName);
                File outputFile = File.createTempFile(Instant.now().toString() + imageName, null);
                IOUtils.copy(Main.s3Client().getObject(Main.bucketName, imageKey).getObjectContent(), new FileOutputStream(outputFile, false));

                bwip.monochromeAndUpload(outputFile, imageName);
                bip.brightenAndUpload(outputFile, imageName);

                deleteFile(outputFile);
                sqsClient.deleteMessage(Main.sqsQueueUrl, message.getReceiptHandle());

                Main.logger().debug("Message processed.");
            }
        } catch (Exception e) {
            Main.logger().debug("Image Processor failed: " + e);
            e.printStackTrace();
        }
    }

    // always re-use this one, because otherwise it just takes up too much of the profile opening ssl connections
    static AmazonSQS sqsClient = AmazonSQSClientBuilder.defaultClient();

    private List<Message> extractTasks(String sqsQueueURL) {
        ReceiveMessageResult response = sqsClient.receiveMessage(
                new ReceiveMessageRequest()
                        .withMessageAttributeNames("key")
                        .withQueueUrl(sqsQueueURL)
        );

        List<Message> messages = response.getMessages();

        try {
            if (Main.withIssues) {
                for (int i = 0; i < 10; i++) {
                    Main.logger().debug("Pointless work: " + Main.objectMapper().writeValueAsString(Main.sqsClient().getClass()));
                    Main.logger().error("Expensive exception: ", new Exception());
                }
                Main.logger().debug("Result from SQS: " + Main.objectMapper().writeValueAsString(response));
                Main.logger().debug("Messages: " + Main.objectMapper().writeValueAsString(messages));
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return messages;
    }

    private String getNameFromKey(String key) {
        String[] keySplit = key.split("/");
        return keySplit[keySplit.length - 1];
    }

    private class BWImageProcessor {
        private void monochromeAndUpload(File file, String uploadFileName) {
            try {
                File bwFile = File.createTempFile("bw-" + Instant.now().toString() + uploadFileName, null);
                BufferedImage dest = ImageEditor.monochrome(ImageIO.read(file));
                ImageIO.write(dest, "PNG", bwFile);
                PutObjectResult res = upload(
                        bwFolder + uploadFileName + Instant.now().toString(),
                        bwFile
                );
                res.getContentMd5();
                deleteFile(bwFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class BrightenImageProcessor {
        private void brightenAndUpload(File file, String uploadFileName) {
            try {
                File brightened = File.createTempFile("bright-" + Instant.now().toString() + uploadFileName, null);
                BufferedImage dest = ImageEditor.brightenImage(ImageIO.read(file));
                ImageIO.write(dest, "PNG", brightened);
                PutObjectResult res = upload(
                        brightenFolder + uploadFileName + Instant.now().toString(),
                        brightened
                );
                res.getContentMd5();
                deleteFile(brightened);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private PutObjectResult upload(String fileName, File file) {
        return Main.s3Client().putObject(
                new PutObjectRequest(
                        Main.bucketName,
                        fileName,
                        file
                )
        );
    }

    private void deleteFile(File file) {
        if (!file.delete()) {
            Main.logger().debug("Failed to remove file: " + file.getPath());
        }
    }
}
