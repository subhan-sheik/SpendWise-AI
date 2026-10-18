package com.spendwise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spendwise.entity.Transaction;
import com.spendwise.exception.TransactionNotFoundException;
import com.spendwise.repository.TransactionRepository;
import com.spendwise.service.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

	@Mock
	private TransactionRepository transactionRepository;

	@InjectMocks
	private TransactionServiceImpl transactionService;

	@Test
	public void testCreateTransaction() {

		Transaction transaction = new Transaction();

		transaction.setType("EXPENSE");
		transaction.setAmount(new BigDecimal("500"));
		transaction.setCategory("Food");
		transaction.setDate(LocalDate.of(2026, 10, 12));
		transaction.setDescription("Lunch");

		when(transactionRepository.save(transaction)).thenReturn(transaction);

		Transaction result = transactionService.createTransaction(transaction);

		assertSame(transaction, result);
		assertEquals("EXPENSE", result.getType());
		assertEquals(new BigDecimal("500"), result.getAmount());
		assertEquals("Food", result.getCategory());

		verify(transactionRepository).save(transaction);
	}

	@Test
	public void testGetAllTransactions() {

		Transaction transaction1 = new Transaction();
		transaction1.setType("EXPENSE");
		transaction1.setAmount(new BigDecimal("500"));
		transaction1.setCategory("Food");

		Transaction transaction2 = new Transaction();
		transaction2.setType("INCOME");
		transaction2.setAmount(new BigDecimal("2500"));
		transaction2.setCategory("Salary");

		List<Transaction> expectedTransactions = List.of(transaction1, transaction2);

		when(transactionRepository.findAll()).thenReturn(expectedTransactions);

		List<Transaction> result = transactionService.getAllTransaction();

		assertEquals(2, result.size());

		assertEquals("Food", result.get(0).getCategory());
		assertEquals("Salary", result.get(1).getCategory());

		verify(transactionRepository).findAll();
	}

	@Test
	public void testDeleteTransaction() {

		Integer transactionId = 1;
		Transaction existingTransaction = new Transaction();

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existingTransaction));

		transactionService.deleteTransaction(transactionId);

		verify(transactionRepository).findById(transactionId);
		verify(transactionRepository).delete(existingTransaction);
	}

	@Test
	public void testUpdateTransaction() {

		Integer id = 1;

		Transaction existing = new Transaction();
		existing.setType("EXPENSE");
		existing.setAmount(new BigDecimal("500"));
		existing.setCategory("Food");
		existing.setDate(LocalDate.of(2026, 10, 12));
		existing.setDescription("Lunch");

		Transaction updated = new Transaction();
		updated.setType("INCOME");
		updated.setAmount(new BigDecimal("2500"));
		updated.setCategory("Salary");
		updated.setDate(LocalDate.of(2026, 10, 12));
		updated.setDescription("Monthly salary");

		when(transactionRepository.findById(id)).thenReturn(Optional.of(existing));

		when(transactionRepository.save(existing)).thenReturn(existing);

		Transaction result = transactionService.updateTransaction(id, updated);

		assertSame(existing, result);
		assertEquals("INCOME", result.getType());
		assertEquals(new BigDecimal("2500"), result.getAmount());
		assertEquals("Salary", result.getCategory());
		assertEquals("Monthly salary", result.getDescription());

		verify(transactionRepository).findById(id);
		verify(transactionRepository).save(existing);
	}

	@Test
	public void testUpdateTransactionNotFound() {

		Integer id = 999;

		when(transactionRepository.findById(id)).thenReturn(Optional.empty());

		TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
				() -> transactionService.updateTransaction(id, new Transaction()));

		assertEquals("Transaction not found with id: 999", exception.getMessage());

		verify(transactionRepository).findById(id);
	}

	@Test
	public void testDeleteTransactionNotFound() {

		Integer id = 999;

		when(transactionRepository.findById(id)).thenReturn(Optional.empty());

		TransactionNotFoundException exception = assertThrows(TransactionNotFoundException.class,
				() -> transactionService.deleteTransaction(id));

		assertEquals("Transaction not found with id: 999", exception.getMessage());

		verify(transactionRepository).findById(id);

		verify(transactionRepository, never()).delete(any(Transaction.class));
	}

	@Test
	public void testTransactionIdGetterAndSetter() {

		Transaction transaction = new Transaction();

		transaction.setId(1);

		assertEquals(Integer.valueOf(1), transaction.getId());
	}

}
