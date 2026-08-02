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
            const [summaryRes, catRes, accountRes] = await Promise.all([
                api.get('/api/budgets/summary'),
                api.get('/api/categories/'),
                api.get('/api/accounts/summary'),
            ]);

            let summaryList = [];
            if (Array.isArray(summaryRes.data)) summaryList = summaryRes.data;
            else if (summaryRes.data?.content && Array.isArray(summaryRes.data.content)) summaryList = summaryRes.data.content;
            else if (summaryRes.data) summaryList = [summaryRes.data];
            setSummaries(summaryList);

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
                '#3b82f6', '#ef4444', '#10b981', '#f59e0b',
                '#8b5cf6', '#ec4899', '#14b8a6'
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

    if (loading) return <div style={{ padding: '2rem' }}>Loading budgets...</div>;
    if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1>Budgets</h1>

            {/* Add Budget Form */}
            <div className="card">
                <h2>Set New Budget</h2>
                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '500px' }}>
                    <select
                        value={form.category_id}
                        onChange={e => setForm({ ...form, category_id: e.target.value })}
                        required
                    >
                        <option value="">Select Category</option>
                        {categories.map(cat => (
                            <option key={cat.id} value={cat.id}>
                                {cat.icon ? `${cat.icon} ` : ''}{cat.name}{cat.type ? ` (${cat.type})` : ''}
                            </option>
                        ))}
                    </select>
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Budget Amount"
                        value={form.amount}
                        onChange={e => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
                        required
                    />
                    <select
                        value={form.currency}
                        onChange={e => setForm({ ...form, currency: e.target.value })}
                    >
                        {CURRENCIES.map(c => (
                            <option key={c.code} value={c.code}>{c.code} — {c.name}</option>
                        ))}
                    </select>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        <select
                            value={form.month}
                            onChange={e => setForm({ ...form, month: parseInt(e.target.value) })}
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
                            style={{ width: '100px' }}
                        />
                    </div>
                    <button type="submit" className="primary">Set Budget</button>
                </form>
            </div>

            {/* Budget List with Comparison */}
            <div className="card">
                <h2>Current Budgets vs Actual Spending</h2>
                {summaries.length === 0 ? (
                    <p>No budgets set yet. Create one above.</p>
                ) : (
                    <div>
                        {summaries.map((summary, index) => {
                            const actual = summary.spent || 0;
                            const amount = summary.amount || 0;
                            const currency = summary.currency || 'INR';
                            const percentage = amount > 0 ? Math.min((actual / amount) * 100, 100) : 0;
                            const isOver = actual > amount;

                            return (
                                <div key={summary.id || index} style={{ marginBottom: '20px', padding: '15px', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                        <strong>{getCategoryName(summary.categoryId) || `Category #${summary.categoryId}`}</strong>
                                        <span>
                      {formatMoney(actual, currency)} / {formatMoney(amount, currency)}
                    </span>
                                    </div>
                                    <div style={{ height: '12px', background: '#e2e8f0', borderRadius: '9999px', overflow: 'hidden' }}>
                                        <div
                                            style={{
                                                height: '100%',
                                                width: `${percentage}%`,
                                                background: isOver ? '#ef4444' : '#3b82f6',
                                                transition: 'width 0.3s',
                                            }}
                                        />
                                    </div>
                                    <small style={{ color: isOver ? '#ef4444' : '#64748b' }}>
                                        {percentage.toFixed(0)}% used {isOver && '(Over budget!)'}
                                    </small>
                                </div>
                            );
                        })}
                    </div>
                )}
            </div>

            {/* Budget Allocation Pie Chart */}
            {summaries.length > 0 && (
                <div className="card">
                    <h2>Budget Allocation</h2>
                    <div style={{ height: '300px', maxWidth: '500px', margin: '0 auto' }}>
                        <Pie data={chartData} options={chartOptions} />
                    </div>
                </div>
            )}
        </div>
    );
}
