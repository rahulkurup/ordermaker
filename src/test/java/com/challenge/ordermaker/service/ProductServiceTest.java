package com.challenge.ordermaker.service;

import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.dao.ProductDao;
import com.challenge.ordermaker.repo.ProductRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProductServiceTest {

    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final ProductService productService = new ProductService(productRepository);

    @Test
    public void mustCreateProduct() {
        ProductCreateRequest productCreateRequest = new ProductCreateRequest("Test", 23f);
        when(productRepository.save(productCreateRequest))
                .thenReturn(new ProductDao(1, "Test", 23f, 1, true));

        Product product = productService.createProduct(productCreateRequest);
        Assert.assertEquals(product.getName(), "Test");
        Assert.assertEquals(product.getProductId(), 1);
        Assert.assertEquals(product.getPrice(), 23f, 0);
    }

    @Test
    public void mustGetAllProducts() {
        when(productRepository.getAllActive())
                .thenReturn(new HashSet<>(Arrays.asList(
                        new ProductDao(1, "Test", 23f, 1, true),
                        new ProductDao(2, "Test", 25f, 1, true))
                ));

        Set<Product> allActiveProducts = productService.getAllActiveProducts();
        Assert.assertThat(allActiveProducts, hasSize(2));
    }

    @Test
    public void getActiveProductForId() {
        long id = 999L;
        when(productRepository.getActiveForId(id))
                .thenReturn(new ProductDao(1, "Test", 23f, 1, true));
        Assert.assertEquals(productService.getActiveProductForId(id).get().getName(), "Test");
        Assert.assertEquals(productService.getActiveProductForId(id).get().getProductId(), 1);
        Assert.assertEquals(productService.getActiveProductForId(id).get().getPrice(), 23f, 0);
    }

    @Test
    public void mustConvertGetToOptionalIfNoRecordsFound() {
        long id = 999L;
        when(productRepository.getActiveForId(id)).thenThrow(new EmptyResultDataAccessException(5));
        Assert.assertFalse(productService.getActiveProductForId(id).isPresent());

        long id2 = 999L;
        int version = 5;
        when(productRepository.getForVersion(id2, version))
                .thenThrow(new EmptyResultDataAccessException(5));
        Assert.assertFalse(productService.getProductForVersion(id2, version).isPresent());
    }

    @Test
    public void mustProvideLatestVersionNumber() {
        long id = 999L;
        when(productRepository.getLatestVersionNumber(id)).thenReturn(2);
        Assert.assertEquals(productService.getLatestVersionNumber(id), 2);
    }

    @Test
    public void getProductForVersion() {
        long id = 999L;
        int version = 5;
        when(productRepository.getForVersion(id, version))
                .thenReturn(new ProductDao(1, "Test", 23f, 1, true));

        Product product = productService.getProductForVersion(id, version).get();
        Assert.assertEquals(product.getName(), "Test");
        Assert.assertEquals(product.getProductId(), 1);
        Assert.assertEquals(product.getPrice(), 23f, 0);
    }
}