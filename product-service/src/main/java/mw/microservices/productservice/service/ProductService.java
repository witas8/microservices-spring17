package mw.microservices.productservice.service;

import mw.microservices.productservice.dto.ProductRequest;
import mw.microservices.productservice.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mw.microservices.productservice.model.Product;
import org.springframework.stereotype.Service;
import mw.microservices.productservice.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product with ID {} is saved", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse
                .builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
