import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { Pencil, Plus, Trash2 } from "lucide-react";
import {
  createTransaction,
  deleteTransaction,
  listTransactions,
  updateTransaction,
} from "../api/transactions";
import { listCategories } from "../api/categories";
import { extractErrorMessage } from "../api/errors";
import type {
  CategoryResponse,
  PageResponse,
  TransactionResponse,
  TransactionType,
} from "../types/api";

const inputClass =
  "mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500";

const currencyFormat = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const dateFormat = new Intl.DateTimeFormat(undefined, {
  dateStyle: "medium",
  timeStyle: "short",
});

interface TransactionFormValues {
  type: TransactionType;
  categoryId: string;
  amount: string;
  transactionDate: string;
}

// The backend uses LocalDateTime, so dates travel as zone-less "YYYY-MM-DDTHH:mm" strings.
function toLocalDateTimeInput(date: Date): string {
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}`;
}

function emptyFormValues(): TransactionFormValues {
  return {
    type: "EXPENSE",
    categoryId: "",
    amount: "",
    transactionDate: toLocalDateTimeInput(new Date()),
  };
}

function toRequest(values: TransactionFormValues) {
  return {
    type: values.type,
    categoryId: values.categoryId,
    amount: Number(values.amount),
    transactionDate: values.transactionDate,
  };
}

interface TransactionFieldsProps {
  values: TransactionFormValues;
  categories: CategoryResponse[];
  onChange: (values: TransactionFormValues) => void;
}

function TransactionFields({ values, categories, onChange }: TransactionFieldsProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div>
        <label className="block text-sm font-medium text-slate-700">Type</label>
        <select
          value={values.type}
          onChange={(event) =>
            onChange({ ...values, type: event.target.value as TransactionType })
          }
          required
          className={inputClass}
        >
          <option value="EXPENSE">Expense</option>
          <option value="INCOME">Income</option>
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">Category</label>
        <select
          value={values.categoryId}
          onChange={(event) => onChange({ ...values, categoryId: event.target.value })}
          required
          className={inputClass}
        >
          <option value="" disabled>
            Select a category
          </option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">Amount</label>
        <input
          type="number"
          step="0.01"
          min="0.01"
          required
          value={values.amount}
          onChange={(event) => onChange({ ...values, amount: event.target.value })}
          className={inputClass}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">Date</label>
        <input
          type="datetime-local"
          required
          max={toLocalDateTimeInput(new Date())}
          value={values.transactionDate}
          onChange={(event) => onChange({ ...values, transactionDate: event.target.value })}
          className={inputClass}
        />
      </div>
    </div>
  );
}

export function TransactionsPage() {
  const [page, setPage] = useState<PageResponse<TransactionResponse> | null>(null);
  const [pageIndex, setPageIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [categories, setCategories] = useState<CategoryResponse[]>([]);

  const [isAdding, setIsAdding] = useState(false);
  const [createValues, setCreateValues] = useState<TransactionFormValues>(emptyFormValues);
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editValues, setEditValues] = useState<TransactionFormValues>(emptyFormValues);
  const [editError, setEditError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<string | null>(null);

  const loadTransactions = useCallback(async (targetPage: number) => {
    setIsLoading(true);
    setError(null);
    try {
      setPage(await listTransactions(targetPage));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTransactions(pageIndex);
  }, [pageIndex, loadTransactions]);

  useEffect(() => {
    listCategories(0, 100)
      .then((data) => setCategories(data.content))
      .catch(() => setCategories([]));
  }, []);

  function toggleAdding() {
    setIsAdding((value) => !value);
    setCreateValues(emptyFormValues());
    setFormError(null);
  }

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);
    setIsSubmitting(true);
    try {
      await createTransaction(toRequest(createValues));
      setIsAdding(false);
      setPageIndex(0);
      await loadTransactions(0);
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  function startEdit(transaction: TransactionResponse) {
    setEditingId(transaction.id);
    setEditValues({
      type: transaction.type,
      categoryId: transaction.categoryId ?? "",
      amount: String(transaction.amount),
      transactionDate: transaction.transactionDate.slice(0, 16),
    });
    setEditError(null);
  }

  function cancelEdit() {
    setEditingId(null);
    setEditError(null);
  }

  async function handleUpdate(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    setEditError(null);
    setIsSubmitting(true);
    try {
      await updateTransaction(id, toRequest(editValues));
      setEditingId(null);
      await loadTransactions(pageIndex);
    } catch (err) {
      setEditError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm("Delete this transaction? This can't be undone.")) {
      return;
    }
    setDeletingId(id);
    setError(null);
    try {
      await deleteTransaction(id);
      const isLastItemOnPage = page?.content.length === 1 && pageIndex > 0;
      if (isLastItemOnPage) {
        setPageIndex((current) => current - 1);
      } else {
        await loadTransactions(pageIndex);
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setDeletingId(null);
    }
  }

  const hasCategories = categories.length > 0;

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Transactions</h1>
          <p className="mt-1 text-sm text-slate-500">Record your income and expenses.</p>
        </div>
        <button
          type="button"
          onClick={toggleAdding}
          disabled={!hasCategories}
          title={hasCategories ? undefined : "Add a category first"}
          className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <Plus size={16} />
          Add transaction
        </button>
      </div>

      {!hasCategories && (
        <p className="mb-6 text-sm text-slate-500">
          You don't have any categories yet.{" "}
          <Link to="/app/categories" className="font-medium text-brand-600 hover:text-brand-700">
            Create one
          </Link>{" "}
          before adding a transaction.
        </p>
      )}

      {isAdding && (
        <form
          onSubmit={handleCreate}
          className="mb-6 rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
        >
          <TransactionFields
            values={createValues}
            categories={categories}
            onChange={setCreateValues}
          />

          {formError && <p className="mt-3 text-sm text-red-600">{formError}</p>}

          <div className="mt-4 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setIsAdding(false)}
              className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isSubmitting ? "Adding…" : "Add transaction"}
            </button>
          </div>
        </form>
      )}

      {isLoading ? (
        <p className="text-sm text-slate-500">Loading transactions…</p>
      ) : error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : !page || page.content.length === 0 ? (
        <p className="text-sm text-slate-500">
          No transactions yet. Add one to start tracking your money.
        </p>
      ) : (
        <div className="space-y-3">
          {page.content.map((transaction) =>
            editingId === transaction.id ? (
              <form
                key={transaction.id}
                onSubmit={(event) => handleUpdate(event, transaction.id)}
                className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-brand-200"
              >
                <TransactionFields
                  values={editValues}
                  categories={categories}
                  onChange={setEditValues}
                />

                {editError && <p className="mt-3 text-sm text-red-600">{editError}</p>}

                <div className="mt-4 flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={cancelEdit}
                    className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={isSubmitting}
                    className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {isSubmitting ? "Saving…" : "Save"}
                  </button>
                </div>
              </form>
            ) : (
              <div
                key={transaction.id}
                className="flex items-center justify-between rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
              >
                <div>
                  <p className="font-medium text-slate-900">
                    {transaction.categoryName ?? "Uncategorized"}
                  </p>
                  <p className="mt-0.5 text-sm text-slate-500">
                    {dateFormat.format(new Date(transaction.transactionDate))}
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <p
                    className={`font-semibold ${
                      transaction.type === "INCOME" ? "text-emerald-600" : "text-red-600"
                    }`}
                  >
                    {transaction.type === "INCOME" ? "+" : "−"}
                    {currencyFormat.format(transaction.amount)}
                  </p>
                  <div className="flex items-center gap-1">
                    <button
                      type="button"
                      onClick={() => startEdit(transaction)}
                      title="Edit"
                      className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(transaction.id)}
                      disabled={deletingId === transaction.id}
                      title="Delete"
                      className="rounded-lg p-2 text-slate-500 hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>
              </div>
            ),
          )}
        </div>
      )}

      {page && page.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-between text-sm text-slate-600">
          <button
            type="button"
            onClick={() => setPageIndex((current) => current - 1)}
            disabled={page.first}
            className="rounded-lg px-3 py-1.5 font-medium hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Previous
          </button>
          <span>
            Page {page.number + 1} of {page.totalPages}
          </span>
          <button
            type="button"
            onClick={() => setPageIndex((current) => current + 1)}
            disabled={page.last}
            className="rounded-lg px-3 py-1.5 font-medium hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
