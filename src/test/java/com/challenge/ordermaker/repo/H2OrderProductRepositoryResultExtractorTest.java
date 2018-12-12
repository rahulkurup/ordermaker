package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.dao.OrderProductDao;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;

import static junit.framework.Assert.assertEquals;


public class H2OrderProductRepositoryResultExtractorTest {

    @Test
    public void emptyResults() throws Exception {
        OrderProductDao dao = ((ResultSetExtractor<OrderProductDao>) rs -> {
            if (rs.next()) {
                OrderProductDao dao1 = new OrderProductDao(rs.getLong("orderId"), rs.getLong("productId"), rs.getInt("version"));
                return dao1;
            } else
                return null;
        }).extractData(getEmptyMockResultSet());
        Assert.assertNull(dao);
    }

    @Test
    public void dataExists() throws Exception {
        OrderProductDao dao = ((ResultSetExtractor<OrderProductDao>) rs -> {
            if (rs.next()) {
                OrderProductDao dao1 = new OrderProductDao(rs.getLong("orderId"), rs.getLong("productId"), rs.getInt("version"));
                return dao1;
            } else
                return null;
        }).extractData(getMockResultSet());

        assertEquals(dao.getProductId(), 2);
        assertEquals(dao.getVersion(), 3);
        assertEquals(dao.getOrderId(), 1);

    }


    private ResultSet getMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rs.getLong("orderId")).thenReturn(1L);
        Mockito.when(rs.getLong("productId")).thenReturn(2L);
        Mockito.when(rs.getInt("version")).thenReturn(3);
        return rs;
    }

    private ResultSet getEmptyMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(false);
        return rs;
    }
}