package com.url.shortener.exceptions;

public class ShortUrlTooLongException extends RuntimeException {
    public ShortUrlTooLongException(String message) {
        super(message);
    }
}
