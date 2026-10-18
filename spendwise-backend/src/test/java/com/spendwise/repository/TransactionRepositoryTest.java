package com.spendwise.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.spendwise.entity.Transaction;

@DataJpaTest
public class TransactionRepositoryTest {

	@Autowired
	private TransactionRepository transactionRepository;

	@Test
	public void testSaveAndFindTransaction() {

		Transaction transaction = new Transaction();

		transaction.setType("EXPENSE");
		transaction.setAmount(new BigDecimal("500.00"));
		transaction.setCategory("Food");
		transaction.setDate(LocalDate.of(2026, 10, 12));
		transaction.setDescription("Lunch");

		Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);

		assertNotNull(savedTransaction.getId());

		Optional<Transaction> result = transactionRepository.findById(savedTransaction.getId());

		assertTrue(result.isPresent());
		assertEquals("Food", result.get().getCategory());
		assertEquals(0, result.get().getAmount().compareTo(new BigDecimal("500.00")));
		assertEquals("Lunch", result.get().getDescription());
	}

	@Test
	public void testFindAllTransactions() {

		Transaction first = new Transaction();
		first.setType("EXPENSE");
		first.setAmount(new BigDecimal("500.00"));
		first.setCategory("Food");
		first.setDate(LocalDate.of(2026, 10, 12));
		first.setDescription("Lunch");

		Transaction second = new Transaction();
		second.setType("INCOME");
		second.setAmount(new BigDecimal("2500.00"));
		second.setCategory("Salary");
		second.setDate(LocalDate.of(2026, 10, 12));
		second.setDescription("Monthly salary");

		transactionRepository.save(first);
		transactionRepository.save(second);

		List<Transaction> transactions = transactionRepository.findAll();

		assertEquals(2, transactions.size());
	}

	@Test
	public void testDeleteTransaction() {

		Transaction transaction = new Transaction();

		transaction.setType("EXPENSE");
		transaction.setAmount(new BigDecimal("500.00"));
		transaction.setCategory("Food");
		transaction.setDate(LocalDate.of(2026, 10, 12));
		transaction.setDescription("Lunch");

		Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);

		Integer id = savedTransaction.getId();

		transactionRepository.deleteById(id);
		transactionRepository.flush();

		assertFalse(transactionRepository.existsById(id));
	}

	@Test
	public void testUpdateTransaction() {

		Transaction transaction = new Transaction();
		transaction.setType("EXPENSE");
		transaction.setAmount(new BigDecimal("500.00"));
		transaction.setCategory("Food");
		transaction.setDate(LocalDate.of(2026, 10, 12));
		transaction.setDescription("Lunch");

		Transaction savedTransaction = transactionRepository.saveAndFlush(transaction);

		savedTransaction.setAmount(new BigDecimal("750.00"));
		savedTransaction.setCategory("Shopping");
		savedTransaction.setDescription("Updated expense");

		transactionRepository.saveAndFlush(savedTransaction);

		Transaction result = transactionRepository.findById(savedTransaction.getId()).orElseThrow();

		assertEquals("Shopping", result.getCategory());
		assertEquals("Updated expense", result.getDescription());
		assertEquals(0, result.getAmount().compareTo(new BigDecimal("750.00")));
	}

}
