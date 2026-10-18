package com.spendwise;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.spendwise.controller.TransactionController;
import com.spendwise.entity.Transaction;
import com.spendwise.exception.TransactionNotFoundException;
import com.spendwise.service.TransactionService;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransactionService transactionService;

	@Test
	public void testGetAllTransactions() throws Exception {

		when(transactionService.getAllTransaction()).thenReturn(List.of());

		mockMvc.perform(get("/api/transactions")).andExpect(status().isOk()).andExpect(content().json("[]"));

		verify(transactionService).getAllTransaction();
	}

	@Test
	public void testCreateTransaction() throws Exception {

		Transaction savedTransaction = new Transaction();
		savedTransaction.setType("EXPENSE");
		savedTransaction.setAmount(new BigDecimal("500"));
		savedTransaction.setCategory("Food");
		savedTransaction.setDate(LocalDate.of(2026, 10, 12));
		savedTransaction.setDescription("Lunch");

		when(transactionService.createTransaction(any(Transaction.class))).thenReturn(savedTransaction);

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": 500,
				    "category": "Food",
				    "date": "2026-10-12",
				    "description": "Lunch"
				}
				""")).andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("EXPENSE"))
				.andExpect(jsonPath("$.category").value("Food"));

		verify(transactionService).createTransaction(any(Transaction.class));
	}

	@Test
	public void testCreateTransactionWithInvalidAmount() throws Exception {

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": -500,
				    "category": "Food",
				    "date": "2026-10-12",
				    "description": "Invalid test"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.amount").value("Amount must be greater than zero"));

		verify(transactionService, never()).createTransaction(any(Transaction.class));
	}

	@Test
	public void testUpdateTransaction() throws Exception {

		Transaction updatedTransaction = new Transaction();
		updatedTransaction.setType("INCOME");
		updatedTransaction.setAmount(new BigDecimal("2500"));
		updatedTransaction.setCategory("Salary");
		updatedTransaction.setDate(LocalDate.of(2026, 10, 12));
		updatedTransaction.setDescription("Monthly salary");

		when(transactionService.updateTransaction(eq(1), any(Transaction.class))).thenReturn(updatedTransaction);

		mockMvc.perform(put("/api/transactions/1").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "INCOME",
				    "amount": 2500,
				    "category": "Salary",
				    "date": "2026-10-12",
				    "description": "Monthly salary"
				}
				""")).andExpect(status().isOk()).andExpect(jsonPath("$.type").value("INCOME"))
				.andExpect(jsonPath("$.amount").value(2500)).andExpect(jsonPath("$.category").value("Salary"))
				.andExpect(jsonPath("$.description").value("Monthly salary"));

		verify(transactionService).updateTransaction(eq(1), any(Transaction.class));
	}

	@Test
	public void testDeleteTransaction() throws Exception {

		mockMvc.perform(delete("/api/transactions/1")).andExpect(status().isNoContent());

		verify(transactionService).deleteTransaction(1);
	}

	@Test
	public void testDeleteTransactionNotFound() throws Exception {

		doThrow(new TransactionNotFoundException("Transaction not found with id: 999")).when(transactionService)
				.deleteTransaction(999);

		mockMvc.perform(delete("/api/transactions/999")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Resource not found"))
				.andExpect(jsonPath("$.errors.transaction").value("Transaction not found with id: 999"));

		verify(transactionService).deleteTransaction(999);
	}

	@Test
	public void testUpdateTransactionNotFound() throws Exception {

		when(transactionService.updateTransaction(eq(999), any(Transaction.class)))
				.thenThrow(new TransactionNotFoundException("Transaction not found with id: 999"));

		mockMvc.perform(put("/api/transactions/999").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": 500,
				    "category": "Food",
				    "date": "2026-10-12",
				    "description": "Test transaction"
				}
				""")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Resource not found"))
				.andExpect(jsonPath("$.errors.transaction").value("Transaction not found with id: 999"));

		verify(transactionService).updateTransaction(eq(999), any(Transaction.class));
	}

	@Test
	public void testCreateTransactionWithInvalidType() throws Exception {

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "TRANSFER",
				    "amount": 500,
				    "category": "Food",
				    "date": "2026-10-12",
				    "description": "Invalid transaction type"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.type").value("Type must be either INCOME or EXPENSE"));

		verify(transactionService, never()).createTransaction(any(Transaction.class));
	}

	@Test
	public void testCreateTransactionWithoutCategory() throws Exception {

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": 500,
				    "date": "2026-10-12",
				    "description": "Missing category test"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.category").value("Category is required"));

		verify(transactionService, never()).createTransaction(any(Transaction.class));
	}

	@Test
	public void testCreateTransactionWithFutureDate() throws Exception {

		String futureDate = LocalDate.now().plusDays(1).toString();

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": 500,
				    "category": "Food",
				    "date": "%s",
				    "description": "Future date test"
				}
				""".formatted(futureDate))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.date").value("Transaction date cannot be in the future"));

		verify(transactionService, never()).createTransaction(any(Transaction.class));
	}

	@Test
	public void testCreateTransactionAboveMaximumAmount() throws Exception {

		mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON_VALUE).content("""
				{
				    "type": "EXPENSE",
				    "amount": 10000001,
				    "category": "Shopping",
				    "date": "2026-10-12",
				    "description": "Above maximum amount test"
				}
				""")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.errors.amount").value("Amount cannot exceed 1 crore"));

		verify(transactionService, never()).createTransaction(any(Transaction.class));
	}

}
