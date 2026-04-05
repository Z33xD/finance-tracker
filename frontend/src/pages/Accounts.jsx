import { useEffect, useState } from 'react';
import api from '../services/api';

export default function Accounts() {
    const [accounts, setAccounts] = useState([]);
    const [form, setForm] = useState({ name: '', balance: 0, currency: 'INR' });

    const fetchAccounts = async () => {
        const res = await api.get('/api/accounts/');
        setAccounts(res.data);
    };

    useEffect(() => { fetchAccounts(); }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        await api.post('/api/accounts/', form);
        setForm({ name: '', balance: 0, currency: 'INR' });
        fetchAccounts();
    };

    const deleteAccount = async (id) => {
        if (window.confirm('Delete this account?')) {
            await api.delete(`/api/accounts/${id}`);
            fetchAccounts();
        }
    };

    return (
        <div>
            <h1>Accounts</h1>

            <div className="card">
                <h2>Add New Account</h2>
                <form onSubmit={handleSubmit}>
                    <input placeholder="Account Name" value={form.name} onChange={e => setForm({...form, name: e.target.value})} required />
                    <input type="number" placeholder="Initial Balance" value={form.balance} onChange={e => setForm({...form, balance: +e.target.value})} />
                    <button type="submit" className="primary">Add Account</button>
                </form>
            </div>

            <table>
                <thead>
                <tr><th>Name</th><th>Balance</th><th>Actions</th></tr>
                </thead>
                <tbody>
                {accounts.map(acc => (
                    <tr key={acc.id}>
                        <td>{acc.name}</td>
                        <td>₹{acc.balance}</td>
                        <td><button className="danger" onClick={() => deleteAccount(acc.id)}>Delete</button></td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}