package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.request.ProductUpdateRequest;
import com.challenge.ordermaker.dao.ProductDao;

import java.util.Set;

public interface ProductRepository {

    Set<ProductDao> getAllActive();

    ProductDao getActiveForId(long productId);

    ProductDao getForVersion(long productId, int version);

    ProductDao save(ProductCreateRequest product);

    ProductDao update(ProductUpdateRequest productUpdateRequest);

    int getLatestVersionNumber(long productId);

    void lockProduct(long productId);
}
