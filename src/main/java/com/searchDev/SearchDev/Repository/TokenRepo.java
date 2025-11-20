package com.searchDev.SearchDev.Repository;

import com.searchDev.SearchDev.Model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TokenRepo extends JpaRepository<PasswordResetToken,UUID> {
    PasswordResetToken findByResetTokenId(UUID uuid);
}
