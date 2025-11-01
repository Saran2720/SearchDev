package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.ExceptionHandler.UserAlreadyExistException;
import com.searchDev.SearchDev.Model.PasswordResetToken;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.TokenRepo;
import com.searchDev.SearchDev.Repository.UserRepo;
import com.searchDev.SearchDev.Service.UserService.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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

    @Autowired
    private TokenRepo tokenRepo;
    public Users forgetPassword(String email) {
       Users isExistUser =   userRepo.findByEmail(email);
       System.out.println(isExistUser);
       if(isExistUser==null){
           throw new UserNotFoundException("User not found");
       }

       UUID token = UUID.randomUUID();
        PasswordResetToken prt = PasswordResetToken.builder()
                .resetTokenId(token)
                .email(email)
                .time(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepo.save(prt);

        String resetLink = "http://localhost:8080/reset-password?token="+ token;
        System.out.println("Reset link for " + email + ": " + resetLink);


       return isExistUser;
    }
}
