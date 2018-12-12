package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.dao.OrderProductDao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class H2OrderProductRepository implements OrderProductRepository {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Set<OrderProductDao> getAllForOrderId(long orderId) {

        Set<OrderProductDao> results = new HashSet<>();
        jdbcTemplate.query("SELECT orderId, productId, version FROM ORDERS_PRODUCTS where orderId = ?",
                resultSet -> {
                    OrderProductDao dao = new OrderProductDao(resultSet.getLong("orderId"),
                            resultSet.getLong("productId"),
                            resultSet.getInt("version"));
                    results.add(dao);
                }, orderId);

        return results;
    }
}
