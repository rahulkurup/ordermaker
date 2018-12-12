package com.challenge.ordermaker;

import com.challenge.ordermaker.api.v1.OrderController;
import com.challenge.ordermaker.api.v1.ProductController;
import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.api.v1.request.OrdersInRangeRequest;
import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.request.ProductUpdateRequest;
import com.challenge.ordermaker.api.v1.response.Order;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.error.ResourceNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
//@WebAppConfiguration
@ActiveProfiles("test")
public class OrdermakerIntegrationTests {

    @LocalServerPort
    int randomServerPort;
    @Autowired
    private ProductController productController;
    @Autowired
    private OrderController orderController;

    @Test
    public void testTheEntireFlow() {

        /* should throw not found for non existing product */
        ResponseEntity<Product> productResponseEntity = productController.get(999L);
        assertEquals(productResponseEntity.getStatusCode(), HttpStatus.NOT_FOUND);

        /* should throw not found for non existing order */
        ResponseEntity<Order> orderResponseEntity = orderController.get(999L);
        assertEquals(orderResponseEntity.getStatusCode(), HttpStatus.NOT_FOUND);

        /* should return empty set when there are no products */
        ResponseEntity<Set<Product>> allProducts = productController.getAll();
        assertEquals(allProducts.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(allProducts.getBody()).size(), 0);

        /* should return empty set when there are no orders */
        OrdersInRangeRequest ordersInRangeRequest = new OrdersInRangeRequest(LocalDateTime.now().minus(5, ChronoUnit.HOURS), LocalDateTime.now());
        ResponseEntity<Set<Order>> allOrdersInTime = orderController.getAll(ordersInRangeRequest);
        assertEquals(allOrdersInTime.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(allProducts.getBody()).size(), 0);


        /* should throw exception when try to update a non existent product*/
        ProductUpdateRequest productUpdateRequest = new ProductUpdateRequest(999, "new Value", 25f);
        Exception ex = null;
        try {
            productController.update(productUpdateRequest);
        } catch (ResourceNotFoundException e) {
            ex = e;
        }
        assertNotNull(ex);

        /* should throw exception when try to recalculate a non existent order*/
        Exception ex2 = null;
        try {
            assertEquals(orderController.recalculate(999L).getStatusCode(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            ex2 = e;
        }
        assertNotNull(ex2);


        /* The OrderCreation should fail if product ids are null or is empty */
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest("test@test.com", null, LocalDateTime.now());
        assertEquals(orderController.place(orderCreateRequest).getStatusCode(), HttpStatus.BAD_REQUEST);

        OrderCreateRequest orderCreateRequestWithEmptySet = new OrderCreateRequest("test@test.com", new HashSet<>(), LocalDateTime.now());
        assertEquals(orderController.place(orderCreateRequestWithEmptySet).getStatusCode(), HttpStatus.BAD_REQUEST);

        /* The OrderCreation should fail if product ids are not valid. We validate this*/
        OrderCreateRequest orderCreateRequestWithIds = new OrderCreateRequest("test@test.com", new HashSet<>(Arrays.asList(1L)), LocalDateTime.now());
        assertEquals(orderController.place(orderCreateRequestWithIds).getStatusCode(), HttpStatus.BAD_REQUEST);


        /* Create 10 products */
        for (int i = 1; i <= 10; i++) {
            /* Create a products*/
            ProductCreateRequest productCreateRequest = new ProductCreateRequest("Name" + "_" + i, i + 0.5f);
            ResponseEntity<Product> createResponse = productController.createProduct(productCreateRequest);
            assertEquals(createResponse.getStatusCode(), HttpStatus.OK);
        }

        /* product getAll must return now all 10 products */
        ResponseEntity<Set<Product>> allProductsResponse = productController.getAll();
        assertEquals(allProducts.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(allProductsResponse.getBody()).size(), 10);

        /* product get must return the correct corresponding product */
        for (int i = 1; i <= 10; i++) {
            ResponseEntity<Product> productGetResponse = productController.get((long) i);
            assertEquals(productGetResponse.getStatusCode(), HttpStatus.OK);
            Product product = productGetResponse.getBody();
            assertEquals(product.getName(), "Name" + "_" + i);
            assertEquals(product.getProductId(), i);
            assertEquals(product.getPrice(), i + 0.5, 0);
        }

        /* Create a new Order with products from 1 to 5 */
        HashSet<Long> productIds = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        LocalDateTime now = LocalDateTime.now();
        OrderCreateRequest orderCreateRequestValid = new OrderCreateRequest("test@test.com", productIds, now);
        ResponseEntity<Order> orderCreatedResponse = orderController.place(orderCreateRequestValid);

        /* The order created and give an order cost by summing all product costs and get correct order details */
        assertEquals(orderCreatedResponse.getStatusCode(), HttpStatus.OK);
        Order order = orderCreatedResponse.getBody();
        assertEquals(order.getOrderId(), 1);
        assertEquals(order.getOrderTime(), now);
        assertEquals(order.getBuyerEmailId(), "test@test.com");
        assertEquals(order.getOrderCost(), 1.5 + 2.5 + 3.5 + 4.5 + 5.5, 0);

        /* The order created must find the correct products based on Ids in sorted form*/
        Iterator<Product> iterator = order.getProducts().iterator();
        int i = 1;
        while (iterator.hasNext()) {
            Product product = iterator.next();
            assertEquals(product.getName(), "Name" + "_" + i);
            assertEquals(product.getProductId(), i);
            assertEquals(product.getPrice(), i + 0.5, 0);
            i++;
        }


        /* The order get must find the order based on id */
        ResponseEntity<Order> orderResponseEntityValid = orderController.get(1L);
        assertEquals(orderResponseEntityValid.getStatusCode(), HttpStatus.OK);
        assertEquals(orderResponseEntityValid.getBody().getOrderId(), 1);

        /* We try recalculate the order cost. But no product was updated. So recalculation returns the same cost as before*/
        assertEquals(orderController.recalculate(1L).getStatusCode(), HttpStatus.OK);
        assertEquals(orderController.recalculate(1L).getBody(), 1.5 + 2.5 + 3.5 + 4.5 + 5.5, 0);


        /* update the product 1 to have a price 1000 */
        ProductUpdateRequest productUpdateRequestValid = new ProductUpdateRequest(1, "new Value", 1000);
        assertEquals(productController.update(productUpdateRequestValid).getStatusCode(), HttpStatus.OK);
        ResponseEntity<Order> orderResponseAfterPriceUpdate = orderController.get(1L);
        assertEquals(orderResponseEntityValid.getStatusCode(), HttpStatus.OK);
        Order orderAfterProductUpdate = orderResponseAfterPriceUpdate.getBody();

        /* update of the product should not affect the already placed order*/
        assertEquals(orderAfterProductUpdate.getOrderId(), 1);
        assertEquals(orderAfterProductUpdate.getOrderTime(), now);
        assertEquals(orderAfterProductUpdate.getBuyerEmailId(), "test@test.com");
        assertEquals(orderAfterProductUpdate.getOrderCost(), 1.5 + 2.5 + 3.5 + 4.5 + 5.5, 0);
        assertEquals(orderResponseEntityValid.getBody().getOrderId(), 1);


        /* Product Get returns the new latest product price*/
        ResponseEntity<Product> productGetResponse = productController.get(1L);
        assertEquals(productGetResponse.getStatusCode(), HttpStatus.OK);
        Product product = productGetResponse.getBody();
        assertEquals(product.getName(), "new Value");
        assertEquals(product.getProductId(), 1);
        assertEquals(product.getPrice(), 1000, 0);

        /* We try recalculate the order cost. one product was updated. So recalculation returns the order cost based on new price*/
        assertEquals(orderController.recalculate(1L).getStatusCode(), HttpStatus.OK);
        assertEquals(orderController.recalculate(1L).getBody(), 1000 + 2.5 + 3.5 + 4.5 + 5.5, 0);

        /* All orders created after the product update must have the new price*/
        HashSet<Long> productIdsIncludingUpdatedOne = new HashSet<>(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        OrderCreateRequest orderCreateRequestValidAfterProductUpdate = new OrderCreateRequest("test@test.com", productIdsIncludingUpdatedOne, now);
        ResponseEntity<Order> orderCreatedResponseAfterProductUpdate = orderController.place(orderCreateRequestValidAfterProductUpdate);

        /* The order created must find the correct products based on Ids in sorted form and give the updated price for updated product*/
        assertEquals(orderCreatedResponseAfterProductUpdate.getStatusCode(), HttpStatus.OK);
        Order orderCreatedAfterUpdate = orderCreatedResponseAfterProductUpdate.getBody();
        assertEquals(orderCreatedAfterUpdate.getOrderId(), 2L);
        assertEquals(orderCreatedAfterUpdate.getOrderTime(), now);
        assertEquals(orderCreatedAfterUpdate.getBuyerEmailId(), "test@test.com");
        assertEquals(orderCreatedAfterUpdate.getOrderCost(), 1000 + 2.5 + 3.5 + 4.5 + 5.5, 0);

        /* The order created must find the correct products based on Ids in sorted form and give the updated price for updated product*/
        Iterator<Product> iteratorNew = orderCreatedAfterUpdate.getProducts().iterator();
        int index = 1;
        while (iteratorNew.hasNext()) {
            Product productInNewOrder = iteratorNew.next();
            if (index == 1) {
                assertEquals(productInNewOrder.getName(), "new Value");
                assertEquals(productInNewOrder.getProductId(), index);
                assertEquals(productInNewOrder.getPrice(), 1000, 0);
            } else {
                assertEquals(productInNewOrder.getName(), "Name" + "_" + index);
                assertEquals(productInNewOrder.getProductId(), index);
                assertEquals(productInNewOrder.getPrice(), index + 0.5, 0);
            }
            index++;
        }
    }
}
