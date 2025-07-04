package com.wishlistapp.repositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories; 
import com.wishlistapp.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableMongoRepositories(basePackages = "com.wishlist.wishlistbackend.repository")
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}
