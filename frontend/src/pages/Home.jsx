import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatMoney } from '../utils/format';

export default function Home() {
    const { user } = useAuth();
    const [balance, setBalance] = useState(0);
    const [incomeTotal, setIncomeTotal] = useState(0);
    const [expenseTotal, setExpenseTotal] = useState(0);
    const [baseCurrency, setBaseCurrency] = useState('INR');
    const [recentTx, setRecentTx] = useState([]);
    const [accountCurrencies, setAccountCurrencies] = useState({});
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                const [summaryRes, txRes] = await Promise.all([
                    api.get('/api/accounts/summary'),
                    api.get('/api/transactions/')
                ]);

                // ← SAFE HANDLING: make sure we have arrays
                const summary = summaryRes.data || {};
                const accounts = summary.accounts || [];
                const currencyMap = {};
                accounts.forEach(acc => {
                    if (acc && acc.id) currencyMap[acc.id] = acc.currency || 'INR';
                });
                setAccountCurrencies(currencyMap);
                setAccounts(accounts);
                setBalance(Number(summary.total || 0));
                setBaseCurrency(summary.base || 'INR');

                let transactions = [];
                if (Array.isArray(txRes.data)) {
                    transactions = txRes.data;
                } else if (txRes.data?.content && Array.isArray(txRes.data.content)) {
                    transactions = txRes.data.content;   // common paginated response
                } else if (txRes.data) {
                    transactions = [txRes.data];         // single object fallback
                }

                // Derive income / expense summaries from the full transaction ledger
                let income = 0;
                let expense = 0;
                transactions.forEach(tx => {
                    const amount = Number(tx.amount || 0);
                    if (tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT') {
                        income += amount;
                    } else {
                        expense += amount;
                    }
                });
                setIncomeTotal(income);
                setExpenseTotal(expense);
                setRecentTx(transactions.slice(0, 5));
            } catch (err) {
                console.error("Failed to fetch dashboard data:", err);
                setError("Could not load dashboard data. Please try refreshing.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return (
            <div className="page">
                <div className="flex items-center justify-center py-24 text-sm text-slate-400">
                    Loading dashboard...
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

    const name = user?.username || user?.email || 'User';
    const today = new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' });
    const accountCount = accounts.length;

    return (
        <div className="page">
            {/* Page header */}
            <header className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl text-slate-900">Welcome back, {name}</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        {today} · All figures in {baseCurrency}
                    </p>
                </div>
                <Link to="/transactions" className="btn-primary">New Transaction</Link>
            </header>

            {/* Bento grid */}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-6 lg:gap-6">

                {/* Total balance hero */}
                <div className="relative flex h-full flex-col overflow-hidden rounded-(--radius-card) bg-brand-900 p-6 text-white shadow-card ring-1 ring-slate-900/5 md:col-span-2 lg:col-span-3 lg:row-span-2">
                    {/* Header */}
                    <div className="relative flex items-center justify-between">
                        <p className="text-sm font-medium text-brand-200">Total Balance</p>
                        <span className="badge bg-brand-500/20 text-brand-100 ring-brand-400/40">
                            {baseCurrency}
                        </span>
                    </div>

                    {/* Centered body */}
                    <div className="relative flex flex-1 flex-col justify-center">
                        <p className="text-4xl font-semibold tabular-nums tracking-tight text-white sm:text-5xl">
                            {formatMoney(balance, baseCurrency)}
                        </p>

                        <div className="mt-6 flex flex-wrap items-center gap-x-5 gap-y-2 text-sm text-brand-200/80">
                            <span>{accountCount} {accountCount === 1 ? "account" : "accounts"}</span>
                            <span className="h-3 w-px bg-brand-400/30" />
                            <span>Net position</span>
                            <span className="h-3 w-px bg-brand-400/30" />
                            <span>Updated today</span>
                        </div>
                    </div>
                </div>

                {/* Income */}
                <div className="stat md:col-span-1 lg:col-span-3">
                    <div className="flex items-center justify-between">
                        <p className="stat-label">Income</p>
                        <span className="badge badge-emerald">Credit</span>
                    </div>
                    <p className="stat-value text-brand-700">{formatMoney(incomeTotal, baseCurrency)}</p>
                    <p className="mt-1 text-xs text-slate-400">Earned across all accounts</p>
                </div>

                {/* Expense */}
                <div className="stat md:col-span-1 lg:col-span-3">
                    <div className="flex items-center justify-between">
                        <p className="stat-label">Expenses</p>
                        <span className="badge badge-red">Debit</span>
                    </div>
                    <p className="stat-value text-red-600">{formatMoney(expenseTotal, baseCurrency)}</p>
                    <p className="mt-1 text-xs text-slate-400">Spent across all accounts</p>
                </div>

                {/* Recent transactions */}
                <div className="card md:col-span-2 lg:col-span-4">
                    <div className="mb-4 flex items-center justify-between">
                        <div>
                            <h3 className="card-title">Recent Transactions</h3>
                            <p className="text-sm text-slate-500">Your latest five entries</p>
                        </div>
                        <Link to="/transactions" className="link text-sm">View all</Link>
                    </div>

                    {recentTx.length === 0 ? (
                        <p className="py-6 text-center text-sm text-slate-400">
                            No transactions yet. Add your first one to start tracking.
                        </p>
                    ) : (
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Description</th>
                                    <th className="text-right">Amount</th>
                                    <th className="text-right">Type</th>
                                </tr>
                            </thead>
                            <tbody>
                                {recentTx.map((tx, index) => {
                                    const isIncome = tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT';
                                    return (
                                        <tr key={tx.id || index}>
                                            <td className="whitespace-nowrap text-slate-500">{tx.date || tx.transactionDate || 'N/A'}</td>
                                            <td>{tx.description || 'No description'}</td>
                                            <td className={`text-right ${isIncome ? 'amount-pos' : 'amount-neg'}`}>
                                                {isIncome ? '+' : '-'}
                                                {formatMoney(tx.amount || 0, accountCurrencies[tx.account_id] || baseCurrency)}
                                            </td>
                                            <td className="text-right">
                                                <span className={isIncome ? 'badge badge-emerald' : 'badge badge-red'}>
                                                    {tx.transactionType || 'EXPENSE'}
                                                </span>
                                            </td>
                                        </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    )}
                </div>

                {/* Accounts snapshot */}
                <div className="card md:col-span-2 lg:col-span-2">
                    <div className="mb-4 flex items-center justify-between">
                        <div>
                            <h3 className="card-title">Accounts</h3>
                            <p className="text-sm text-slate-500">{accountCount} linked</p>
                        </div>
                        <Link to="/accounts" className="link text-sm">Manage</Link>
                    </div>

                    {accounts.length === 0 ? (
                        <div className="py-6 text-center">
                            <p className="text-sm text-slate-400">No accounts yet.</p>
                            <Link to="/accounts" className="btn-outline btn-sm mt-4">Create Account</Link>
                        </div>
                    ) : (
                        <ul className="space-y-3">
                            {accounts.map(acc => {
                                const value = Number(acc.balance ?? acc.initial_balance ?? 0);
                                return (
                                    <li key={acc.id} className="flex items-center justify-between gap-3">
                                        <div className="min-w-0">
                                            <p className="truncate text-sm font-medium text-slate-900">
                                                {acc.accountName || acc.account_name || 'Account'}
                                            </p>
                                            <p className="text-xs text-slate-400">{acc.currency || 'INR'}</p>
                                        </div>
                                        <p className={`shrink-0 text-sm ${value >= 0 ? 'amount-pos' : 'amount-neg'}`}>
                                            {formatMoney(value, acc.currency || 'INR')}
                                        </p>
                                    </li>
                                );
                            })}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
}
