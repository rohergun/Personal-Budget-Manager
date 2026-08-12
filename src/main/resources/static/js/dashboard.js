import { apiRequest } from "./api.js";
import { requireAuth, logout } from "./auth.js";

requireAuth();

const errorAlert = document.getElementById("errorAlert");
const userNameEl = document.getElementById("userName");
const totalIncomeEl = document.getElementById("totalIncome");
const totalExpensesEl = document.getElementById("totalExpenses");
const netEl = document.getElementById("net");
const categoryListEl = document.getElementById("categoryList");

document.getElementById("logoutBtn").addEventListener("click", logout);

function formatMoney(amount) {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(amount);
}

function renderCategoryRow(category) {
    const hasBudget = category.budgetLimit !== null && category.budgetLimit !== undefined;
    const percent = hasBudget
        ? Math.min(100, Math.round((category.spent / category.budgetLimit) * 100))
        : 0;
    const barColor = hasBudget && category.spent > category.budgetLimit ? "bg-danger" : "bg-primary";

    const amountLabel = hasBudget
        ? `${formatMoney(category.spent)} of ${formatMoney(category.budgetLimit)}`
        : `${formatMoney(category.spent)} (no budget)`;

    const row = document.createElement("div");
    row.className = "mb-3";
    row.innerHTML = `
        <div class="d-flex justify-content-between small mb-1">
            <span>${category.categoryName}</span>
            <span class="text-muted">${amountLabel}</span>
        </div>
        <div class="progress" style="height: 6px;">
            <div class="progress-bar ${barColor}" style="width: ${hasBudget ? percent : 0}%"></div>
        </div>
    `;
    return row;
}

async function loadDashboard() {
    try {
        const [user, summary] = await Promise.all([
            apiRequest("/users/me"),
            apiRequest("/summaries/monthly"),
        ]);

        userNameEl.textContent = user.name;

        totalIncomeEl.textContent = formatMoney(summary.totalIncome);
        totalExpensesEl.textContent = formatMoney(summary.totalExpenses);
        netEl.textContent = formatMoney(summary.net);

        categoryListEl.innerHTML = "";

        if (summary.byCategory.length === 0) {
            categoryListEl.innerHTML = '<p class="text-muted small mb-0">No spending or budgets yet this month.</p>';
            return;
        }

        summary.byCategory.forEach((category) => {
            categoryListEl.appendChild(renderCategoryRow(category));
        });
    } catch (err) {
        errorAlert.textContent = err.message;
        errorAlert.classList.remove("d-none");
    }
}

loadDashboard();