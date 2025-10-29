package com.searchDev.SearchDev.ExceptionHandler;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException(String message){
        super(message);
    }
}
