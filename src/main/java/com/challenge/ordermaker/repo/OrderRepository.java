package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.dao.OrderDao;

import java.time.LocalDateTime;
import java.util.Set;


public interface OrderRepository {

    Set<OrderDao> retrieveAllBetween(LocalDateTime startTime, LocalDateTime endTime);

    OrderDao get(long orderId);

    OrderDao save(OrderCreateRequest order);
}
