package com.spendwise.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spendwise.entity.Transaction;
import com.spendwise.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:5173}")
@Tag(name = "SpendWise Transactions", description = "APIs to create, view, update, and delete transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Operation(summary = "Create a transaction", description = "Validates and saves a new income or expense transaction")

	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid transaction data") })

	@PostMapping
	ResponseEntity<Transaction> createTransaction(@Valid @RequestBody Transaction transaction) {
		Transaction createTransaction = transactionService.createTransaction(transaction);
		return ResponseEntity.status(HttpStatus.CREATED).body(createTransaction);
	}

	@Operation(summary = "Get all transactions", description = "Retrieves all transactions stored in the database")

	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully") })

	@GetMapping
	List<Transaction> getAllTransaction() {
		return transactionService.getAllTransaction();
	}

	@Operation(summary = "Delete a transaction", description = "Deletes an existing transaction using its ID")

	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Transaction deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Transaction not found") })

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTransaction(@PathVariable Integer id) {
		transactionService.deleteTransaction(id);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Update a transaction", description = "Updates an existing transaction using its ID")

	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid transaction data"),
			@ApiResponse(responseCode = "404", description = "Transaction not found") })

	@PutMapping("/{id}")
	public Transaction updateTransaction(@Valid @PathVariable Integer id, @RequestBody Transaction transaction) {

		return transactionService.updateTransaction(id, transaction);
	}

}
