package com.searchDev.SearchDev.Service.AuthService;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {
   private final Set<String> blackListedTokens = ConcurrentHashMap.newKeySet();

   public void blackListToken(String token){
       blackListedTokens.add(token);
   }
   public boolean istokenBlackListed(String token){
       return blackListedTokens.contains(token);
   }

    @Override
    public String toString() {
        return "TokenBlacklistService{" +
                "blackListedTokens=" + blackListedTokens +
                '}';
    }
}
