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
        Users user = userRepo.findByEmail(email);
        if(user==null){
            throw new UsernameNotFoundException("Email was not found");
        }
        return new UserPrincipal(user);
    }
}
