package com.ecommerce.project.service;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    List<CartDTO> getAllCarts();

    Cart getCartFromEmail(String email);

    CartDTO getCart(String email, Long cartId);

    CartDTO addProductToCart(Long productId, Integer quantity);
    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer quantity);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCart(Long cartId, Long productId);
}
