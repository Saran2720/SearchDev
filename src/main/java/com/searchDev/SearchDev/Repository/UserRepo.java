package com.searchDev.SearchDev.Repository;

import com.searchDev.SearchDev.Model.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<Users, UUID> {
    Users findByEmail(String email);
}
