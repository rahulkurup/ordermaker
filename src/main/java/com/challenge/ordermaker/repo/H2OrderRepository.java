package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.api.v1.request.OrderCreateRequest;
import com.challenge.ordermaker.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Repository
public class H2OrderRepository implements OrderRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;


    @Override
    public Set<OrderDao> retrieveAllBetween(LocalDateTime startTime, LocalDateTime endTime) {
        Set<OrderDao> results = new HashSet<>();
        jdbcTemplate.query("SELECT orderId, buyerEmailId, orderTime FROM ORDERS where orderTime >= ? and orderTime <= ?",
                resultSet -> {
                    long orderId = resultSet.getLong("orderId");
                    results.add(
                            new OrderDao(
                                    orderId,
                                    resultSet.getString("buyerEmailId"),
                                    resultSet.getTimestamp("orderTime")
                            ));
                }, Timestamp.valueOf(startTime), Timestamp.valueOf(endTime));

        return results;
    }

    @Override
    public OrderDao get(long orderId) {
        return jdbcTemplate.queryForObject(
                "SELECT orderId, buyerEmailId, orderTime FROM ORDERS where orderId = ?",
                (resultSet, i) -> new OrderDao(
                        orderId,
                        resultSet.getString("buyerEmailId"),
                        resultSet.getTimestamp("orderTime")
                ), orderId);
    }


    @Transactional   /* Make sure that the write to two tables are atomic */
    @Override
    public OrderDao save(OrderCreateRequest request) {
        long id = jdbcTemplate.queryForObject("SELECT SQ_ORDER_ID.nextval from dual;", Long.class);

        OrderDao orderDao = new OrderDao(id, request.getBuyerEmailId(), Timestamp.valueOf(request.getOrderTime()));

        jdbcTemplate.update("insert into ORDERS (orderId, buyerEmailId, orderTime)  VALUES (?, ?, ?)",
                orderDao.getOrderId(), orderDao.getBuyerEmailId(), orderDao.getOrderTime());

        updateProductListInAssociationTable(request, orderDao);

        return orderDao;
    }

    private void updateProductListInAssociationTable(OrderCreateRequest request, OrderDao orderDao) {
        request.getProductSet().forEach(productId ->
                jdbcTemplate.update("insert into ORDERS_PRODUCTS (orderId, productId, version)  VALUES (?, ?, ?)",
                        orderDao.getOrderId(),
                        productId,
                        lockProductAndGetLatestVersionNumber(productId)));
    }

    private int lockProductAndGetLatestVersionNumber(long productId) {
        /* Get the latest version of product when creating order,
           This need to be done using a row level locking to avoid a race condition.
           ie, some other transaction change the version after we read it and before we insert into junction table
        */
        productRepository.lockProduct(productId);
        return productRepository.getLatestVersionNumber(productId);
    }
}