package com.example.bakery.service;

import com.example.bakery.dto.AddItemToCartRequest;
import com.example.bakery.dto.CartDto;
import com.example.bakery.dto.CartItemDto;
import com.example.bakery.dto.UpdateCartItemRequest;
import com.example.bakery.model.Cart;
import com.example.bakery.model.CartItem;
import com.example.bakery.model.Product;
import com.example.bakery.repository.CartItemRepository;
import com.example.bakery.repository.CartRepository;
import com.example.bakery.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
// import java.util.UUID; // Không cần thiết nếu bạn tự sinh CHAR(7)
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final IdGeneratorService idGeneratorService; // <-- Inject IdGeneratorService

    @Transactional
    public CartDto getCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tìm thấy: " + cartId));
        return mapToCartDto(cart);
    }


    @Transactional
    public CartDto addItemToCart(AddItemToCartRequest request) {
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tìm thấy: " + request.getCartId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tìm thấy: " + request.getProductId()));

        if (request.getQuantity() <= 0 || request.getQuantity() > product.getStockQuantity()) {
            throw new IllegalArgumentException("Số lượng không hợp lệ hoặc vượt quá số lượng tồn kho.");
        }

        Optional<CartItem> existingCartItem = cartItemRepository.findByCart_CartIdAndProduct_ProductId(cart.getCartId(), product.getProductId());

        if (existingCartItem.isPresent()) {
            CartItem item = existingCartItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Tổng số lượng vượt quá số lượng tồn kho.");
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCartItemId(idGeneratorService.generateCartItemId());
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setQuantity(request.getQuantity());
            newCartItem.setPriceAtAddition(product.getPrice());

            cartItemRepository.save(newCartItem);
            cart.addCartItem(newCartItem); // Đảm bảo mối quan hệ được cập nhật đúng nếu bạn đang dùng bidirectional
        }

        // Cập nhật lại giỏ hàng để trigger last_modified_date update
        cartRepository.save(cart);

        return mapToCartDto(cart);
    }

    // Hàm này bạn cần thêm vào
    private CartDto mapToCartDto(Cart cart) {
        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(this::mapToCartItemDto)
                .collect(Collectors.toList());

        // Tính toán tổng số lượng và tổng tiền
        int totalItems = itemDtos.size(); // Số lượng loại sản phẩm khác nhau
        // Nếu bạn muốn tính tổng số lượng của TẤT CẢ sản phẩm trong giỏ (ví dụ: 2 áo + 3 quần = 5 items),
        // thì thay bằng:
        // int totalItems = itemDtos.stream().mapToInt(CartItemDto::getQuantity).sum();

        BigDecimal totalAmount = itemDtos.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartDto(
                cart.getCartId(),
                cart.getUser().getUserId(), // Gán userId nếu có
                itemDtos,
                totalItems,
                totalAmount
        );
    }

    // Hàm này bạn cần thêm vào
    private CartItemDto mapToCartItemDto(CartItem cartItem) {
        // Giả định Product có các trường getName(), getImageUrl(), getPrice()
        // imageUrls có thể là một List<String>, bạn có thể lấy ảnh đầu tiên
        String imageUrl = (cartItem.getProduct().getImageUrl() != null && !cartItem.getProduct().getImageUrl().isEmpty())
                ? cartItem.getProduct().getImageUrl()
                : null; // hoặc một URL ảnh mặc định

        return new CartItemDto(
                cartItem.getCartItemId(),
                cartItem.getProduct().getProductId(),
                cartItem.getProduct().getName(),
                imageUrl, // Lấy URL ảnh từ Product
                cartItem.getPriceAtAddition(),
                cartItem.getQuantity(),
                cartItem.getPriceAtAddition().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    // Hàm tạo giỏ hàng cho khách (nếu bạn muốn hỗ trợ guest cart)
    // Bạn cần tạo endpoint cho hàm này trong CartController.
    @Transactional
    public CartDto createGuestCart() {
        Cart newCart = new Cart();
        newCart.setCartId(idGeneratorService.generateCartId()); // Sinh ID cho giỏ hàng
        newCart.setCreatedAt(java.time.LocalDateTime.now());
        newCart.setLastModifiedAt(java.time.LocalDateTime.now());
        // Không set user nếu là giỏ hàng khách
        cartRepository.save(newCart);
        return mapToCartDto(newCart);
    }

    @Transactional
    public CartDto updateCartItemQuantity(UpdateCartItemRequest request) {
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tìm thấy: " + request.getCartId()));

        CartItem cartItem = cartItemRepository.findById(request.getCartItemId())
                .orElseThrow(() -> new RuntimeException("Mục giỏ hàng không tìm thấy: " + request.getCartItemId()));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Mục giỏ hàng không thuộc giỏ hàng này.");
        }

        Product product = cartItem.getProduct();
        if (request.getQuantity() <= 0) {
            // Nếu số lượng là 0 hoặc âm, xóa mục đó
            cart.removeCartItem(cartItem); // Xóa khỏi set trong cart entity
            cartItemRepository.delete(cartItem); // Xóa khỏi DB
        } else if (request.getQuantity() > product.getStockQuantity()) {
            throw new IllegalArgumentException("Số lượng cập nhật vượt quá số lượng tồn kho.");
        } else {
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        // Lưu lại giỏ hàng để cập nhật last_modified_date
        cartRepository.save(cart);

        return mapToCartDto(cart);
    }

    @Transactional
    public CartDto removeCartItem(String cartId, String cartItemId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tìm thấy: " + cartId));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Mục giỏ hàng không tìm thấy: " + cartItemId));

        // Đảm bảo mục giỏ hàng thuộc về giỏ hàng được yêu cầu
        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new IllegalArgumentException("Mục giỏ hàng không thuộc giỏ hàng này.");
        }

        cart.removeCartItem(cartItem); // Xóa khỏi set trong cart entity
        cartItemRepository.delete(cartItem); // Xóa khỏi DB

        // Lưu lại giỏ hàng để cập nhật last_modified_date
        cartRepository.save(cart);

        return mapToCartDto(cart);
    }

    @Transactional
    public void clearCart(String cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng không tìm thấy: " + cartId));

        // Xóa tất cả cartItems liên quan
        // Do có orphanRemoval = true trên @OneToMany, khi clear Set và lưu Cart,
        // các CartItem tương ứng sẽ bị xóa khỏi DB.
        if (cart.getCartItems() != null) {
            cart.getCartItems().clear();
        }
        cartRepository.save(cart); // Lưu lại để kích hoạt orphanRemoval
    }


}