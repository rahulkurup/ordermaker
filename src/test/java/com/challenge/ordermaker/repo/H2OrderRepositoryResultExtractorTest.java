package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.dao.OrderDao;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.Timestamp;

import static junit.framework.Assert.assertEquals;

public class H2OrderRepositoryResultExtractorTest {

    @Test
    public void emptyResults() throws Exception {
        OrderDao dao = ((ResultSetExtractor<OrderDao>) rs -> {
            if (rs.next()) {
                OrderDao dao1 = new OrderDao(rs.getLong("orderId"), rs.getString("buyerEmailId"), rs.getTimestamp("orderTime"));
                return dao1;
            } else
                return null;
        }).extractData(getEmptyMockResultSet());
        Assert.assertNull(dao);
    }

    @Test
    public void dataExists() throws Exception {
        OrderDao dao = ((ResultSetExtractor<OrderDao>) rs -> {
            if (rs.next()) {
                OrderDao dao1 = new OrderDao(rs.getLong("orderId"), rs.getString("buyerEmailId"), rs.getTimestamp("orderTime"));
                return dao1;
            } else
                return null;
        }).extractData(getMockResultSet());

        assertEquals(dao.getOrderId(), 1);
        assertEquals(dao.getBuyerEmailId(), "test@test.com");
        assertEquals(dao.getOrderTime(), new Timestamp(1));
    }


    private ResultSet getMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rs.getLong("orderId")).thenReturn(1L);
        Mockito.when(rs.getString("buyerEmailId")).thenReturn("test@test.com");
        Mockito.when(rs.getTimestamp("orderTime")).thenReturn(new Timestamp(1));
        return rs;
    }

    private ResultSet getEmptyMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(false);
        return rs;
    }
}