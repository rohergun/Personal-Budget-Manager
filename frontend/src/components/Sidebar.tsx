import {
  LayoutDashboard,
  ArrowLeftRight,
  Wallet,
  Tags,
  Target,
  LogOut,
} from "lucide-react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface NavItem {
  label: string;
  to: string;
  icon: typeof LayoutDashboard;
  enabled: boolean;
}

const NAV_ITEMS: NavItem[] = [
  { label: "Dashboard", to: "/app/dashboard", icon: LayoutDashboard, enabled: true },
  { label: "Transactions", to: "/app/transactions", icon: ArrowLeftRight, enabled: true },
  { label: "Budgets", to: "/app/budgets", icon: Wallet, enabled: true },
  { label: "Categories", to: "/app/categories", icon: Tags, enabled: true },
  { label: "Financial Goals", to: "/app/goals", icon: Target, enabled: false },
];

export function Sidebar() {
  const { user, logout } = useAuth();

  return (
    <aside className="flex h-screen w-64 shrink-0 flex-col border-r border-slate-200 bg-white">
      <div className="flex items-center gap-2 px-6 py-5">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-bold text-white">
          B
        </div>
        <span className="text-lg font-semibold text-slate-900">BudgetManager</span>
      </div>

      <nav className="flex-1 space-y-1 px-3">
        {NAV_ITEMS.map(({ label, to, icon: Icon, enabled }) =>
          enabled ? (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-brand-50 text-brand-700"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ) : (
            <div
              key={to}
              title="Coming soon"
              className="flex cursor-not-allowed items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-slate-400"
            >
              <Icon size={18} />
              {label}
              <span className="ml-auto rounded-full bg-slate-100 px-2 py-0.5 text-[10px] font-semibold uppercase tracking-wide text-slate-400">
                Soon
              </span>
            </div>
          ),
        )}
      </nav>

      <div className="border-t border-slate-200 px-3 py-4">
        <div className="mb-2 px-3">
          <p className="truncate text-sm font-medium text-slate-900">
            {user ? `${user.name} ${user.surname}` : "…"}
          </p>
          <p className="truncate text-xs text-slate-500">{user?.email}</p>
        </div>
        <button
          type="button"
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900"
        >
          <LogOut size={18} />
          Log out
        </button>
      </div>
    </aside>
  );
}
