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

import com.google.common.base.Optional;
import java.util.Date;
import java.util.Random;

public class CreateOrderThread extends Thread{

    static int id = 0;

    private volatile boolean exit = false;

    private static Random random = new Random();

    public void run() {
        while (!exit) {
            createOrder("APPLE");
            createOrder("ORANGE");
            createOrder("PINEAPPLE");
        }

    }

    /**
     * Create random orders
     * @param productName
     */
    public void createOrder(String productName){
        try {
            Date orderDate = Util.getRandomDate();
            Optional<ProductName> optional = ProductName.getProductName(productName);

            if (!optional.isPresent()) {
                return;
            }

            ProductName enumProductName = optional.get();

            Order order = new Order(enumProductName, orderDate, random.nextDouble() * 10000, id);

			if (SalesSystem.orders.size() > 10000) {
				SalesSystem.orders.clear();
				id = 0;
			}

			SalesSystem.orders.put(orderDate, order);
			id++;
		} catch (IllegalArgumentException e){
            //e.printStackTrace();
            //not showing exception stack trace here because it will wash away Profiler's running log
        }
    }

}

