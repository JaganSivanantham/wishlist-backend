package com.wishlistapp.repositories;

import com.wishlistapp.models.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {
    List<Wishlist> findByOwnerIdOrCollaboratorIdsContaining(String ownerId, String collaboratorId);
}