package com.ecommerce.dto;

import com.ecommerce.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryDto(
        Long id,
        String name,
        String description,
        String imageUrl,
        Long parentId,
        String parentName,
        boolean active,
        List<CategoryDto> subcategories,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryDto fromEntity(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.isActive(),
                null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public static CategoryDto fromEntityWithSubcategories(Category category) {
        List<CategoryDto> subcategoryDtos = category.getSubcategories() != null
                ? category.getSubcategories().stream()
                    .map(CategoryDto::fromEntity)
                    .toList()
                : null;

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.isActive(),
                subcategoryDtos,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public record CreateRequest(
            @NotBlank @Size(min = 2, max = 100) String name,
            @Size(max = 500) String description,
            String imageUrl,
            Long parentId
    ) {
        public Category toEntity() {
            Category category = new Category(name, description);
            category.setImageUrl(imageUrl);
            return category;
        }
    }

    public record UpdateRequest(
            @Size(min = 2, max = 100) String name,
            @Size(max = 500) String description,
            String imageUrl,
            Long parentId,
            Boolean active
    ) {}
}
