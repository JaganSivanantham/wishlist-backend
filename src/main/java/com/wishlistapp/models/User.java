package com.wishlistapp.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data // Lombok: Generates getters, setters, toString, equals, hashCode
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private String passwordHash; // In a real app, this would be properly hashed
}