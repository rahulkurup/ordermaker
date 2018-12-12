package com.challenge.ordermaker.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;

@Getter
@RequiredArgsConstructor
public class OrderDao {
    private final long orderId;
    private final String buyerEmailId;
    private final Timestamp orderTime;
}
