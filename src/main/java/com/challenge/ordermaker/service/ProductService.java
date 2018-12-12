package com.challenge.ordermaker.service;

import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.request.ProductUpdateRequest;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.dao.ProductDao;
import com.challenge.ordermaker.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;

    public Product createProduct(ProductCreateRequest request) {
        return mapDaoToProduct(productRepository.save(request));
    }

    public Set<Product> getAllActiveProducts() {
        Supplier<TreeSet<Product>> supplier = () -> new TreeSet<>(Comparator.comparingLong(Product::getProductId));

        return productRepository.getAllActive().stream()
                .map(this::mapDaoToProduct).collect(Collectors.toCollection(supplier));
    }

    public Optional<Product> getActiveProductForId(long productId) {
        try {
            return Optional.of(mapDaoToProduct(productRepository.getActiveForId(productId)));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Product updateProduct(ProductUpdateRequest productUpdateRequest) {
        return mapDaoToProduct(productRepository.update(productUpdateRequest));
    }

    public int getLatestVersionNumber(long productId) {
        return productRepository.getLatestVersionNumber(productId);
    }

    Optional<Product> getProductForVersion(long productId, int version) {
        try {
            return Optional.of(mapDaoToProduct(productRepository.getForVersion(productId, version)));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    private Product mapDaoToProduct(ProductDao dao) {
        return new Product(dao.getProductId(), dao.getName(), dao.getPrice());
    }
}
