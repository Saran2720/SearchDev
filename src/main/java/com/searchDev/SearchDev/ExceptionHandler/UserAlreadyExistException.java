package com.searchDev.SearchDev.ExceptionHandler;

public class UserAlreadyExistException extends  RuntimeException{
    public UserAlreadyExistException(String message){
        super(message);
    }
}
