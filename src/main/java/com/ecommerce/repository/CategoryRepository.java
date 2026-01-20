package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT c FROM Category c WHERE c.active = true")
    List<Category> findAllActive();

    @Query("SELECT c FROM Category c WHERE c.active = true AND c.parent IS NULL")
    List<Category> findActiveRootCategories();

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> searchByName(@Param("name") String name);
}
