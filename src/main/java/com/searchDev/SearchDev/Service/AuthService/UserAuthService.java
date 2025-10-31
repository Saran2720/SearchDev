package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.ExceptionHandler.UserAlreadyExistException;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class UserAuthService {


    @Autowired
    private UserRepo userRepo;

    public Users register(Users user){
        Users isExistUser = userRepo.findByEmail(user.getEmail());
        if(isExistUser!=null){
            throw new UserAlreadyExistException("Email already exist");
        }
        return userRepo.save(user);
    }
}
