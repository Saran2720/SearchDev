package com.searchDev.SearchDev.Repository;

import com.searchDev.SearchDev.Model.Users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<Users, UUID> {
    Users findByEmail(String email);

    Page<Users> findByUsernameIgnoreCase(String username, Pageable pageable);

    //for returning page of users by searching by username

}
