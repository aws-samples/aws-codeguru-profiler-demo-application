/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

class ImageEditor {

    static BufferedImage brightenImage(BufferedImage image) {
        return Scalr.apply(image, Scalr.OP_BRIGHTER);
    }

    static BufferedImage monochrome(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        return newImage;
    }
}
