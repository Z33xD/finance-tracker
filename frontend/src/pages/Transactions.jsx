import { useEffect, useState } from 'react';
import api from '../services/api';

export default function Transactions() {
    const [transactions, setTransactions] = useState([]);
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        description: '',
        amount: 0,
        transactionType: 'EXPENSE',
        account_id: '',           // ← important: linked to account
        date: new Date().toISOString().split('T')[0]
    });

    // Fetch both accounts and transactions
    const fetchData = async () => {
        setLoading(true);
        setError(null);
        try {
            // Fetch accounts for the dropdown
            const accRes = await api.get('/api/accounts/');
            let accountList = [];
            if (Array.isArray(accRes.data)) accountList = accRes.data;
            else if (accRes.data?.content && Array.isArray(accRes.data.content)) accountList = accRes.data.content;
            else if (accRes.data) accountList = [accRes.data];
            setAccounts(accountList);

            // Fetch transactions
            const txRes = await api.get('/api/transactions/');
            let txList = [];
            if (Array.isArray(txRes.data)) txList = txRes.data;
            else if (txRes.data?.content && Array.isArray(txRes.data.content)) txList = txRes.data.content;
            else if (txRes.data) txList = [txRes.data];
            setTransactions(txList);
        } catch (err) {
            console.error(err);
            setError("Failed to load data. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.account_id) {
            alert("Please select an account for this transaction.");
            return;
        }

        try {
            await api.post('/api/transactions/', {
                ...form,
                account_id: parseInt(form.account_id)   // ensure it's a number if your backend expects int
            });

            // Reset form
            setForm({
                description: '',
                amount: 0,
                transactionType: 'EXPENSE',
                account_id: '',
                date: new Date().toISOString().split('T')[0]
            });

            fetchData(); // refresh both lists
            alert("Transaction added successfully!");
        } catch (err) {
            alert("Failed to add transaction: " + (err.response?.data?.message || err.message));
        }
    };

    const deleteTx = async (id) => {
        if (!window.confirm('Delete this transaction?')) return;
        try {
            await api.delete(`/api/transactions/${id}`);
            fetchData();
        } catch (err) {
            alert("Failed to delete transaction");
        }
    };

    if (loading) return <div style={{ padding: '2rem' }}>Loading transactions...</div>;
    if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1>Transactions</h1>

            {/* Add Transaction Form */}
            <div className="card">
                <h2>Add New Transaction</h2>

                {accounts.length === 0 ? (
                    <p style={{ color: '#ef4444' }}>
                        No accounts found. Please go to the <strong>Accounts</strong> tab and create at least one account first.
                    </p>
                ) : (
                    <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '500px' }}>
                        <select
                            value={form.account_id}
                            onChange={e => setForm({...form, account_id: e.target.value})}
                            required
                        >
                            <option value="">Select Account</option>
                            {accounts.map(acc => (
                                <option key={acc.id} value={acc.id}>
                                    {acc.name} (₹{Number(acc.balance || 0).toFixed(2)})
                                </option>
                            ))}
                        </select>

                        <input
                            placeholder="Description"
                            value={form.description}
                            onChange={e => setForm({...form, description: e.target.value})}
                            required
                        />

                        <input
                            type="number"
                            step="0.01"
                            placeholder="Amount"
                            value={form.amount}
                            onChange={e => setForm({...form, amount: parseFloat(e.target.value) || 0})}
                            required
                        />

                        <select
                            value={form.transactionType}
                            onChange={e => setForm({...form, transactionType: e.target.value})}
                        >
                            <option value="INCOME">Income</option>
                            <option value="EXPENSE">Expense</option>
                        </select>

                        <input
                            type="date"
                            value={form.date}
                            onChange={e => setForm({...form, date: e.target.value})}
                        />

                        <button type="submit" className="primary">Add Transaction</button>
                    </form>
                )}
            </div>

            {/* Transactions List */}
            <div className="card">
                <h2>All Transactions</h2>
                {transactions.length === 0 ? (
                    <p>No transactions found yet. Add one using the form above.</p>
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Account</th>
                            <th>Type</th>
                            <th>Amount</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {transactions.map((tx, index) => (
                            <tr key={tx.id || index}>
                                <td>{tx.date || tx.transactionDate || 'N/A'}</td>
                                <td>{tx.description || 'No description'}</td>
                                <td>{tx.accountName || `Account #${tx.account_id}`}</td>
                                <td>{tx.transactionType}</td>
                                <td style={{
                                    fontWeight: 'bold',
                                    color: (tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT')
                                        ? '#16a34a' : '#ef4444'
                                }}>
                                    {(tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT') ? '+' : '-'}
                                    ₹{Number(tx.amount || 0).toFixed(2)}
                                </td>
                                <td>
                                    <button className="danger" onClick={() => deleteTx(tx.id)}>Delete</button>
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