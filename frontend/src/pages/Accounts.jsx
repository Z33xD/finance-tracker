import { useEffect, useState } from 'react';
import api from '../services/api';
import { CURRENCIES } from '../constants/currencies';
import { formatMoney } from '../utils/format';

const ACCOUNT_TYPE_BADGE = {
    SAVINGS: 'badge-emerald',
    CHECKING: 'badge-slate',
    CREDIT: 'badge-red',
    INVESTMENT: 'bg-brand-50 text-brand-700 ring-brand-600/20',
};

export default function Accounts() {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        account_name: '',
        initial_balance: 0,
        currency: 'INR',
        account_type: 'SAVINGS'
    });

    const fetchAccounts = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await api.get('/api/accounts/');

            let accountList = [];
            if (Array.isArray(res.data)) {
                accountList = res.data;
            } else if (res.data?.content && Array.isArray(res.data.content)) {
                accountList = res.data.content;
            } else if (res.data) {
                accountList = [res.data];
            }

            setAccounts(accountList);
        } catch (err) {
            console.error("Failed to fetch accounts:", err);
            setError("Failed to load accounts. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.account_name.trim()) {
            alert("Account name is required");
            return;
        }

        try {
            await api.post('/api/accounts/', form);
            setForm({
                account_name: '',
                initial_balance: 0,
                currency: 'INR',
                account_type: 'SAVINGS'
            });
            fetchAccounts(); // refresh list
            alert("Account created successfully!");
        } catch (err) {
            alert("Failed to create account: " + (err.response?.data?.message || err.message));
        }
    };

    const deleteAccount = async (id) => {
        if (!window.confirm("Delete this account and all associated transactions?")) return;

        try {
            await api.delete(`/api/accounts/${id}`);
            fetchAccounts();
            alert("Account deleted successfully!");
        } catch (err) {
            alert("Failed to delete account: " + (err.response?.data?.message || "Unknown error"));
        }
    };

    if (loading) {
        return (
            <div className="page">
                <div className="flex items-center justify-center py-24 text-sm text-slate-400">
                    Loading accounts...
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page">
                <div className="rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-600/20">
                    {error}
                </div>
            </div>
        );
    }

    return (
        <div className="page">
            <header className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl text-slate-900">My Accounts</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Balances are shown in each account's native currency
                    </p>
                </div>
                <span className="badge badge-slate w-fit">{accounts.length} linked</span>
            </header>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                {/* Create New Account form */}
                <div className="card h-fit lg:col-span-1">
                    <h2 className="card-title">Create New Account</h2>
                    <p className="card-subtitle">Link a savings, checking, credit or investment account</p>

                    <form onSubmit={handleSubmit}>
                        <div className="field">
                            <label htmlFor="account_name" className="label">Account Name</label>
                            <input
                                id="account_name"
                                placeholder="e.g. Savings Account, Credit Card"
                                value={form.account_name}
                                onChange={(e) => setForm({ ...form, account_name: e.target.value })}
                                required
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="initial_balance" className="label">Initial Balance</label>
                            <input
                                id="initial_balance"
                                type="number"
                                step="0.01"
                                placeholder="0.00"
                                value={form.initial_balance}
                                onChange={(e) => setForm({ ...form, initial_balance: parseFloat(e.target.value) || 0 })}
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="currency" className="label">Currency</label>
                            <select
                                id="currency"
                                value={form.currency}
                                onChange={(e) => setForm({ ...form, currency: e.target.value })}
                                className="input"
                            >
                                {CURRENCIES.map(c => (
                                    <option key={c.code} value={c.code}>{c.code} — {c.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="field">
                            <label htmlFor="account_type" className="label">Account Type</label>
                            <select
                                id="account_type"
                                value={form.account_type}
                                onChange={(e) => setForm({ ...form, account_type: e.target.value })}
                                className="input"
                            >
                                <option value="SAVINGS">Savings</option>
                                <option value="CHECKING">Checking / Current</option>
                                <option value="CREDIT">Credit Card</option>
                                <option value="INVESTMENT">Investment</option>
                            </select>
                        </div>

                        <button type="submit" className="btn-primary w-full">Create Account</button>
                    </form>
                </div>

                {/* Account card matrix */}
                <div className="lg:col-span-2">
                    <h2 className="mb-4 text-lg font-semibold text-slate-900">Your Accounts</h2>

                    {accounts.length === 0 ? (
                        <div className="card flex flex-col items-center justify-center py-16 text-center">
                            <p className="text-sm font-medium text-slate-600">No accounts found.</p>
                            <p className="mt-1 text-sm text-slate-400">
                                Create your first account above to start tracking transactions.
                            </p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                            {accounts.map(acc => {
                                const value = Number(acc.balance ?? acc.initial_balance ?? 0);
                                const badge = ACCOUNT_TYPE_BADGE[acc.account_type] || 'badge-slate';
                                return (
                                    <div key={acc.id || acc.account_name} className="card flex flex-col p-5 transition-shadow duration-200 hover:shadow-pop">
                                        <div className="flex items-start justify-between gap-3">
                                            <div className="min-w-0">
                                                <p className="truncate font-semibold text-slate-900">{acc.account_name}</p>
                                                <p className="mt-0.5 text-xs text-slate-400">{acc.currency || 'INR'}</p>
                                            </div>
                                            <span className={`badge shrink-0 ${badge}`}>{acc.account_type || '—'}</span>
                                        </div>

                                        <div className="mt-6">
                                            <p className="stat-label">Balance</p>
                                            <p className={`mt-1 text-2xl font-semibold tabular-nums tracking-tight ${value >= 0 ? 'text-brand-700' : 'text-red-600'}`}>
                                                {formatMoney(value, acc.currency || 'INR')}
                                            </p>
                                        </div>

                                        <button
                                            type="button"
                                            className="btn-danger btn-sm mt-5 w-full"
                                            onClick={() => deleteAccount(acc.id)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
