package com.challenge.ordermaker.api.v1.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Product {
    private final long productId;
    private final String name;
    private final float price;
}