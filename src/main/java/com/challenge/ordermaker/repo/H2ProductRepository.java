package com.challenge.ordermaker.repo;

import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.request.ProductUpdateRequest;
import com.challenge.ordermaker.dao.ProductDao;
import com.challenge.ordermaker.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@Repository
public class H2ProductRepository implements ProductRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Set<ProductDao> getAllActive() {
        Set<ProductDao> results = new HashSet<>();
        jdbcTemplate.query("SELECT productId, name, price, version, latest FROM ACTIVE_PRODUCTS",
                resultSet -> {
                    results.add(new ProductDao(
                            resultSet.getLong("productId"),
                            resultSet.getString("name"),
                            resultSet.getFloat("price"),
                            resultSet.getInt("version"),
                            resultSet.getBoolean("latest")
                    ));
                });

        return results;
    }

    @Override
    public ProductDao getActiveForId(long productId) {
        return jdbcTemplate.queryForObject(
                "SELECT productId, name, price, version, latest FROM ACTIVE_PRODUCTS where productId = ?",
                (resultSet, i) -> new ProductDao(
                        resultSet.getLong("productId"),
                        resultSet.getString("name"),
                        resultSet.getFloat("price"),
                        resultSet.getInt("version"),
                        resultSet.getBoolean("latest")
                ), productId);
    }

    @Override
    public ProductDao getForVersion(long productId, int version) {
        return jdbcTemplate.queryForObject(
                "SELECT productId, name, price, version, latest FROM PRODUCTS where version = ? and productId = ?",
                (resultSet, i) -> new ProductDao(
                        resultSet.getLong("productId"),
                        resultSet.getString("name"),
                        resultSet.getFloat("price"),
                        resultSet.getInt("version"),
                        resultSet.getBoolean("latest")
                ), version, productId);
    }

    @Override
    public ProductDao save(ProductCreateRequest request) {
        long id = jdbcTemplate.queryForObject("SELECT SQ_PRODUCT_ID.nextval from dual;", Long.class);

        /* do we need transaction ?  If update fails after fetching an, we loose an id, but it does not matter*/

        ProductDao productDao = new ProductDao(id, request.getName(), request.getPrice(), 1, true);

        jdbcTemplate.update("insert into PRODUCTS (productId, name, price, version, latest)  VALUES (?, ?, ?, ?, ?)",
                productDao.getProductId(), productDao.getName(), productDao.getPrice(), productDao.getVersion(), productDao.isLatest());

        return productDao;
    }

    @Transactional  // We need this to happen atomic
    @Override
    public ProductDao update(ProductUpdateRequest request) {
        long productId = request.getProductId();

        int version = getLatestVersionNumber(productId);

        if (version == 0) {
            throw new ResourceNotFoundException("productId not found");
        }
        jdbcTemplate.update("UPDATE PRODUCTS set latest='false' where version= ? and productId =?;", version, productId);

        ProductDao productDao = new ProductDao(productId, request.getName(), request.getPrice(), ++version, true);

        jdbcTemplate.update("insert into PRODUCTS (productId, name, price, version, latest)  VALUES (?, ?, ?, ?, ?)",
                productDao.getProductId(), productDao.getName(), productDao.getPrice(), productDao.getVersion(), productDao.isLatest());

        return productDao;
    }

    @Override
    public int getLatestVersionNumber(long productId) {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM PRODUCTS where productId = ?", Integer.class, productId);
    }

    @Override
    public void lockProduct(long productId) {
        jdbcTemplate.queryForObject(
                "SELECT productId, name, price, version, latest FROM PRODUCTS where latest = TRUE and productId = ? FOR UPDATE",
                (resultSet, i) -> new ProductDao(
                        resultSet.getLong("productId"),
                        resultSet.getString("name"),
                        resultSet.getFloat("price"),
                        resultSet.getInt("version"),
                        resultSet.getBoolean("latest")
                ), productId);
    }
}