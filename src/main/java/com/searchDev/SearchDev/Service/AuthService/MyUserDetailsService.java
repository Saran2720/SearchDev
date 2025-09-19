package com.searchDev.SearchDev.Service.AuthService;

import com.searchDev.SearchDev.Model.UserPrincipal;
import com.searchDev.SearchDev.Model.Users;
import com.searchDev.SearchDev.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        System.out.println("email comes for loaduserByusername :" + email);
        Users user = userRepo.findByEmail(email);
//        System.out.println("user email from db "+ user);
        if(user==null){
//            throw new UsernameNotFoundException("Email was not found in db");
        }
        return new UserPrincipal(user);
    }
}
