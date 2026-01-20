package com.ecommerce.dto;

import com.ecommerce.entity.Product;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String sku,
        String imageUrl,
        List<String> additionalImages,
        Long categoryId,
        String categoryName,
        boolean active,
        boolean featured,
        BigDecimal rating,
        Integer reviewCount,
        BigDecimal discountPrice,
        BigDecimal effectivePrice,
        boolean inStock,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductDto fromEntity(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getSku(),
                product.getImageUrl(),
                product.getAdditionalImages(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.isActive(),
                product.isFeatured(),
                product.getRating(),
                product.getReviewCount(),
                product.getDiscountPrice(),
                product.getEffectivePrice(),
                product.isInStock(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public record CreateRequest(
            @NotBlank @Size(min = 2, max = 200) String name,
            @Size(max = 2000) String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Min(0) Integer stockQuantity,
            String sku,
            String imageUrl,
            List<String> additionalImages,
            Long categoryId,
            Boolean featured,
            BigDecimal discountPrice
    ) {
        public Product toEntity() {
            Product product = new Product(name, description, price, stockQuantity);
            product.setSku(sku);
            product.setImageUrl(imageUrl);
            if (additionalImages != null) {
                product.setAdditionalImages(additionalImages);
            }
            if (featured != null) {
                product.setFeatured(featured);
            }
            product.setDiscountPrice(discountPrice);
            return product;
        }
    }

    public record UpdateRequest(
            @Size(min = 2, max = 200) String name,
            @Size(max = 2000) String description,
            @DecimalMin("0.01") BigDecimal price,
            @Min(0) Integer stockQuantity,
            String sku,
            String imageUrl,
            List<String> additionalImages,
            Long categoryId,
            Boolean active,
            Boolean featured,
            BigDecimal discountPrice
    ) {}

    public record SearchCriteria(
            String keyword,
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Boolean featured
    ) {}
}
