package com.spendwise.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.spendwise.entity.Transaction;
import com.spendwise.exception.TransactionNotFoundException;
import com.spendwise.repository.TransactionRepository;

@Service
public class TransactionServiceImpl implements TransactionService {

	private final TransactionRepository transactionRepository;

	public TransactionServiceImpl(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Override
	public Transaction createTransaction(Transaction transaction) {
		return transactionRepository.save(transaction);
	}

	@Override
	public List<Transaction> getAllTransaction() {
		return transactionRepository.findAll();
	}

	@Override
	public void deleteTransaction(Integer id) {

		Transaction existingTransaction = transactionRepository.findById(id)
				.orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

		transactionRepository.delete(existingTransaction);
	}

	@Override
	public Transaction updateTransaction(Integer id, Transaction transaction) {
		Transaction existingTransaction = transactionRepository.findById(id)
				.orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));

		existingTransaction.setType(transaction.getType());
		existingTransaction.setAmount(transaction.getAmount());
		existingTransaction.setCategory(transaction.getCategory());
		existingTransaction.setDate(transaction.getDate());
		existingTransaction.setDescription(transaction.getDescription());

		return transactionRepository.save(existingTransaction);
	}

}
