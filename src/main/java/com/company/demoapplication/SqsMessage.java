/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 */

package com.company.demoapplication;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class SqsMessage {

    public String imageKey;
    public Map<String, Map<String, String>> dummyData = new HashMap<>();

    public SqsMessage() {

    }

    public SqsMessage(String imageKey) {
        this.imageKey = imageKey;

        Map<String, String> dummyInner = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            dummyInner.put("foo" + i, "bar" + i);
        }
        for (int i = 0; i < 20; i++) {
            dummyData.put("foo" + i, dummyInner);
        }
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    public void setDummyData(Map<String, Map<String, String>> dummyData) {
        this.dummyData = dummyData;
    }

    static SqsMessage deserialize(String serialized) {
        try {
            return Main.objectMapper().readValue(serialized, SqsMessage.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    String serialize() {
        try {
            return Main.objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
