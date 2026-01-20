package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Create admin user
            if (!userRepository.existsByEmail("admin@ecommerce.com")) {
                User admin = new User("admin", "admin@ecommerce.com", passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                admin.setFirstName("Admin");
                admin.setLastName("User");
                userRepository.save(admin);
            }

            // Create seller user
            if (!userRepository.existsByEmail("seller@ecommerce.com")) {
                User seller = new User("seller", "seller@ecommerce.com", passwordEncoder.encode("seller123"));
                seller.setRole(User.Role.SELLER);
                seller.setFirstName("Seller");
                seller.setLastName("User");
                userRepository.save(seller);
            }

            // Create customer user
            if (!userRepository.existsByEmail("customer@ecommerce.com")) {
                User customer = new User("customer", "customer@ecommerce.com", passwordEncoder.encode("customer123"));
                customer.setRole(User.Role.CUSTOMER);
                customer.setFirstName("Customer");
                customer.setLastName("User");
                userRepository.save(customer);
            }

            // Create categories
            Category electronics = categoryRepository.findByName("Electronics")
                    .orElseGet(() -> {
                        Category cat = new Category("Electronics", "Electronic devices and accessories");
                        return categoryRepository.save(cat);
                    });

            Category clothing = categoryRepository.findByName("Clothing")
                    .orElseGet(() -> {
                        Category cat = new Category("Clothing", "Fashion and apparel");
                        return categoryRepository.save(cat);
                    });

            Category books = categoryRepository.findByName("Books")
                    .orElseGet(() -> {
                        Category cat = new Category("Books", "Books and literature");
                        return categoryRepository.save(cat);
                    });

            // Create subcategories
            if (!categoryRepository.existsByName("Smartphones")) {
                Category smartphones = new Category("Smartphones", "Mobile phones and accessories");
                smartphones.setParent(electronics);
                categoryRepository.save(smartphones);
            }

            if (!categoryRepository.existsByName("Laptops")) {
                Category laptops = new Category("Laptops", "Laptop computers");
                laptops.setParent(electronics);
                categoryRepository.save(laptops);
            }

            // Create sample products
            if (!productRepository.existsBySku("PHONE-001")) {
                Product phone = new Product("Smartphone Pro X",
                        "Latest flagship smartphone with amazing features",
                        new BigDecimal("999.99"), 50);
                phone.setSku("PHONE-001");
                phone.setCategory(electronics);
                phone.setFeatured(true);
                productRepository.save(phone);
            }

            if (!productRepository.existsBySku("LAPTOP-001")) {
                Product laptop = new Product("Business Laptop Pro",
                        "High-performance laptop for professionals",
                        new BigDecimal("1499.99"), 30);
                laptop.setSku("LAPTOP-001");
                laptop.setCategory(electronics);
                laptop.setFeatured(true);
                productRepository.save(laptop);
            }

            if (!productRepository.existsBySku("TSHIRT-001")) {
                Product tshirt = new Product("Classic Cotton T-Shirt",
                        "Comfortable cotton t-shirt for everyday wear",
                        new BigDecimal("29.99"), 100);
                tshirt.setSku("TSHIRT-001");
                tshirt.setCategory(clothing);
                productRepository.save(tshirt);
            }

            if (!productRepository.existsBySku("BOOK-001")) {
                Product book = new Product("Java Programming Guide",
                        "Comprehensive guide to Java programming",
                        new BigDecimal("49.99"), 75);
                book.setSku("BOOK-001");
                book.setCategory(books);
                book.setFeatured(true);
                productRepository.save(book);
            }

            System.out.println("Sample data initialized successfully!");
            System.out.println("Admin user: admin@ecommerce.com / admin123");
            System.out.println("Seller user: seller@ecommerce.com / seller123");
            System.out.println("Customer user: customer@ecommerce.com / customer123");
        };
    }
}
