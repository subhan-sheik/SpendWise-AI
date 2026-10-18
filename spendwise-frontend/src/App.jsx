import "./App.css";
import { useEffect, useState, useRef, lazy, Suspense } from "react";
const ExpenseChart = lazy(() => import("./ExpenseChart"));

function formatDate(dateString) {
  if (!dateString) return "—";

  const [year, month, day] = dateString.split("-").map(Number);
  const date = new Date(year, month - 1, day);

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function formatCurrency(amount) {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(amount) || 0);
}

function getLocalDateString(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function App() {
  const [transaction, setTransaction] = useState({
    type: "EXPENSE",
    amount: "",
    category: "Food",
    date: "",
    description: "",
  });
  const [transactions, setTransactions] = useState([]);
  const [selectedMonth, setSelectedMonth] = useState(
    getLocalDateString().slice(0, 7),
  );

  const [searchTerm, setSearchTerm] = useState("");

  const [selectedType, setSelectedType] = useState("ALL");

  const [sortBy, setSortBy] = useState("newest");
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(5);
  const formRef = useRef(null);
  const [isLoading, setIsLoading] = useState(true);
  const [fetchError, setFetchError] = useState("");
  const [feedbackMessage, setFeedbackMessage] = useState("");
  const [feedbackType, setFeedbackType] = useState("success");
  const [isSaving, setIsSaving] = useState(false);
  const [historyMessage, setHistoryMessage] = useState("");
  const [historyMessageType, setHistoryMessageType] = useState("success");

  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("spendwise-theme") === "dark"
      ? "dark"
      : "light";
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("spendwise-theme", theme);
  }, [theme]);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, selectedType, sortBy, selectedMonth]);

  const fetchTransactions = async () => {
    setIsLoading(true);
    setFetchError("");

    try {
      const response = await fetch("http://localhost:9999/api/transactions");

      if (!response.ok) {
        throw new Error("Failed to fetch transactions");
      }

      const data = await response.json();
      setTransactions(data);
    } catch (error) {
      console.error("Error fetching transactions:", error);
      setFetchError(
        "Unable to load transactions. Please check whether the backend is running.",
      );
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;

    setTransaction((previous) => ({
      ...previous,
      [name]: value,
      ...(name === "type"
        ? {
            category: value === "INCOME" ? "Salary" : "Food",
          }
        : {}),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isSaving) return;

    const isEditing = Boolean(transaction.id);

    const url = isEditing
      ? `http://localhost:9999/api/transactions/${transaction.id}`
      : "http://localhost:9999/api/transactions";

    setIsSaving(true);
    setFeedbackMessage("");

    try {
      const response = await fetch(url, {
        method: isEditing ? "PUT" : "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          type: transaction.type,
          amount: Number(transaction.amount),
          category: transaction.category,
          date: transaction.date,
          description: transaction.description,
        }),
      });

      if (!response.ok) {
        let errorMessage = "Unable to save transaction. Please try again.";

        try {
          const errorData = await response.json();

          if (errorData.errors && typeof errorData.errors === "object") {
            errorMessage = Object.values(errorData.errors).join(" ");
          } else if (errorData.message) {
            errorMessage = errorData.message;
          }
        } catch {
          // Keep the fallback message if the error response isn't JSON.
        }

        throw new Error(errorMessage);
      }

      await fetchTransactions();

      setTransaction({
        type: "EXPENSE",
        amount: "",
        category: "Food",
        date: "",
        description: "",
      });

      setFeedbackType("success");
      setFeedbackMessage(
        isEditing
          ? "Transaction updated successfully!"
          : "Transaction added successfully!",
      );
    } catch (error) {
      console.error("Save error:", error);
      setFeedbackType("error");
      setFeedbackMessage(error.message);
    } finally {
      setIsSaving(false);
    }
  };

  const [monthlyBudgets, setMonthlyBudgets] = useState(() => {
    let savedBudgets = {};

    try {
      const stored = localStorage.getItem("spendwise-monthly-budgets");

      if (stored) {
        const parsed = JSON.parse(stored);

        if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
          savedBudgets = parsed;
        }
      }
    } catch (error) {
      console.error("Unable to load monthly budgets:", error);
    }

    const oldBudget = Number(localStorage.getItem("spendwise-monthly-budget"));

    if (
      savedBudgets[selectedMonth] == null &&
      Number.isFinite(oldBudget) &&
      oldBudget > 0
    ) {
      savedBudgets[selectedMonth] = oldBudget;
    }

    return savedBudgets;
  });

  const monthlyBudget = Number(monthlyBudgets[selectedMonth] ?? 10000);

  useEffect(() => {
    localStorage.setItem(
      "spendwise-monthly-budgets",
      JSON.stringify(monthlyBudgets),
    );
  }, [monthlyBudgets]);

  const handleBudgetChange = (e) => {
    const value = Number(e.target.value);

    setMonthlyBudgets((previous) => ({
      ...previous,
      [selectedMonth]: value,
    }));
  };

  const monthlyTransactions = transactions.filter((item) => {
    return item.date && item.date.startsWith(selectedMonth);
  });

  const monthlyIncome = monthlyTransactions
    .filter((item) => item.type === "INCOME")
    .reduce((total, item) => total + Number(item.amount), 0);

  const monthlyExpenses = monthlyTransactions
    .filter((item) => item.type === "EXPENSE")
    .reduce((total, item) => total + Number(item.amount), 0);

  const availableBalance = monthlyIncome - monthlyExpenses;

  const budgetRemaining = monthlyBudget - monthlyExpenses;

  const budgetProgress =
    monthlyBudget > 0
      ? Math.min((monthlyExpenses / monthlyBudget) * 100, 100)
      : 0;

  const expenseByCategory = monthlyTransactions
    .filter((item) => item.type === "EXPENSE")
    .reduce((result, item) => {
      const category = item.category;
      const amount = Number(item.amount);

      result[category] = (result[category] || 0) + amount;

      return result;
    }, {});

  const chartData = Object.entries(expenseByCategory).map(
    ([category, amount]) => ({
      name: category,
      value: amount,
    }),
  );

  const topExpenseCategory = Object.entries(expenseByCategory).sort(
    (a, b) => b[1] - a[1],
  )[0];

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this transaction?",
    );

    if (!confirmed) {
      return;
    }

    setHistoryMessage("");

    try {
      const response = await fetch(
        `http://localhost:9999/api/transactions/${id}`,
        {
          method: "DELETE",
        },
      );

      if (!response.ok) {
        throw new Error("Failed to delete transaction");
      }

      await fetchTransactions();

      setHistoryMessageType("success");
      setHistoryMessage("Transaction deleted successfully!");
    } catch (error) {
      console.error("Delete error:", error);

      setHistoryMessageType("error");
      setHistoryMessage("Unable to delete transaction. Please try again.");
    }
  };

  const handleEdit = (item) => {
    setTransaction({
      id: item.id,
      type: item.type,
      amount: item.amount,
      category: item.category,
      date: item.date,
      description: item.description,
    });
    formRef.current?.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  };

  const handleCancelEdit = () => {
    setTransaction({
      type: "EXPENSE",
      amount: "",
      category: "Food",
      date: "",
      description: "",
    });

    setFeedbackMessage("");
  };

  const today = getLocalDateString();

  const filteredTransactions = transactions.filter((item) => {
    const search = searchTerm.toLowerCase();

    const matchesSearch =
      (item.category || "").toLowerCase().includes(search) ||
      (item.description || "").toLowerCase().includes(search);

    const matchesType = selectedType === "ALL" || item.type === selectedType;

    const matchesMonth = item.date && item.date.startsWith(selectedMonth);

    return matchesSearch && matchesType && matchesMonth;
  });

  const sortedTransactions = [...filteredTransactions].sort((a, b) => {
    switch (sortBy) {
      case "amountLow":
        return Number(a.amount) - Number(b.amount);

      case "amountHigh":
        return Number(b.amount) - Number(a.amount);

      case "oldest":
        return new Date(a.date) - new Date(b.date);

      case "newest":
        return new Date(b.date) - new Date(a.date);

      default:
        return 0;
    }
  });
  const totalPages = Math.ceil(sortedTransactions.length / itemsPerPage);

  useEffect(() => {
    const lastPage = Math.max(totalPages, 1);

    if (currentPage > lastPage) {
      setCurrentPage(lastPage);
    }
  }, [currentPage, totalPages]);

  const startIndex = (currentPage - 1) * itemsPerPage;

  const paginatedTransactions = sortedTransactions.slice(
    startIndex,
    startIndex + itemsPerPage,
  );

  const handleResetFilters = () => {
    setSearchTerm("");
    setSelectedType("ALL");
    setSortBy("newest");
    setCurrentPage(1);
  };

  const exportToCSV = () => {
    const headers = ["Date", "Description", "Category", "Type", "Amount"];

    const rows = sortedTransactions.map((item) => [
      item.date,
      item.description,
      item.category,
      item.type,
      item.amount,
    ]);

    const csvContent = [headers, ...rows]
      .map((row) =>
        row
          .map((value) => `"${String(value ?? "").replace(/"/g, '""')}"`)
          .join(","),
      )
      .join("\n");

    const blob = new Blob(["\uFEFF" + csvContent], {
      type: "text/csv;charset=utf-8;",
    });

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "spendwise-transactions.csv";
    link.click();

    URL.revokeObjectURL(url);
  };

  return (
    <div className="container">
      <div className="app-heading">
        <div>
          <h1>SpendWise AI</h1>
          <p className="subtitle">Smart Expense & Subscription Intelligence</p>
        </div>

        <button
          type="button"
          className="theme-toggle"
          onClick={() =>
            setTheme((current) => (current === "light" ? "dark" : "light"))
          }
          aria-label={`Switch to ${theme === "light" ? "dark" : "light"} mode`}
        >
          {theme === "light" ? "🌙 Dark Mode" : "☀️ Light Mode"}
        </button>
      </div>

      <div className="month-selector">
        <label htmlFor="selectedMonth">Select Month:</label>

        <input
          id="selectedMonth"
          type="month"
          value={selectedMonth}
          onChange={(e) => setSelectedMonth(e.target.value)}
        />
      </div>

      <div className="summary">
        <div className="card">
          <h3>Income This Month</h3>
          <h2>{formatCurrency(monthlyIncome)}</h2>
        </div>

        <div className="card">
          <h3>Expenses This Month</h3>
          <h2>{formatCurrency(monthlyExpenses)}</h2>
        </div>

        <div className="card">
          <h3>Available Balance</h3>
          <h2>{formatCurrency(availableBalance)}</h2>
        </div>

        <div className="card">
          <h3>Top Expense Category</h3>
          <h2>{topExpenseCategory ? topExpenseCategory[0] : "—"}</h2>
          <p>
            {topExpenseCategory
              ? formatCurrency(topExpenseCategory[1])
              : "No expenses this month"}
          </p>
        </div>
      </div>

      <section className="budget-section">
        <h2>Monthly Budget</h2>

        <label htmlFor="monthlyBudget">Set your budget (₹)</label>

        <input
          id="monthlyBudget"
          type="number"
          min="1"
          value={monthlyBudget}
          onChange={handleBudgetChange}
        />

        <p>Total Expenses: {formatCurrency(monthlyExpenses)}</p>
        <p>Remaining Budget: {formatCurrency(budgetRemaining)}</p>

        <div className="budget-progress">
          <div
            className={`budget-progress-fill ${
              monthlyBudget > 0 && monthlyExpenses > monthlyBudget
                ? "budget-progress-danger"
                : ""
            }`}
            style={{ width: `${budgetProgress}%` }}
          />
        </div>

        <p>{budgetProgress.toFixed(1)}% of budget used</p>

        {monthlyBudget > 0 && monthlyExpenses > monthlyBudget && (
          <p className="budget-warning">
            Warning: You have exceeded your budget!
          </p>
        )}
      </section>

      <div className="form-container" ref={formRef}>
        <h2>{transaction.id ? "Edit Transaction" : "Add Transaction"}</h2>
        {feedbackMessage && (
          <p
            className={`transaction-feedback ${feedbackType}`}
            role={feedbackType === "error" ? "alert" : "status"}
          >
            {feedbackMessage}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          <label>Transaction Type</label>
          <select
            name="type"
            value={transaction.type}
            onChange={handleChange}
            required
          >
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>

          <label>Amount (₹)</label>

          <input
            type="number"
            name="amount"
            min="0.01"
            max="10000000"
            step="0.01"
            value={transaction.amount}
            onChange={handleChange}
            placeholder="Enter amount (max ₹1 crore)"
            required
          />

          <label>Category</label>
          <select
            name="category"
            value={transaction.category}
            onChange={handleChange}
            required
          >
            {transaction.type === "INCOME" ? (
              <>
                <option value="Salary">Salary</option>
                <option value="Investment">Investment</option>
                <option value="Other">Other</option>
              </>
            ) : (
              <>
                <option value="Food">Food</option>
                <option value="Shopping">Shopping</option>
                <option value="Transport">Transport</option>
                <option value="Rent">Rent / Housing</option>
                <option value="Bills">Bills / Utilities</option>
                <option value="Healthcare">Healthcare</option>
                <option value="Education">Education</option>
                <option value="Entertainment">Entertainment</option>
                <option value="Subscriptions">Subscriptions</option>
                <option value="Travel">Travel</option>
                <option value="Other">Other</option>
              </>
            )}
          </select>

          <label>Date</label>
          <input
            type="date"
            name="date"
            value={transaction.date}
            onChange={handleChange}
            max={today}
            required
          />

          <label>Description</label>
          <input
            type="text"
            name="description"
            value={transaction.description}
            onChange={handleChange}
            placeholder="Example: Lunch"
            required
          />

          <button type="submit" disabled={isSaving}>
            {isSaving
              ? "Saving..."
              : transaction.id
                ? "Update Transaction"
                : "Add Transaction"}
          </button>

          <button
            type="button"
            className="clear-form-button"
            onClick={handleCancelEdit}
          >
            {transaction.id ? "Cancel Edit" : "Clear Form"}
          </button>
        </form>
      </div>

      <div className="form-container expense-chart">
        <h2>Expense Breakdown</h2>

        <Suspense fallback={<p>Loading expense chart...</p>}>
          <ExpenseChart chartData={chartData} formatCurrency={formatCurrency} />
        </Suspense>
      </div>

      <div className="form-container transaction-history">
        <div className="search-box">
          <input
            type="text"
            placeholder="Search by category or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          {searchTerm && (
            <button type="button" onClick={() => setSearchTerm("")}>
              Clear Search
            </button>
          )}

          {(searchTerm || selectedType !== "ALL" || sortBy !== "newest") && (
            <button
              type="button"
              className="reset-filters-button"
              onClick={handleResetFilters}
            >
              Reset Filters
            </button>
          )}
        </div>

        <div className="type-filter">
          <label htmlFor="selectedType">Filter by Type:</label>

          <select
            id="selectedType"
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
          >
            <option value="ALL">All Transactions</option>
            <option value="INCOME">Income Only</option>
            <option value="EXPENSE">Expense Only</option>
          </select>
        </div>

        <div className="sort-filter">
          <label htmlFor="sortBy">Sort By:</label>

          <select
            id="sortBy"
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="newest">Newest First</option>
            <option value="oldest">Oldest First</option>
            <option value="amountLow">Amount: Low to High</option>
            <option value="amountHigh">Amount: High to Low</option>
          </select>
        </div>

        <button type="button" className="export-button" onClick={exportToCSV}>
          Export to CSV
        </button>

        <h2>Recent Transactions</h2>

        {historyMessage && (
          <p
            className={`transaction-feedback ${historyMessageType}`}
            role={historyMessageType === "error" ? "alert" : "status"}
          >
            {historyMessage}
          </p>
        )}

        {isLoading ? (
          <p className="empty-state">Loading transactions...</p>
        ) : fetchError ? (
          <div className="empty-state">
            <p>{fetchError}</p>
            <button type="button" onClick={fetchTransactions}>
              Retry
            </button>
          </div>
        ) : sortedTransactions.length === 0 ? (
          <p className="empty-state">
            {transactions.length === 0
              ? "No transactions yet. Add your first transaction above."
              : monthlyTransactions.length === 0
                ? "No transactions for this month yet."
                : "No transactions match your search or filters."}
          </p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Description</th>
                  <th>Category</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {paginatedTransactions.map((item) => (
                  <tr key={item.id}>
                    <td>{formatDate(item.date)}</td>
                    <td>{item.description}</td>
                    <td>{item.category}</td>
                    <td>
                      <span
                        className={
                          item.type === "INCOME"
                            ? "income-badge"
                            : "expense-badge"
                        }
                      >
                        {item.type}
                      </span>
                    </td>

                    <td
                      className={
                        item.type === "INCOME"
                          ? "amount-income"
                          : "amount-expense"
                      }
                    >
                      {item.type === "INCOME" ? "+" : "−"}
                      {formatCurrency(item.amount)}
                    </td>

                    <td>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="delete-button"
                      >
                        Delete
                      </button>
                      <button
                        onClick={() => handleEdit(item)}
                        className="edit-button"
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="pagination">
              <button
                type="button"
                onClick={() => setCurrentPage((page) => page - 1)}
                disabled={currentPage === 1}
              >
                Previous
              </button>

              <span>
                Page {currentPage} of {Math.max(totalPages, 1)}
              </span>

              <button
                type="button"
                onClick={() => setCurrentPage((page) => page + 1)}
                disabled={currentPage >= totalPages}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
