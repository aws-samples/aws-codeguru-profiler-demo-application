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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListOrderThread extends Thread {
    private volatile boolean exit = false;

    /**
     * Resolution: uncomment the following line to see how this improves the profile.
     */
//    private  static DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance();
//    private  static DateFormat myFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", dateFormatSymbols);
//    private  static DateFormat todayFormat = new SimpleDateFormat("dd MMM yyyy", dateFormatSymbols);
    /**
     * Here DateFormatSymbols are not provided to the SimpleDateFormat
     * constructor and it will look up on every call, comment the below two lines
     */
    private static DateFormat myFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    private static DateFormat todayFormat = new SimpleDateFormat("dd MMM yyyy");

    private static String today = null;

    @Override
    public void run() {
        while (!exit) {
            listOrders();

        }
    }

    /**
     * List the same day orders
     */
    private void listOrders(){
        ObjectMapper objectMapper = new ObjectMapper();

        synchronized (SalesSystem.orders) {
            for(Date orderDate: SalesSystem.orders.keySet()){
                try {
                    objectMapper.setDateFormat(myFormat);
                    Date todayDate = todayFormat.parse(this.today);
                    if(Util.isSameDay(orderDate, todayDate)) {
                        String orderAsString = objectMapper.writeValueAsString(SalesSystem.orders.get(orderDate));
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setDate(String today){
        this.today = today;
    }
}

