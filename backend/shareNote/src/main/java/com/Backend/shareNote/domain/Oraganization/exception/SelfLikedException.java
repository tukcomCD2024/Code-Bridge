package com.Backend.shareNote.domain.Oraganization.exception;

public class SelfLikedException extends RuntimeException {
    public SelfLikedException(String message) {
        super(message);
    }
}
