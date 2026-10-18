package com.spendwise.service;

import java.util.List;

import com.spendwise.entity.Transaction;

public interface TransactionService {

	public abstract Transaction createTransaction(Transaction transaction);
	List<Transaction> getAllTransaction();
	void deleteTransaction(Integer id);
	Transaction updateTransaction(Integer id, Transaction transaction);
}
