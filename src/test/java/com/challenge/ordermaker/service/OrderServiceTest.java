package com.challenge.ordermaker.service;

import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.api.v1.response.Order;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.dao.OrderDao;
import com.challenge.ordermaker.dao.OrderProductDao;
import com.challenge.ordermaker.repo.OrderProductRepository;
import com.challenge.ordermaker.repo.OrderRepository;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderProductRepository orderProductRepository = mock(OrderProductRepository.class);
    private final ProductService productService = mock(ProductService.class);
    private final OrderService orderService = new OrderService(productService, orderRepository, orderProductRepository);

    @Test
    public void mustReturnAFullOrderByQueryingVariousTables() {

        HashSet<Long> productIds = new HashSet<>(Arrays.asList(1L, 2L, 3L));
        LocalDateTime now = LocalDateTime.now();
        OrderCreateRequest request = new OrderCreateRequest("test@mail.com", productIds, now);

        when(orderRepository.save(request))
                .thenReturn(new OrderDao(1, "Test@Test.com", Timestamp.valueOf(now)));


        OrderProductDao orderProductDao1 = new OrderProductDao(1, 20, 1);
        OrderProductDao orderProductDao2 = new OrderProductDao(2, 30, 2);
        OrderProductDao orderProductDao3 = new OrderProductDao(3, 40, 5);
        Set<OrderProductDao> orderProducts = new HashSet<>();
        orderProducts.add(orderProductDao1);
        orderProducts.add(orderProductDao2);
        orderProducts.add(orderProductDao3);
        when(orderProductRepository.getAllForOrderId(1)).thenReturn(orderProducts);


        Product product1 = new Product(20, "name", 23.0f);
        Product product2 = new Product(30, "name", 100.5f);
        Product product3 = new Product(40, "name", 200.5f);
        when(productService.getProductForVersion(20, 1)).thenReturn(Optional.of(product1));
        when(productService.getProductForVersion(30, 2)).thenReturn(Optional.of(product2));
        when(productService.getProductForVersion(40, 5)).thenReturn(Optional.of(product3));
        Set<Product> products = new HashSet<>();
        products.add(product1);
        products.add(product2);
        products.add(product3);

        Order order = orderService.placeOrder(request);
        Assert.assertEquals(order.getOrderId(), 1);
        Assert.assertEquals(order.getBuyerEmailId(), "Test@Test.com");
        Assert.assertEquals(order.getOrderTime(), now);
        Assert.assertEquals(order.getOrderCost(), 324f, 0);
        assertThat(order.getProducts(), containsInAnyOrder(product1, product2, product3));
    }


    @Test
    public void mustRecalculateTheAccountAmountUsingNewPrice() {
        long orderId = 999L;
        LocalDateTime now = LocalDateTime.now();

        when(orderRepository.get(orderId))
                .thenReturn(new OrderDao(orderId, "Test@Test.com", Timestamp.valueOf(now)));


        OrderProductDao orderProductDao1 = new OrderProductDao(1, 20, 2);
        OrderProductDao orderProductDao2 = new OrderProductDao(2, 30, 2);
        OrderProductDao orderProductDao3 = new OrderProductDao(3, 40, 2);
        Set<OrderProductDao> orderProducts = new HashSet<>();
        orderProducts.add(orderProductDao1);
        orderProducts.add(orderProductDao2);
        orderProducts.add(orderProductDao3);
        when(orderProductRepository.getAllForOrderId(orderId)).thenReturn(orderProducts);


        Product product4 = new Product(20, "name1", 23.0f);
        Product product5 = new Product(30, "name2", 100.5f);
        Product product6 = new Product(40, "name3", 200.5f);

        Product product1 = new Product(20, "name1", 200);
        Product product2 = new Product(30, "name2", 200.5f);
        Product product3 = new Product(40, "name2", 200.5f);

        when(productService.getActiveProductForId(20)).thenReturn(Optional.of(product1));
        when(productService.getActiveProductForId(30)).thenReturn(Optional.of(product2));
        when(productService.getActiveProductForId(40)).thenReturn(Optional.of(product3));

        when(productService.getProductForVersion(20, 2)).thenReturn(Optional.of(product4));
        when(productService.getProductForVersion(30, 2)).thenReturn(Optional.of(product5));
        when(productService.getProductForVersion(40, 2)).thenReturn(Optional.of(product6));

        Float amount = orderService.recalculateOrderAmount(orderId);
        Assert.assertEquals(amount, 601, 0);
    }


    @Test
    public void updateInProductPriceMustNotAffectPlacedOrder() {
        long orderId = 1000L;
        LocalDateTime now = LocalDateTime.now();
        when(orderRepository.get(orderId))
                .thenReturn(new OrderDao(orderId, "Test@Test.com", Timestamp.valueOf(now)));


        OrderProductDao orderProductDao1 = new OrderProductDao(1, 20, 2);
        OrderProductDao orderProductDao2 = new OrderProductDao(2, 30, 2);
        OrderProductDao orderProductDao3 = new OrderProductDao(3, 40, 2);
        Set<OrderProductDao> orderProducts = new HashSet<>();
        orderProducts.add(orderProductDao1);
        orderProducts.add(orderProductDao2);
        orderProducts.add(orderProductDao3);
        when(orderProductRepository.getAllForOrderId(orderId)).thenReturn(orderProducts);


        Product product4 = new Product(20, "name1", 23.0f);
        Product product5 = new Product(30, "name2", 100.5f);
        Product product6 = new Product(40, "name3", 200.5f);

        Product product1 = new Product(20, "name1", 200);
        Product product2 = new Product(30, "name2", 200.5f);
        Product product3 = new Product(40, "name2", 200.5f);

        when(productService.getActiveProductForId(20)).thenReturn(Optional.of(product1));
        when(productService.getActiveProductForId(30)).thenReturn(Optional.of(product2));
        when(productService.getActiveProductForId(40)).thenReturn(Optional.of(product3));

        when(productService.getProductForVersion(20, 2)).thenReturn(Optional.of(product4));
        when(productService.getProductForVersion(30, 2)).thenReturn(Optional.of(product5));
        when(productService.getProductForVersion(40, 2)).thenReturn(Optional.of(product6));

        Order order = orderService.getOderForId(orderId).get();
        Assert.assertEquals(order.getOrderId(), orderId);
        Assert.assertEquals(order.getBuyerEmailId(), "Test@Test.com");
        Assert.assertEquals(order.getOrderTime(), now);
        Assert.assertEquals(order.getOrderCost(), 324f, 0);
        assertThat(order.getProducts(), containsInAnyOrder(product4, product5, product6));
        assertThat(order.getProducts(), not(containsInAnyOrder(product1, product2, product3)));
    }


    @Test
    public void newOrderShouldUseLatestProductPrice() {
        long orderId = 2000L;
        LocalDateTime now = LocalDateTime.now();

        when(orderRepository.get(orderId))
                .thenReturn(new OrderDao(orderId, "Test@Test.com", Timestamp.valueOf(now)));


        OrderProductDao orderProductDao1 = new OrderProductDao(1, 20, 2);
        OrderProductDao orderProductDao2 = new OrderProductDao(2, 30, 2);
        OrderProductDao orderProductDao3 = new OrderProductDao(3, 40, 2);
        Set<OrderProductDao> orderProducts = new HashSet<>();
        orderProducts.add(orderProductDao1);
        orderProducts.add(orderProductDao2);
        orderProducts.add(orderProductDao3);
        when(orderProductRepository.getAllForOrderId(orderId)).thenReturn(orderProducts);


        Product product4 = new Product(20, "name1", 23.0f);
        Product product5 = new Product(30, "name2", 100.5f);
        Product product6 = new Product(40, "name3", 200.5f);

        Product product1 = new Product(20, "name1", 200);
        Product product2 = new Product(30, "name2", 200.5f);
        Product product3 = new Product(40, "name2", 200.5f);

        when(productService.getActiveProductForId(20)).thenReturn(Optional.of(product1));
        when(productService.getActiveProductForId(30)).thenReturn(Optional.of(product2));
        when(productService.getActiveProductForId(40)).thenReturn(Optional.of(product3));

        when(productService.getProductForVersion(20, 2)).thenReturn(Optional.of(product4));
        when(productService.getProductForVersion(30, 2)).thenReturn(Optional.of(product5));
        when(productService.getProductForVersion(40, 2)).thenReturn(Optional.of(product6));

        Order order = orderService.getOderForId(orderId).get();
        Assert.assertEquals(order.getOrderId(), orderId);
        Assert.assertEquals(order.getBuyerEmailId(), "Test@Test.com");
        Assert.assertEquals(order.getOrderTime(), now);
        Assert.assertEquals(order.getOrderCost(), 324f, 0);
        assertThat(order.getProducts(), containsInAnyOrder(product4, product5, product6));
        assertThat(order.getProducts(), not(containsInAnyOrder(product1, product2, product3)));
    }
}