package com.wishlistapp.controllers;

import com.wishlistapp.models.Product;
import com.wishlistapp.models.User;
import com.wishlistapp.models.Wishlist;
import com.wishlistapp.services.UserService;
import com.wishlistapp.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlists")
@CrossOrigin(origins = "http://localhost:3000") // Allow React app to access
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    // Helper to extract user ID from token (for authenticated requests)
    private String getUserIdFromToken(String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            Optional<User> userOptional = userService.getUserByToken(token);
            return userOptional.map(User::getId).orElse(null);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<Wishlist>> getUserWishlists(@RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Wishlist> wishlists = wishlistService.getUserWishlists(userId);
        return ResponseEntity.ok(wishlists);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wishlist> getWishlistById(@PathVariable String id, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Add authorization check: only owner or collaborator can view
        Optional<Wishlist> wishlistOptional = wishlistService.getWishlistById(id);
        if (wishlistOptional.isPresent()) {
            Wishlist wishlist = wishlistOptional.get();
            if (wishlist.getOwnerId().equals(userId) || wishlist.getCollaboratorIds().contains(userId)) {
                return ResponseEntity.ok(wishlist);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Not authorized to view this wishlist
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Wishlist> createWishlist(@RequestBody Wishlist wishlist, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Wishlist createdWishlist = wishlistService.createWishlist(wishlist, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWishlist);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wishlist> updateWishlist(@PathVariable String id, @RequestBody Wishlist wishlist, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Add authorization check: only owner can update core wishlist details
        Optional<Wishlist> existingWishlistOptional = wishlistService.getWishlistById(id);
        if (existingWishlistOptional.isEmpty() || !existingWishlistOptional.get().getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<Wishlist> updatedWishlist = wishlistService.updateWishlist(id, wishlist);
        return updatedWishlist.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable String id, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Add authorization check: only owner can delete
        Optional<Wishlist> existingWishlistOptional = wishlistService.getWishlistById(id);
        if (existingWishlistOptional.isEmpty() || !existingWishlistOptional.get().getOwnerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (wishlistService.deleteWishlist(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Product CRUD operations
    @PostMapping("/{wishlistId}/products")
    public ResponseEntity<Wishlist> addProductToWishlist(@PathVariable String wishlistId, @RequestBody Product product, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check if user is owner or collaborator of the wishlist
        Optional<Wishlist> wishlistOptional = wishlistService.getWishlistById(wishlistId);
        if (wishlistOptional.isEmpty() || (!wishlistOptional.get().getOwnerId().equals(userId) && !wishlistOptional.get().getCollaboratorIds().contains(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            Optional<Wishlist> updatedWishlist = wishlistService.addProductToWishlist(wishlistId, product, userId);
            return updatedWishlist.map(wl -> ResponseEntity.status(HttpStatus.CREATED).body(wl)).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Or a more specific error
        }
    }

    @PutMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<Wishlist> updateProductInWishlist(
            @PathVariable String wishlistId,
            @PathVariable String productId,
            @RequestBody Product product,
            @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check if user is owner or collaborator of the wishlist
        Optional<Wishlist> wishlistOptional = wishlistService.getWishlistById(wishlistId);
        if (wishlistOptional.isEmpty() || (!wishlistOptional.get().getOwnerId().equals(userId) && !wishlistOptional.get().getCollaboratorIds().contains(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Wishlist> updatedWishlist = wishlistService.updateProductInWishlist(wishlistId, productId, product);
        return updatedWishlist.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{wishlistId}/products/{productId}")
    public ResponseEntity<Wishlist> removeProductFromWishlist(
            @PathVariable String wishlistId,
            @PathVariable String productId,
            @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Check if user is owner or collaborator of the wishlist
        Optional<Wishlist> wishlistOptional = wishlistService.getWishlistById(wishlistId);
        if (wishlistOptional.isEmpty() || (!wishlistOptional.get().getOwnerId().equals(userId) && !wishlistOptional.get().getCollaboratorIds().contains(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Optional<Wishlist> updatedWishlist = wishlistService.removeProductFromWishlist(wishlistId, productId);
        return updatedWishlist.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Mock invite endpoint
    @PostMapping("/{wishlistId}/invite")
    public ResponseEntity<?> inviteUserToWishlist(@PathVariable String wishlistId, @RequestBody Map<String, String> inviteRequest, @RequestHeader("Authorization") String tokenHeader) {
        String userId = getUserIdFromToken(tokenHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String invitedEmail = inviteRequest.get("email");

        // For now, mock the invitation logic
        Optional<Wishlist> wishlistOptional = wishlistService.getWishlistById(wishlistId);
        if (wishlistOptional.isPresent()) {
            Wishlist wishlist = wishlistOptional.get();
            // Ensure only owner can invite
            if (!wishlist.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only the wishlist owner can invite others."));
            }

            Optional<User> invitedUserOptional = userService.findByEmail(invitedEmail); // Assume we have this method in UserService
            if (invitedUserOptional.isPresent()) {
                User invitedUser = invitedUserOptional.get();
                if (!wishlist.getCollaboratorIds().contains(invitedUser.getId()) && !wishlist.getOwnerId().equals(invitedUser.getId())) {
                    wishlist.getCollaboratorIds().add(invitedUser.getId());
                    wishlistService.updateWishlist(wishlistId, wishlist); // Update to persist collaborator
                    return ResponseEntity.ok(Map.of("message", "User " + invitedEmail + " invited to wishlist (mock update)."));
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "User " + invitedEmail + " is already a member or owner of this wishlist."));
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User with email " + invitedEmail + " not found."));
            }
        }
        return ResponseEntity.notFound().build();
    }
}