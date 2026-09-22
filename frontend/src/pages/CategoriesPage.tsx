import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { createCategory, deleteCategory, listCategories, updateCategory } from "../api/categories";
import { extractErrorMessage } from "../api/errors";
import type { CategoryResponse, PageResponse } from "../types/api";

const inputClass =
  "mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500";

export function CategoriesPage() {
  const [page, setPage] = useState<PageResponse<CategoryResponse> | null>(null);
  const [pageIndex, setPageIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [isAdding, setIsAdding] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState("");
  const [editDescription, setEditDescription] = useState("");
  const [editError, setEditError] = useState<string | null>(null);

  const [deletingId, setDeletingId] = useState<string | null>(null);

  const loadCategories = useCallback(async (targetPage: number) => {
    setIsLoading(true);
    setError(null);
    try {
      setPage(await listCategories(targetPage));
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadCategories(pageIndex);
  }, [pageIndex, loadCategories]);

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    const name = String(data.get("name") ?? "").trim();
    const description = String(data.get("description") ?? "").trim();

    setFormError(null);
    setIsSubmitting(true);
    try {
      await createCategory({ name, description: description || undefined });
      form.reset();
      setIsAdding(false);
      setPageIndex(0);
      await loadCategories(0);
    } catch (err) {
      setFormError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  function startEdit(category: CategoryResponse) {
    setEditingId(category.id);
    setEditName(category.name);
    setEditDescription(category.description ?? "");
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
      const description = editDescription.trim();
      await updateCategory(id, { name: editName.trim(), description: description || undefined });
      setEditingId(null);
      await loadCategories(pageIndex);
    } catch (err) {
      setEditError(extractErrorMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm("Delete this category? This can't be undone.")) {
      return;
    }
    setDeletingId(id);
    setError(null);
    try {
      await deleteCategory(id);
      const isLastItemOnPage = page?.content.length === 1 && pageIndex > 0;
      if (isLastItemOnPage) {
        setPageIndex((current) => current - 1);
      } else {
        await loadCategories(pageIndex);
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
          <h1 className="text-2xl font-semibold text-slate-900">Categories</h1>
          <p className="mt-1 text-sm text-slate-500">Organize your transactions into categories.</p>
        </div>
        <button
          type="button"
          onClick={() => {
            setIsAdding((value) => !value);
            setFormError(null);
          }}
          className="flex items-center gap-2 rounded-lg bg-brand-500 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-600"
        >
          <Plus size={16} />
          Add category
        </button>
      </div>

      {isAdding && (
        <form
          onSubmit={handleCreate}
          className="mb-6 rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
        >
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-slate-700">
                Name
              </label>
              <input id="name" name="name" required minLength={2} maxLength={20} className={inputClass} />
            </div>
            <div>
              <label htmlFor="description" className="block text-sm font-medium text-slate-700">
                Description (optional)
              </label>
              <input
                id="description"
                name="description"
                minLength={3}
                maxLength={100}
                className={inputClass}
              />
            </div>
          </div>

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
              {isSubmitting ? "Adding…" : "Add category"}
            </button>
          </div>
        </form>
      )}

      {isLoading ? (
        <p className="text-sm text-slate-500">Loading categories…</p>
      ) : error ? (
        <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : !page || page.content.length === 0 ? (
        <p className="text-sm text-slate-500">
          No categories yet. Add your first one to start organizing transactions.
        </p>
      ) : (
        <div className="space-y-3">
          {page.content.map((category) =>
            editingId === category.id ? (
              <form
                key={category.id}
                onSubmit={(event) => handleUpdate(event, category.id)}
                className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-brand-200"
              >
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="block text-sm font-medium text-slate-700">Name</label>
                    <input
                      value={editName}
                      onChange={(event) => setEditName(event.target.value)}
                      required
                      minLength={2}
                      maxLength={20}
                      className={inputClass}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700">
                      Description (optional)
                    </label>
                    <input
                      value={editDescription}
                      onChange={(event) => setEditDescription(event.target.value)}
                      minLength={3}
                      maxLength={100}
                      className={inputClass}
                    />
                  </div>
                </div>

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
                key={category.id}
                className="flex items-center justify-between rounded-lg bg-white p-4 shadow-sm ring-1 ring-slate-100"
              >
                <div>
                  <p className="font-medium text-slate-900">{category.name}</p>
                  {category.description && (
                    <p className="mt-0.5 text-sm text-slate-500">{category.description}</p>
                  )}
                </div>
                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={() => startEdit(category)}
                    title="Edit"
                    className="rounded-lg p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-900"
                  >
                    <Pencil size={16} />
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(category.id)}
                    disabled={deletingId === category.id}
                    title="Delete"
                    className="rounded-lg p-2 text-slate-500 hover:bg-red-50 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    <Trash2 size={16} />
                  </button>
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
