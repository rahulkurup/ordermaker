package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.dao.ProductDao;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class H2ProductRepositoryResultExtractorTest {

    @Test
    public void emptyResults() throws Exception {
        ProductDao dao = ((ResultSetExtractor<ProductDao>) rs -> {
            if (rs.next()) {
                ProductDao dao1 = new ProductDao(
                        rs.getLong("productId"), rs.getString("name"),
                        rs.getFloat("price"),
                        rs.getInt("version"),
                        rs.getBoolean("latest"));
                return dao1;
            } else
                return null;
        }).extractData(getEmptyMockResultSet());
        Assert.assertNull(dao);
    }

    @Test
    public void dataExists() throws Exception {
        ProductDao dao = ((ResultSetExtractor<ProductDao>) rs -> {
            if (rs.next()) {
                ProductDao dao1 = new ProductDao(
                        rs.getLong("productId"), rs.getString("name"),
                        rs.getFloat("price"),
                        rs.getInt("version"),
                        rs.getBoolean("latest"));
                return dao1;
            } else
                return null;
        }).extractData(getMockResultSet());

        assertEquals(dao.getName(), "test");
        assertEquals(dao.getPrice(), 23.5f, 0);
        assertEquals(dao.getProductId(), 1L);
        assertEquals(dao.getVersion(), 3);
        assertTrue(dao.isLatest());
    }


    private ResultSet getMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(true).thenReturn(false);
        Mockito.when(rs.getLong("productId")).thenReturn(1L);
        Mockito.when(rs.getString("name")).thenReturn("test");
        Mockito.when(rs.getFloat("price")).thenReturn(23.5f);
        Mockito.when(rs.getInt("version")).thenReturn(3);
        Mockito.when(rs.getBoolean("latest")).thenReturn(true);
        return rs;
    }

    private ResultSet getEmptyMockResultSet() throws Exception {
        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.when(rs.next()).thenReturn(false);
        return rs;
    }

}