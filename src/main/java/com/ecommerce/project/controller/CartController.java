package com.ecommerce.project.controller;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();

        return ResponseEntity.ok(cartDTOs);
    }

    @GetMapping("carts/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartService.getCartFromEmail(email);
        CartDTO cartDTO = cartService.getCart(email, userCart.getCartId());
        return ResponseEntity.ok(cartDTO);
    }

    @PostMapping("carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(
            @PathVariable Long productId,
            @PathVariable Integer quantity
    ) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return ResponseEntity.ok(cartDTO);
    }

    @PutMapping("cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(
        @PathVariable Long productId,
        @PathVariable String operation
    ) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(
                productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1
        );
        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId
    ) {
        String result = cartService.deleteProductFromCart(cartId, productId);
        return ResponseEntity.ok(result);
    }
}
