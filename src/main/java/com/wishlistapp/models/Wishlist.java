package com.wishlistapp.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "wishlists")
@Data
public class Wishlist {
    @Id
    private String id;
    private String title;
    private String description;
    private String ownerId; // User ID of the creator
    private String ownerUsername; // For display
    private List<String> collaboratorIds = new ArrayList<>(); // List of User IDs
    private List<Product> products = new ArrayList<>();
}