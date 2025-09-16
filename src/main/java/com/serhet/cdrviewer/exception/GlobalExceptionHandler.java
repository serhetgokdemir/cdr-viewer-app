package com.serhet.cdrviewer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler.
 * Tum controller'lar icin istisnalari yakalayip standard JSON hata yaniti dondurur.
 * Donus tipi: CdrErrorResponse (status, message, timeStamp).
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CdrNotFoundException icin ozel handler.
     * 404 NOT_FOUND ve anlamli bir mesaj dondurur.
     */
    @ExceptionHandler
    public ResponseEntity<CdrErrorResponse> handleException(CdrNotFoundException exc) {

        CdrErrorResponse error = new CdrErrorResponse(
                HttpStatus.NOT_FOUND.value(),   // 404
                exc.getMessage(),                // istisna mesaji
                System.currentTimeMillis()       // zaman damgasi (ms)
        );

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Diger tum istisnalar icin genel handler.
     * Burada 400 BAD_REQUEST donduruluyor.
     * (Ihtiyaca gore 500 INTERNAL_SERVER_ERROR tercih edilebilir.)
     */
    @ExceptionHandler
    public ResponseEntity<CdrErrorResponse> handleException(Exception exc) {

        CdrErrorResponse error = new CdrErrorResponse(
                HttpStatus.BAD_REQUEST.value(),  // 400
                exc.getMessage(),                // genel mesaj
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}