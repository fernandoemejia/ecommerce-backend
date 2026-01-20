package com.ecommerce.service;

import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.DuplicateResourceException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getActiveProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAllActive(pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        return productRepository.findById(id)
                .map(ProductDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Transactional(readOnly = true)
    public ProductDto getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(ProductDto::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryId(categoryId, pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getFeaturedProducts() {
        return productRepository.findFeaturedProducts().stream()
                .map(ProductDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getInStockProducts(Pageable pageable) {
        Page<Product> page = productRepository.findInStock(pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(ProductDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> searchProducts(String keyword, Pageable pageable) {
        Page<Product> page = productRepository.searchByKeyword(keyword, pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> page = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProductsByCategoryAndPriceRange(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<Product> page = productRepository.findByCategoryAndPriceRange(categoryId, minPrice, maxPrice, pageable);
        return PageResponse.from(page, ProductDto::fromEntity);
    }

    public ProductDto createProduct(ProductDto.CreateRequest request) {
        if (request.sku() != null && productRepository.existsBySku(request.sku())) {
            throw new DuplicateResourceException("Product", "sku", request.sku());
        }

        Product product = request.toEntity();

        if (request.categoryId() != null) {
            Category category = categoryService.getCategoryEntityById(request.categoryId());
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        return ProductDto.fromEntity(savedProduct);
    }

    public ProductDto updateProduct(Long id, ProductDto.UpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.sku() != null && !request.sku().equals(product.getSku())) {
            if (productRepository.existsBySku(request.sku())) {
                throw new DuplicateResourceException("Product", "sku", request.sku());
            }
            product.setSku(request.sku());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.additionalImages() != null) {
            product.setAdditionalImages(request.additionalImages());
        }
        if (request.categoryId() != null) {
            Category category = categoryService.getCategoryEntityById(request.categoryId());
            product.setCategory(category);
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }
        if (request.featured() != null) {
            product.setFeatured(request.featured());
        }
        if (request.discountPrice() != null) {
            product.setDiscountPrice(request.discountPrice());
        }

        Product updatedProduct = productRepository.save(product);
        return ProductDto.fromEntity(updatedProduct);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
    }

    public ProductDto updateStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);
        return ProductDto.fromEntity(updatedProduct);
    }

    public void reduceStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.reduceStock(quantity);
        productRepository.save(product);
    }

    public void increaseStock(Long id, int quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.increaseStock(quantity);
        productRepository.save(product);
    }
}
