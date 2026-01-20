package com.ecommerce.service;

import com.ecommerce.dto.CartDto;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final ProductService productService;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       UserService userService, ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userService = userService;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public CartDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCartForUser(userId));
        return CartDto.fromEntity(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCartEntityByUserId(Long userId) {
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    private Cart createCartForUser(Long userId) {
        User user = userService.getUserEntityById(userId);
        Cart cart = new Cart(user);
        return cartRepository.save(cart);
    }

    public CartDto addItemToCart(Long userId, CartDto.AddItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.getProductEntityById(request.productId());

        if (!product.isActive()) {
            throw new IllegalStateException("Product is not available");
        }

        if (product.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity(), request.quantity());
        }

        var existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.productId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.quantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new InsufficientStockException(product.getName(), product.getStockQuantity(), newQuantity);
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, request.quantity());
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return CartDto.fromEntity(cartRepository.findByUserIdWithItems(userId).orElseThrow());
    }

    public CartDto updateCartItemQuantity(Long userId, Long productId, CartDto.UpdateItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        Product product = productService.getProductEntityById(productId);

        if (product.getStockQuantity() < request.quantity()) {
            throw new InsufficientStockException(product.getName(), product.getStockQuantity(), request.quantity());
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return CartDto.fromEntity(cartRepository.findByUserIdWithItems(userId).orElseThrow());
    }

    public CartDto removeItemFromCart(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);

        return CartDto.fromEntity(cartRepository.findByUserIdWithItems(userId).orElseThrow());
    }

    public CartDto clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "userId", userId));

        cartItemRepository.deleteAllByCartId(cart.getId());
        cart.getCartItems().clear();

        return CartDto.fromEntity(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    public void validateCartForCheckout(Long userId) {
        Cart cart = getCartEntityByUserId(userId);

        if (cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (!product.isActive()) {
                throw new IllegalStateException("Product '" + product.getName() + "' is no longer available");
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(product.getName(), product.getStockQuantity(), item.getQuantity());
            }
        }
    }
}
