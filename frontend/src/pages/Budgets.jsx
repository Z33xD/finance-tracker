import { useEffect, useState } from 'react';
import api from '../services/api';
import { Pie } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    ArcElement,
    Tooltip,
    Legend,
} from 'chart.js';
import { CURRENCIES } from '../constants/currencies';
import { formatMoney } from '../utils/format';

ChartJS.register(ArcElement, Tooltip, Legend);

export default function Budgets() {
    const [summaries, setSummaries] = useState([]); // { id, categoryId, currency, amount, spent, remaining }
    const [budgets, setBudgets] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        category_id: '',
        amount: 0,
        currency: 'INR',
        month: new Date().getMonth() + 1,
        year: new Date().getFullYear(),
    });

    const fetchData = async () => {
        setLoading(true);
        setError(null);
        try {
            // Fetch budget summaries (amount + converted spent per budget)
            const [summaryRes, catRes, accountRes, budgetRes] = await Promise.all([
                api.get('/api/budgets/summary'),
                api.get('/api/categories/'),
                api.get('/api/accounts/summary'),
                api.get('/api/budgets/'),
            ]);

            let summaryList = [];
            if (Array.isArray(summaryRes.data)) summaryList = summaryRes.data;
            else if (summaryRes.data?.content && Array.isArray(summaryRes.data.content)) summaryList = summaryRes.data.content;
            else if (summaryRes.data) summaryList = [summaryRes.data];
            setSummaries(summaryList);

            let budgetList = [];
            if (Array.isArray(budgetRes.data)) budgetList = budgetRes.data;
            else if (budgetRes.data?.content && Array.isArray(budgetRes.data.content)) budgetList = budgetRes.data.content;
            else if (budgetRes.data) budgetList = [budgetRes.data];
            setBudgets(budgetList);

            let categoryList = [];
            if (Array.isArray(catRes.data)) categoryList = catRes.data;
            else if (catRes.data?.content && Array.isArray(catRes.data.content)) categoryList = catRes.data.content;
            else if (catRes.data) categoryList = [catRes.data];
            setCategories(categoryList);

            const base = accountRes.data?.base || 'INR';
            setForm(prev => ({ ...prev, currency: base }));
        } catch (err) {
            console.error(err);
            setError("Failed to load budgets");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.category_id || form.amount <= 0) {
            alert("Please select a valid category and enter an amount");
            return;
        }

        const duplicate = budgets.find(b =>
            String(b.category_id) === String(form.category_id) &&
            b.month === form.month &&
            b.year === form.year
        );
        if (duplicate) {
            alert("A budget for this category and month already exists.");
            return;
        }

        try {
            await api.post('/api/budgets/', {
                ...form,
                category_id: parseInt(form.category_id),
            });
            setForm({
                category_id: '',
                amount: 0,
                currency: 'INR',
                month: new Date().getMonth() + 1,
                year: new Date().getFullYear(),
            });
            fetchData();
            alert("Budget added successfully!");
        } catch (err) {
            alert("Failed to add budget: " + (err.response?.data?.message || err.message));
        }
    };

    // Category name lookup for the budget list / chart
    const getCategoryName = (categoryId) => {
        if (!categoryId) return null;
        const cat = categories.find(c => String(c.id) === String(categoryId));
        return cat ? cat.name : null;
    };

    // Prepare data for Pie Chart
    const chartData = {
        labels: summaries.map(s => getCategoryName(s.categoryId) || `Category #${s.categoryId}`),
        datasets: [{
            data: summaries.map(s => s.amount || 0),
            backgroundColor: [
                '#1f8455', '#2e9e6e', '#86cda7', '#4fb284',
                '#b8e3c9', '#144630', '#0a271a'
            ],
            borderWidth: 1,
        }],
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { position: 'bottom' },
        },
    };

    if (loading) {
        return (
            <div className="page">
                <div className="flex items-center justify-center py-24 text-sm text-slate-400">
                    Loading budgets...
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
            <header className="mb-6">
                <h1 className="text-2xl text-slate-900">Budgets</h1>
                <p className="mt-1 text-sm text-slate-500">
                    Set spending limits per category and track live progress
                </p>
            </header>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-5">
                {/* Set New Budget form */}
                <div className="card h-fit lg:col-span-2">
                    <h2 className="card-title">Set New Budget</h2>
                    <p className="card-subtitle">Allocate a monthly limit per category</p>

                    <form onSubmit={handleSubmit}>
                        <div className="field">
                            <label htmlFor="category_id" className="label">Category</label>
                            <select
                                id="category_id"
                                value={form.category_id}
                                onChange={e => setForm({ ...form, category_id: e.target.value })}
                                required
                                className="input"
                            >
                                <option value="">Select Category</option>
                                {categories.map(cat => (
                                    <option key={cat.id} value={cat.id}>
                                        {cat.icon ? `${cat.icon} ` : ''}{cat.name}{cat.type ? ` (${cat.type})` : ''}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="field">
                            <label htmlFor="amount" className="label">Budget Amount</label>
                            <input
                                id="amount"
                                type="number"
                                step="0.01"
                                placeholder="0.00"
                                value={form.amount}
                                onChange={e => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
                                required
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="currency" className="label">Currency</label>
                            <select
                                id="currency"
                                value={form.currency}
                                onChange={e => setForm({ ...form, currency: e.target.value })}
                                className="input"
                            >
                                {CURRENCIES.map(c => (
                                    <option key={c.code} value={c.code}>{c.code} — {c.name}</option>
                                ))}
                            </select>
                        </div>

                        <div className="field">
                            <label className="label">Period</label>
                            <div className="grid grid-cols-3 gap-3">
                                <select
                                    value={form.month}
                                    onChange={e => setForm({ ...form, month: parseInt(e.target.value) })}
                                    className="input col-span-2"
                                >
                                    {Array.from({ length: 12 }, (_, i) => (
                                        <option key={i + 1} value={i + 1}>
                                            {new Date(0, i).toLocaleString('default', { month: 'long' })}
                                        </option>
                                    ))}
                                </select>
                                <input
                                    type="number"
                                    value={form.year}
                                    onChange={e => setForm({ ...form, year: parseInt(e.target.value) })}
                                    className="input"
                                />
                            </div>
                        </div>

                        <button type="submit" className="btn-primary w-full">Set Budget</button>
                    </form>
                </div>

                {/* Budget progress list */}
                <div className="lg:col-span-3">
                    <div className="card">
                        <div className="mb-5 flex items-center justify-between gap-3">
                            <div>
                                <h2 className="card-title">Budget vs Actual</h2>
                                <p className="text-sm text-slate-500">Live progress against your limits</p>
                            </div>
                            <span className="badge badge-slate shrink-0">{summaries.length} active</span>
                        </div>

                        {summaries.length === 0 ? (
                            <p className="py-10 text-center text-sm text-slate-400">
                                No budgets set yet. Create one above to start tracking.
                            </p>
                        ) : (
                            <ul className="space-y-4">
                                {summaries.map((summary, index) => {
                                    const actual = summary.spent || 0;
                                    const amount = summary.amount || 0;
                                    const currency = summary.currency || 'INR';
                                    const percentage = amount > 0 ? (actual / amount) * 100 : 0;
                                    const displayPercentage = Math.min(percentage, 100);
                                    const isOver = actual > amount;
                                    const isNear = !isOver && percentage >= 80;

                                    const barClass = isOver ? 'bg-red-500' : isNear ? 'bg-amber-500' : 'bg-brand-600';
                                    const statusText = isOver ? 'text-red-700' : isNear ? 'text-amber-700' : 'text-brand-700';
                                    const statusBadge = isOver
                                        ? 'badge-red'
                                        : isNear
                                            ? 'bg-amber-50 text-amber-700 ring-amber-600/20'
                                            : 'badge-emerald';
                                    const statusLabel = isOver ? 'Over budget' : isNear ? 'Near limit' : 'On track';

                                    return (
                                        <li
                                            key={summary.id || index}
                                            className="rounded-2xl bg-slate-50/60 p-4 ring-1 ring-slate-900/5"
                                        >
                                            <div className="flex items-center justify-between gap-3">
                                                <p className="truncate text-sm font-semibold text-slate-900">
                                                    {getCategoryName(summary.categoryId) || `Category #${summary.categoryId}`}
                                                </p>
                                                <span className={`badge shrink-0 ${statusBadge}`}>{statusLabel}</span>
                                            </div>

                                            <div className="mt-2 flex flex-wrap items-baseline justify-between gap-2 text-sm">
                                                <p className="text-slate-500">
                                                    <span className="font-semibold tabular-nums text-slate-900">
                                                        {formatMoney(actual, currency)}
                                                    </span>
                                                    {' / '}{formatMoney(amount, currency)}
                                                </p>
                                                <p className={`font-medium tabular-nums ${statusText}`}>
                                                    {percentage.toFixed(0)}%
                                                </p>
                                            </div>

                                            <div className="mt-3 h-2.5 w-full overflow-hidden rounded-full bg-slate-200/70 ring-1 ring-inset ring-slate-900/5">
                                                <div
                                                    className={`h-full rounded-full transition-all duration-500 ${barClass}`}
                                                    style={{ width: `${displayPercentage}%` }}
                                                />
                                            </div>

                                            <p className="mt-2 text-xs text-slate-400">
                                                {isOver
                                                    ? `Over by ${formatMoney(actual - amount, currency)}`
                                                    : `${formatMoney(amount - actual, currency)} left this month`}
                                            </p>
                                        </li>
                                    );
                                })}
                            </ul>
                        )}
                    </div>
                </div>

                {/* Budget Allocation Pie Chart */}
                {summaries.length > 0 && (
                    <div className="card lg:col-span-5">
                        <h2 className="card-title">Budget Allocation</h2>
                        <p className="text-sm text-slate-500">How your limits are distributed</p>
                        <div className="mx-auto mt-6 h-72 w-full max-w-md">
                            <Pie data={chartData} options={chartOptions} />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
