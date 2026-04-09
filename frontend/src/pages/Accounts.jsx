import { useEffect, useState } from 'react';
import api from '../services/api';

export default function Accounts() {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        name: '',
        balance: 0,
        currency: 'INR',
        accountType: 'SAVINGS'   // optional - adjust if your entity has different types
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
        if (!form.name.trim()) {
            alert("Account name is required");
            return;
        }

        try {
            await api.post('/api/accounts/', form);
            setForm({
                name: '',
                balance: 0,
                currency: 'INR',
                accountType: 'SAVINGS'
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

    if (loading) return <div style={{ padding: '2rem' }}>Loading accounts...</div>;
    if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1>My Accounts</h1>

            {/* Add Account Form */}
            <div className="card">
                <h2>Create New Account</h2>
                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '450px' }}>
                    <input
                        placeholder="Account Name (e.g. Savings Account, Credit Card)"
                        value={form.name}
                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                        required
                    />

                    <input
                        type="number"
                        step="0.01"
                        placeholder="Initial Balance"
                        value={form.balance}
                        onChange={(e) => setForm({ ...form, balance: parseFloat(e.target.value) || 0 })}
                    />

                    <select
                        value={form.currency}
                        onChange={(e) => setForm({ ...form, currency: e.target.value })}
                    >
                        <option value="INR">INR (₹)</option>
                        <option value="USD">USD ($)</option>
                        <option value="EUR">EUR (€)</option>
                    </select>

                    <select
                        value={form.accountType}
                        onChange={(e) => setForm({ ...form, accountType: e.target.value })}
                    >
                        <option value="SAVINGS">Savings</option>
                        <option value="CHECKING">Checking / Current</option>
                        <option value="CREDIT">Credit Card</option>
                        <option value="INVESTMENT">Investment</option>
                    </select>

                    <button type="submit" className="primary">Create Account</button>
                </form>
            </div>

            {/* Accounts List */}
            <div className="card">
                <h2>Your Accounts ({accounts.length})</h2>

                {accounts.length === 0 ? (
                    <p>No accounts found. Create your first account above to start tracking transactions.</p>
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Name</th>
                            <th>Type</th>
                            <th>Balance</th>
                            <th>Currency</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {accounts.map((acc, index) => (
                            <tr key={acc.id || index}>
                                <td><strong>{acc.name}</strong></td>
                                <td>{acc.accountType || '—'}</td>
                                <td style={{
                                    fontWeight: 'bold',
                                    color: Number(acc.balance || 0) >= 0 ? '#16a34a' : '#ef4444'
                                }}>
                                    ₹{Number(acc.balance || 0).toFixed(2)}
                                </td>
                                <td>{acc.currency || 'INR'}</td>
                                <td>
                                    <button
                                        className="danger"
                                        onClick={() => deleteAccount(acc.id)}
                                    >
                                        Delete
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}