package com.challenge.ordermaker.service;

import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.api.v1.response.Order;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.dao.OrderDao;
import com.challenge.ordermaker.dao.OrderProductDao;
import com.challenge.ordermaker.error.ResourceNotFoundException;
import com.challenge.ordermaker.repo.OrderProductRepository;
import com.challenge.ordermaker.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private final ProductService productService;

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final OrderProductRepository orderProductRepository;


    public Set<Order> getAllOrdersBetweenDates(LocalDateTime startTime, LocalDateTime endTime) {

        Supplier<TreeSet<Order>> supplier = () -> new TreeSet<>(Comparator.comparingLong(Order::getOrderId));

        return orderRepository.retrieveAllBetween(startTime, endTime).stream().map(
                this::mapDaoToOrder).collect(Collectors.toCollection(supplier));
    }

    public Optional<Order> getOderForId(long orderId) {
        try {
            return Optional.of(mapDaoToOrder(orderRepository.get(orderId)));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public float recalculateOrderAmount(long orderId) {
        Optional<Order> order = getOderForId(orderId);

        if (order.isPresent()) {
            Set<Product> productsWithLatestPriceForOrder = getProductsWithLatestPriceForOrder(orderId);
            return getTotalOrderPrice(productsWithLatestPriceForOrder);
        } else {
            throw new ResourceNotFoundException("No order exist");
        }
    }

    public Order placeOrder(OrderCreateRequest request) {
        return mapDaoToOrder(orderRepository.save(request));
    }

    private float getTotalOrderPrice(Set<Product> productsForOrder) {
        return productsForOrder.stream().map(Product::getPrice).reduce(0f, Float::sum);
    }

    private Set<Product> getProductsForOrder(long orderId) {
        Supplier<TreeSet<Product>> supplier = () -> new TreeSet<>(Comparator.comparingLong(Product::getProductId));


        Set<OrderProductDao> orderProducts = orderProductRepository.getAllForOrderId(orderId);

        return orderProducts.stream()
                .map(OrderProductDao ->
                        productService.getProductForVersion(OrderProductDao.getProductId(), OrderProductDao.getVersion()).get())
                .collect(Collectors.toCollection(supplier));
    }

    private Set<Product> getProductsWithLatestPriceForOrder(long orderId) {
        Set<OrderProductDao> orderProducts = orderProductRepository.getAllForOrderId(orderId);

        return orderProducts.stream()
                .map(OrderProductDao ->
                        productService.getActiveProductForId(OrderProductDao.getProductId()).get())
                .collect(Collectors.toSet());
    }

    private Order mapDaoToOrder(OrderDao dao) {
        Set<Product> productsForOrder = getProductsForOrder(dao.getOrderId());
        return new Order(dao.getOrderId(),
                dao.getBuyerEmailId(),
                dao.getOrderTime().toLocalDateTime(),
                productsForOrder,
                getTotalOrderPrice(productsForOrder));
    }
}
