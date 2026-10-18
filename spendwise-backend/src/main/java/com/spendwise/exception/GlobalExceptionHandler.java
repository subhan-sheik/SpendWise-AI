package com.spendwise.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

		Map<String, String> errors = new HashMap<>();

		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Validation failed");
		response.put("errors", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleTransactionNotFound(
	        TransactionNotFoundException ex) {

	    Map<String, Object> response = new HashMap<>();
	    response.put("message", "Resource not found");
	    response.put("errors", Map.of("transaction", ex.getMessage()));

	    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

}
