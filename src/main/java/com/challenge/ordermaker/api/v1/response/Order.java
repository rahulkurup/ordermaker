package com.challenge.ordermaker.api.v1.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;


@Getter
@RequiredArgsConstructor
public class Order {
    private final long orderId;
    private final String buyerEmailId;
    private final LocalDateTime orderTime;

    /* In real world scenario, I would only send back ids of product to clients.
       Clients can then request for details of any product if required, using product endpoint.
       But here, we are sending the entire product as part of JSON response to make it easier to view responses.
       We are anyway need to get all products for an order to calculate the order price. So, this does not bring any
       extra complexity. So, Its is nice to see entire products in response.
      */
    private final Set<Product> products;

    private final float orderCost;
}
