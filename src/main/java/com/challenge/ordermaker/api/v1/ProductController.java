package com.challenge.ordermaker.api.v1;

import com.challenge.ordermaker.api.v1.request.ProductCreateRequest;
import com.challenge.ordermaker.api.v1.request.ProductUpdateRequest;
import com.challenge.ordermaker.api.v1.response.Product;
import com.challenge.ordermaker.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@Valid ProductCreateRequest productCreateRequest) {
        return ResponseEntity.ok(productService.createProduct(productCreateRequest));
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Set<Product>> getAll() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> get(@PathVariable Long productId) {
        return productService.getActiveProductForId(productId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update}")
    public ResponseEntity<Product> update(ProductUpdateRequest productUpdateRequest) {
        return ResponseEntity.ok(productService.updateProduct(productUpdateRequest));
    }
}
