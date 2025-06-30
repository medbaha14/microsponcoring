package com.example.microsponsoringbackend.repository;

import com.example.microsponsoringbackend.model.User;
import com.example.microsponsoringbackend.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    List<User> findAllByUserType(UserType userType);
} 