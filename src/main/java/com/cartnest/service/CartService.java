package com.cartnest.service;

import com.cartnest.dto.CartDto;
import com.cartnest.exception.ResourceNotFoundException;
import com.cartnest.model.Cart;
import com.cartnest.model.CartItem;
import com.cartnest.model.Product;
import com.cartnest.model.User;
import com.cartnest.repository.CartRepository;
import com.cartnest.repository.ProductRepository;
import com.cartnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartDto getCart(String email) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createCart(user));
        return mapToDto(cart);
    }

    public CartDto addToCart(String email, Long productId, int quantity) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createCart(user));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.getItems().add(newItem);
        }
        return mapToDto(cartRepository.save(cart));
    }

    public CartDto updateCartItem(String email, Long itemId, int quantity) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return mapToDto(cartRepository.save(cart));
    }

    public CartDto removeFromCart(String email, Long itemId) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return mapToDto(cartRepository.save(cart));
    }

    public void clearCart(String email) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    private Cart createCart(User user) {
        return cartRepository.save(Cart.builder().user(user).build());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private CartDto mapToDto(Cart cart) {
        List<CartDto.CartItemDto> items = cart.getItems().stream()
                .map(item -> CartDto.CartItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getProduct().getImageUrl())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalPrice = items.stream()
                .map(CartDto.CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(totalPrice)
                .totalItems(items.stream().mapToInt(CartDto.CartItemDto::getQuantity).sum())
                .build();
    }
}
