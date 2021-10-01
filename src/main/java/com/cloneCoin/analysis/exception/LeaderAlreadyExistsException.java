package com.cloneCoin.analysis.exception;

public class LeaderAlreadyExistsException extends RuntimeException{

    public LeaderAlreadyExistsException(String message){
        super(message);
    }
}
