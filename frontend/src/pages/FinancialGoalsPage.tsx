import { useCallback, useEffect, useState, type FormEvent } from "react";
import { CalendarDays, Pencil, PiggyBank, Plus, Trash2 } from "lucide-react";
import {
  contributeToFinancialGoal,
  createFinancialGoal,
  deleteFinancialGoal,
  listFinancialGoals,
  updateFinancialGoal,
} from "../api/financialGoals";
import { extractErrorMessage } from "../api/errors";
import type { FinancialGoalResponse, PageResponse } from "../types/api";

const inputClass =
  "mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500";

const currencyFormat = new Intl.NumberFormat(undefined, {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const dateFormat = new Intl.DateTimeFormat(undefined, { dateStyle: "medium" });

interface GoalFormValues {
  name: string;
  description: string;
  targetAmount: string;
  deadline: string;
}

function todayInputValue(): string {
  const now = new Date();
  const pad = (value: number) => String(value).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
}

const emptyFormValues: GoalFormValues = {
  name: "",
  description: "",
  targetAmount: "",
  deadline: "",
};

// The backend deadline is a LocalDateTime validated with @Future, so a picked date
// is sent as the end of that day to keep "today" a valid choice.
function toRequest(values: GoalFormValues) {
  const description = values.description.trim();
  return {
    name: values.name.trim(),
    description: description || undefined,
    targetAmount: Number(values.targetAmount),
    deadline: `${values.deadline}T23:59:59`,
  };
}

interface GoalFieldsProps {
  values: GoalFormValues;
  onChange: (values: GoalFormValues) => void;
}

function GoalFields({ values, onChange }: GoalFieldsProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <div>
        <label className="block text-sm font-medium text-slate-700">Name</label>
        <input
          type="text"
          required
          minLength={2}
          maxLength={50}
          value={values.name}
          onChange={(event) => onChange({ ...values, name: event.target.value })}
          className={inputClass}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">Target amount</label>
        <input
          type="number"
          step="0.01"
          min="0.01"
          required
          value={values.targetAmount}
          onChange={(event) => onChange({ ...values, targetAmount: event.target.value })}
          className={inputClass}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">Deadline</label>
        <input
          type="date"
          required
          min={todayInputValue()}
          value={values.deadline}
          onChange={(event) => onChange({ ...values, deadline: event.target.value })}
          className={inputClass}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-slate-700">
          Description <span className="font-normal text-slate-400">(optional)</span>
        </label>
        <input
          type="text"
          maxLength={255}
          value={values.description}
          onChange={(event) => onChange({ ...values, description: event.target.value })}
          className={inputClass}
        />
      </div>
    </div>
  );
}

function GoalProgress({ goal }: { goal: FinancialGoalResponse }) {
  const saved = Math.min(goal.currentAmount, goal.targetAmount);
  const left = Math.max(goal.targetAmount - goal.currentAmount, 0);
  const percent = goal.targetAmount > 0 ? (saved / goal.targetAmount) * 100 : 0;

  return (
    <div>
      <div
        role="progressbar"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(percent)}
        className="flex h-3 w-full overflow-hidden rounded-full bg-red-100"
      >
        <div
          className="h-full rounded-full bg-emerald-500 transition-all duration-500"
          style={{ width: `${percent}%` }}
        />
      </div>
      <div className="mt-2 flex items-center justify-between text-sm">
        <span className="font-medium text-emerald-700">
          {currencyFormat.format(goal.currentAmount)} saved
        </span>
        <span className="text-slate-500">{Math.floor(percent)}%</span>
        <span className="font-medium text-red-500">{currencyFormat.format(left)} left</span>
      </div>
      <div className="mt-2 flex h-2 w-full overflow-hidden rounded-full bg-slate-200">
        <div
          className="h-full rounded-full bg-emerald-500 transition-all duration-500"
          style={{ width: `${percent}%` }}
        />
      </div>
    </div>
  );
}

