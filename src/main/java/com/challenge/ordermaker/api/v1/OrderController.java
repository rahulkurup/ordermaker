package com.challenge.ordermaker.api.v1;

import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.api.v1.request.OrdersInRangeRequest;
import com.challenge.ordermaker.api.v1.response.Order;
import com.challenge.ordermaker.service.OrderService;
import com.challenge.ordermaker.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;

    private final ProductService productService;

    @PostMapping("/place")
    @ResponseBody
    public ResponseEntity<Order> place(@Valid OrderCreateRequest orderCreateRequest) {

        /* Check if all products are valid. We use Foreign Key constraint, so the update will fail anyway even without this validation,
        but cleaner to do validation here  */
        boolean productNotFound = CheckIfAllProductsValid(orderCreateRequest.getProductSet());
        if (productNotFound) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return ResponseEntity.ok(orderService.placeOrder(orderCreateRequest));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Set<Order>> getAll(@Valid OrdersInRangeRequest ordersInRangeRequest) {
        return ResponseEntity.ok(orderService.getAllOrdersBetweenDates(ordersInRangeRequest.getStartTime(), ordersInRangeRequest.getEndTime()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> get(@PathVariable Long orderId) {
        return orderService.getOderForId(orderId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping("/price/recalculate/{orderId}")
    public ResponseEntity<Float> recalculate(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.recalculateOrderAmount(orderId));

    }

    private boolean CheckIfAllProductsValid(Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return true;
        }
        return productIds.stream().map(productService::getLatestVersionNumber).anyMatch(count -> count == 0);
    }
}

