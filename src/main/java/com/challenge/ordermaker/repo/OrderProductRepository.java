package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.dao.OrderProductDao;

import java.util.Set;

public interface OrderProductRepository {

    Set<OrderProductDao> getAllForOrderId(long orderId);
}
