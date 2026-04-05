import { useEffect, useState } from 'react';
import api from '../services/api';

export default function Transactions() {
    const [transactions, setTransactions] = useState([]);
    const [form, setForm] = useState({ description: '', amount: 0, transactionType: 'EXPENSE', date: new Date().toISOString().split('T')[0] });

    const fetchTransactions = async () => {
        const res = await api.get('/api/transactions/');
        setTransactions(res.data);
    };

    useEffect(() => { fetchTransactions(); }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        await api.post('/api/transactions/', form);
        setForm({ description: '', amount: 0, transactionType: 'EXPENSE', date: new Date().toISOString().split('T')[0] });
        fetchTransactions();
    };

    const deleteTx = async (id) => {
        if (window.confirm('Delete?')) {
            await api.delete(`/api/transactions/${id}`);
            fetchTransactions();
        }
    };

    return (
        <div>
            <h1>Transactions</h1>

            <div className="card">
                <h2>Add Transaction</h2>
                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '10px' }}>
                    <input placeholder="Description" value={form.description} onChange={e => setForm({...form, description: e.target.value})} />
                    <input type="number" placeholder="Amount" value={form.amount} onChange={e => setForm({...form, amount: +e.target.value})} />
                    <select value={form.transactionType} onChange={e => setForm({...form, transactionType: e.target.value})}>
                        <option value="INCOME">Income</option>
                        <option value="EXPENSE">Expense</option>
                    </select>
                    <input type="date" value={form.date} onChange={e => setForm({...form, date: e.target.value})} />
                    <button type="submit" className="primary">Add Transaction</button>
                </form>
            </div>

            <table>
                <thead>
                <tr><th>Date</th><th>Description</th><th>Type</th><th>Amount</th><th>Action</th></tr>
                </thead>
                <tbody>
                {transactions.map(tx => (
                    <tr key={tx.id}>
                        <td>{tx.date}</td>
                        <td>{tx.description}</td>
                        <td>{tx.transactionType}</td>
                        <td style={{ color: tx.transactionType === 'INCOME' ? '#16a34a' : '#ef4444' }}>
                            ₹{tx.amount}
                        </td>
                        <td><button className="danger" onClick={() => deleteTx(tx.id)}>Delete</button></td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}