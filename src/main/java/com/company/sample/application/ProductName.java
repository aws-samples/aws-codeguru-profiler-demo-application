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

import com.google.common.base.Enums;

import com.google.common.base.Optional;

public enum ProductName{

    APPLE, ORANGE, PINEAPPLE;

    /**
     * Pick a random value of the ProductName enum.
     * @return a random ProductName.
     */
    public static Optional<ProductName> getProductName(String name) {
        /**
         * Here is attempting to parse a value in the enum, if the value is not found in the enum,
         * it results in an exception being thrown, comment the below two lines to fix it and uncomment line 43
         */
        ProductName productName = ProductName.valueOf(name);
        return Optional.of(productName);

        /**
         * Resolution: uncomment the following line to see how this improves the profile.
         */
//        return Enums.getIfPresent(ProductName.class, name);
    }
}

