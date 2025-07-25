package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;

    @Override
    public Cart getCartFromEmail(String email) {
        Cart userCart = cartRepository.findCartByEmail(email);
        if (userCart == null) {
            throw new APIException("Cart associated with email(" + email + ") does not exists");
        }

        return userCart;
    }

    @Override
    public CartDTO getCart(String email, Long cartId) {
        Cart userCart = cartRepository.findCartByEmailAndCartId(email, cartId);
        if (userCart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(userCart, CartDTO.class);
        List<CartItem> cartItems = userCart.getCartItems();
        List<ProductDTO> productDTOs = cartItems.stream().map(ci -> {
            ProductDTO map = modelMapper.map(ci.getProduct(), ProductDTO.class);
            map.setQuantity(ci.getQuantity());
            return map;
        }).toList();
        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }

        return carts.stream().map(cart -> {
           CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
           List<ProductDTO> productDTOs = cart.getCartItems().stream().map(cartItem -> {
               ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
               productDTO.setQuantity(cartItem.getQuantity());
               return productDTO;
           }).toList();
           cartDTO.setProducts(productDTOs);

           return cartDTO;
        }).toList();
    }

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = getOrCreateCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }
        if (product.getQuantity() < quantity) {
            throw new APIException(
                    "Please make an order of the " + product.getProductName() +
                    " less than or equal to the quantity: " + product.getQuantity());
        }

        // Cart new cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        cartItemRepository.save(newCartItem);

        // Update related model
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        // Format output
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        // Prevent empty cart when the cart entity is newly created
        // (there will be error where cart items will be empty when the cart item has been added to the cart for the first time)
        if (cartItems.isEmpty()) {
            cartItems.add(newCartItem);
        }
        List<ProductDTO> productDTOs = cartItems.stream().map(ci -> {
           ProductDTO map = modelMapper.map(ci.getProduct(), ProductDTO.class);
           map.setQuantity(ci.getQuantity());
           return map;
        }).toList();
        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String email = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(email);
        Long cartId = cart.getCartId();

        // Retrieve cart entity from repository again to prevent using entity that is not fully initialized
        // (Some relationships might use lazy loading which might have issue when saving data back to the database)
        Cart userCart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart");
        }

        Integer newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity < 0) {
            throw new APIException("Quantity cannot be negative");
        }
        if (product.getQuantity() < newQuantity) {
            throw new APIException(
                    "Please make an order of the " + product.getProductName() +
                            " less than or equal to the quantity: " + newQuantity);
        }

        if (newQuantity <= 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(newQuantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        cartItemRepository.save(cartItem);
        CartDTO cartDTO = modelMapper.map(userCart, CartDTO.class);
        List<CartItem> cartItems = userCart.getCartItems();
        List<ProductDTO> productDTOs = cartItems.stream().map(ci -> {
            ProductDTO map = modelMapper.map(ci.getProduct(), ProductDTO.class);
            map.setQuantity(ci.getQuantity());
            return map;
        }).toList();
        cartDTO.setProducts(productDTOs);

        return cartDTO;
    }

    @Override
    @Transactional
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        Double totalProductValue = cartItem.getProductPrice() * cartItem.getQuantity();
        cart.setTotalPrice(cart.getTotalPrice() - totalProductValue);
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " has been removed from the cart";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " is not available in the cart");
        }

        Double oldTotalProductPrice = cartItem.getProductPrice() * cartItem.getQuantity();
        Double newTotalProductPrice = product.getSpecialPrice() * cartItem.getQuantity();
        Double cartPrice = cart.getTotalPrice() - oldTotalProductPrice + newTotalProductPrice;

        // Update new product unit price in the cart
        cartItem.setProductPrice(product.getSpecialPrice());
        // Update total price in the cart
        cart.setTotalPrice(cartPrice);

        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if (userCart != null) {
            return userCart;
        }

        Cart newCart = new Cart();
        newCart.setTotalPrice(0.0);
        newCart.setUser(authUtil.loggedInUser());
        return cartRepository.save(newCart);
    }
}
