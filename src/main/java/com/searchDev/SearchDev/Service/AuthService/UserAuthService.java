package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {


    @Autowired
    private UserRepo userRepo;

    public Users register(Users user){

        return userRepo.save(user);
    }
}
