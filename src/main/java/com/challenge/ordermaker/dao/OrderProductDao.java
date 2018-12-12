package com.challenge.ordermaker.dao;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class OrderProductDao {
    private final long orderId;
    private final long productId;
    private final int version;
}
