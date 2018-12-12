package com.challenge.ordermaker.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ProductDao {
    private final long productId;
    private final String name;
    private final float price;
    private final int version;
    private final boolean latest;
}