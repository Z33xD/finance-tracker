import { useEffect, useState } from 'react';
import api from '../services/api';
import { Pie } from 'react-chartjs-2';
import {
    Chart as ChartJS,
    ArcElement,
    Tooltip,
    Legend,
} from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);

export default function Budgets() {
    const [budgets, setBudgets] = useState([]);
    const [transactions, setTransactions] = useState([]); // to calculate actual spending
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        category: '',
        amount: 0,
        month: new Date().getMonth() + 1,
        year: new Date().getFullYear(),
    });

    const fetchData = async () => {
        setLoading(true);
        setError(null);
        try {
            // Fetch budgets
            const budgetRes = await api.get('/api/budgets/');
            let budgetList = [];
            if (Array.isArray(budgetRes.data)) budgetList = budgetRes.data;
            else if (budgetRes.data?.content && Array.isArray(budgetRes.data.content)) budgetList = budgetRes.data.content;
            else if (budgetRes.data) budgetList = [budgetRes.data];
            setBudgets(budgetList);

            // Fetch transactions to calculate actual spending (for this month)
            const txRes = await api.get('/api/transactions/');
            let txList = [];
            if (Array.isArray(txRes.data)) txList = txRes.data;
            else if (txRes.data?.content) txList = txRes.data.content;
            setTransactions(txList);
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
        if (!form.category || form.amount <= 0) {
            alert("Please enter a valid category and amount");
            return;
        }

        try {
            await api.post('/api/budgets/', form);
            setForm({
                category: '',
                amount: 0,
                month: new Date().getMonth() + 1,
                year: new Date().getFullYear(),
            });
            fetchData();
            alert("Budget added successfully!");
        } catch (err) {
            alert("Failed to add budget: " + (err.response?.data?.message || err.message));
        }
    };

    // Calculate actual spent per category for current month
    const getActualSpent = (category) => {
        const currentMonth = new Date().getMonth() + 1;
        const currentYear = new Date().getFullYear();

        return transactions
            .filter(tx =>
                tx.transactionType === 'EXPENSE' &&
                tx.category === category && // assuming your Transaction has a 'category' field (string or id)
                new Date(tx.date).getMonth() + 1 === currentMonth &&
                new Date(tx.date).getFullYear() === currentYear
            )
            .reduce((sum, tx) => sum + Number(tx.amount || 0), 0);
    };

    // Prepare data for Pie Chart
    const chartData = {
        labels: budgets.map(b => b.category || 'Unknown'),
        datasets: [{
            data: budgets.map(b => b.amount || 0),
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
                    <input
                        placeholder="Category (e.g. Food, Transport, Rent)"
                        value={form.category}
                        onChange={e => setForm({ ...form, category: e.target.value })}
                        required
                    />
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Budget Amount"
                        value={form.amount}
                        onChange={e => setForm({ ...form, amount: parseFloat(e.target.value) || 0 })}
                        required
                    />
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
                {budgets.length === 0 ? (
                    <p>No budgets set yet. Create one above.</p>
                ) : (
                    <div>
                        {budgets.map((budget, index) => {
                            const actual = getActualSpent(budget.category);
                            const percentage = budget.amount > 0 ? Math.min((actual / budget.amount) * 100, 100) : 0;
                            const isOver = actual > budget.amount;

                            return (
                                <div key={budget.id || index} style={{ marginBottom: '20px', padding: '15px', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
                                        <strong>{budget.category}</strong>
                                        <span>
                      ₹{actual.toFixed(2)} / ₹{budget.amount.toFixed(2)}
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
            {budgets.length > 0 && (
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