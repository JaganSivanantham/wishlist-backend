package com.wishlistapp.services;

import com.wishlistapp.models.Product;
import com.wishlistapp.models.User;
import com.wishlistapp.models.Wishlist;
import com.wishlistapp.repositories.UserRepository;
import com.wishlistapp.repositories.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Wishlist> getUserWishlists(String userId) {
        return wishlistRepository.findByOwnerIdOrCollaboratorIdsContaining(userId, userId);
    }

    public Optional<Wishlist> getWishlistById(String id) {
        return wishlistRepository.findById(id);
    }

    public Wishlist createWishlist(Wishlist wishlist, String ownerId) {
        Optional<User> ownerOptional = userRepository.findById(ownerId);
        if (ownerOptional.isEmpty()) {
            throw new RuntimeException("Owner not found for ID: " + ownerId);
        }
        wishlist.setOwnerId(ownerId);
        wishlist.setOwnerUsername(ownerOptional.get().getUsername());
        wishlist.setId(UUID.randomUUID().toString()); // Generate ID for new wishlist
        return wishlistRepository.save(wishlist);
    }

    public Optional<Wishlist> updateWishlist(String id, Wishlist updatedWishlist) {
        return wishlistRepository.findById(id)
                .map(wishlist -> {
                    wishlist.setTitle(updatedWishlist.getTitle());
                    wishlist.setDescription(updatedWishlist.getDescription());
                    // Collaborators update logic might be more complex
                    // For now, simple replacement
                    wishlist.setCollaboratorIds(updatedWishlist.getCollaboratorIds());
                    return wishlistRepository.save(wishlist);
                });
    }

    public boolean deleteWishlist(String id) {
        if (wishlistRepository.existsById(id)) {
            wishlistRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Wishlist> addProductToWishlist(String wishlistId, Product product, String addedByUserId) {
        return wishlistRepository.findById(wishlistId)
                .map(wishlist -> {
                    Optional<User> userOptional = userRepository.findById(addedByUserId);
                    if (userOptional.isEmpty()) {
                        throw new RuntimeException("User adding product not found for ID: " + addedByUserId);
                    }
                    product.setId(UUID.randomUUID().toString()); // Unique ID for the product
                    product.setAddedByUserId(addedByUserId);
                    product.setAddedByUsername(userOptional.get().getUsername()); // Set username for display
                    product.setCreatedAt(LocalDateTime.now());
                    product.setLastEditedAt(LocalDateTime.now());
                    wishlist.getProducts().add(product);
                    return wishlistRepository.save(wishlist);
                });
    }

    public Optional<Wishlist> updateProductInWishlist(String wishlistId, String productId, Product updatedProduct) {
        return wishlistRepository.findById(wishlistId)
                .map(wishlist -> {
                    Optional<Product> existingProductOptional = wishlist.getProducts().stream()
                            .filter(p -> p.getId().equals(productId))
                            .findFirst();

                    if (existingProductOptional.isPresent()) {
                        Product existingProduct = existingProductOptional.get();
                        existingProduct.setName(updatedProduct.getName());
                        existingProduct.setImageUrl(updatedProduct.getImageUrl());
                        existingProduct.setPrice(updatedProduct.getPrice());
                        existingProduct.setLastEditedAt(LocalDateTime.now());
                        return wishlistRepository.save(wishlist);
                    }
                    return null; // Product not found in wishlist
                });
    }

    public Optional<Wishlist> removeProductFromWishlist(String wishlistId, String productId) {
        return wishlistRepository.findById(wishlistId)
                .map(wishlist -> {
                    wishlist.getProducts().removeIf(p -> p.getId().equals(productId));
                    return wishlistRepository.save(wishlist);
                });
    }
}