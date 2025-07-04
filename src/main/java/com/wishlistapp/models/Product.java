package com.wishlistapp.models;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Product {
    private String id; // Unique ID for the product within the wishlist
    private String name;
    private String imageUrl;
    private double price;
    private String addedByUserId; // ID of the user who added it
    private String addedByUsername; // Or email, for display
    private LocalDateTime createdAt;
    private LocalDateTime lastEditedAt;
}