export function FinancialGoalsPage() {
  const [page, setPage] = useState<PageResponse<FinancialGoalResponse> | null>(null);
  const [pageIndex, setPageIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isAdding, setIsAdding] = useState(false);
  const [createValues, setCreateValues] = useState<GoalFormValues>(emptyFormValues);
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editValues, setEditValues] = useState<GoalFormValues>(emptyFormValues);
  const [editError, setEditError] = useState<string | null>(null);

  const [contributingId, setContributingId] = useState<string | null>(null);
  const [contributionAmount, setContributionAmount] = useState("");
  const [contributionError, setContributionError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<string | null>(null);

  const loadGoals = useCallback(async (targetPage: number) => {
    setIsLoading(true);
    setError(null);
    try {
      setPage(await listFinancialGoals(targetPage));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadGoals(pageIndex);
  }, [pageIndex, loadGoals]);

  function toggleAdding() {
    setIsAdding((value) => !value);
    setCreateValues(emptyFormValues);
    setFormError(null);
  }

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);
    setIsSubmitting(true);
    try {
      await createFinancialGoal(toRequest(createValues));
      setIsAdding(false);
      setPageIndex(0);
      await loadGoals(0);
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  function startEdit(goal: FinancialGoalResponse) {
    setContributingId(null);
    setEditingId(goal.id);
    setEditValues({
      name: goal.name,
      description: goal.description ?? "",
      targetAmount: String(goal.targetAmount),
      deadline: goal.deadline.slice(0, 10),
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
      await updateFinancialGoal(id, toRequest(editValues));
      setEditingId(null);
      await loadGoals(pageIndex);
    } catch (err) {
      setEditError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  function startContribution(id: string) {
    setContributingId((current) => (current === id ? null : id));
    setContributionAmount("");
    setContributionError(null);
  }

  async function handleContribute(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    setContributionError(null);
    setIsSubmitting(true);
    try {
      const updated = await contributeToFinancialGoal(id, {
        amount: Number(contributionAmount),
      });
      setPage((current) =>
        current && {
          ...current,
          content: current.content.map((goal) => (goal.id === id ? updated : goal)),
        },
      );
      setContributingId(null);
    } catch (err) {
      setContributionError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm("Delete this goal? This can't be undone.")) {
      return;
    }
    setDeletingId(id);
    setError(null);
    try {
      await deleteFinancialGoal(id);
      const isLastItemOnPage = page?.content.length === 1 && pageIndex > 0;
      if (isLastItemOnPage) {
        setPageIndex((current) => current - 1);
      } else {
        await loadGoals(pageIndex);
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Financial Goals</h1>
          <p className="mt-1 text-sm text-slate-500">
            Save towards what matters and track how close you are.
          </p>
        </div>
        <button
          type="button"
          onClick={toggleAdding}
          className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600"
        >
          <Plus size={16} />
          Add goal
        </button>
      </div>

      {isAdding && (
        <form
          onSubmit={handleCreate}
          className="mb-6 rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
        >
          <GoalFields values={createValues} onChange={setCreateValues} />

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
              {isSubmitting ? "Adding…" : "Add goal"}
            </button>
          </div>
        </form>
      )}

      {isLoading ? (
        <p className="text-sm text-slate-500">Loading goals…</p>
      ) : error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : !page || page.content.length === 0 ? (
        <p className="text-sm text-slate-500">
          No goals yet. Add one to start saving towards it.
        </p>
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {page.content.map((goal) => {
            const isCompleted = goal.currentAmount >= goal.targetAmount;
            const isOverdue = !isCompleted && new Date(goal.deadline) < new Date();

            if (editingId === goal.id) {
              return (
                <form
                  key={goal.id}
                  onSubmit={(event) => handleUpdate(event, goal.id)}
                  className="rounded-lg bg-white p-5 shadow-sm ring-1 ring-brand-200"
                >
                  <GoalFields values={editValues} onChange={setEditValues} />

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
              );
            }

            return (
              <div
                key={goal.id}
                className="rounded-lg bg-white p-5 shadow-sm ring-1 ring-slate-100"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <p className="truncate font-medium text-slate-900">{goal.name}</p>
                      {isCompleted && (
                        <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-emerald-700">
                          Completed
                        </span>
                      )}
                      {isOverdue && (
                        <span className="rounded-full bg-red-50 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-red-600">
                          Overdue
                        </span>
                      )}
                    </div>
                    {goal.description && (
                      <p className="mt-0.5 truncate text-sm text-slate-500">{goal.description}</p>
                    )}
                  </div>
                  <div className="flex shrink-0 items-center gap-1">
                    <button
                      type="button"
                      onClick={() => startEdit(goal)}
                      title="Edit"
                      className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
                    >
                      <Pencil size={16} />
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(goal.id)}
                      disabled={deletingId === goal.id}
                      title="Delete"
                      className="rounded-lg p-2 text-slate-500 hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                </div>

                <div className="mt-4">
                  <GoalProgress goal={goal} />
                </div>

                <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                  <span className="flex items-center gap-1.5">
                    <CalendarDays size={14} />
                    {dateFormat.format(new Date(goal.deadline))}
                    <span className="text-slate-300">·</span>
                    Target {currencyFormat.format(goal.targetAmount)}
                  </span>
                  {!isCompleted && (
                    <button
                      type="button"
                      onClick={() => startContribution(goal.id)}
                      className="flex items-center gap-1.5 rounded-lg px-3 py-1.5 font-medium text-brand-600 hover:bg-brand-50"
                    >
                      <PiggyBank size={16} />
                      Add money
                    </button>
                  )}
                </div>

                {contributingId === goal.id && (
                  <form
                    onSubmit={(event) => handleContribute(event, goal.id)}
                    className="mt-3 border-t border-slate-100 pt-3"
                  >
                    <div className="flex items-end gap-2">
                      <div className="flex-1">
                        <label className="block text-sm font-medium text-slate-700">Amount</label>
                        <input
                          type="number"
                          step="0.01"
                          min="0.01"
                          required
                          autoFocus
                          value={contributionAmount}
                          onChange={(event) => setContributionAmount(event.target.value)}
                          className={inputClass}
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => setContributingId(null)}
                        className="rounded-lg px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                      >
                        Cancel
                      </button>
                      <button
                        type="submit"
                        disabled={isSubmitting}
                        className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        {isSubmitting ? "Adding…" : "Add"}
                      </button>
                    </div>
                    {contributionError && (
                      <p className="mt-2 text-sm text-red-600">{contributionError}</p>
                    )}
                  </form>
                )}
              </div>
            );
          })}
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
