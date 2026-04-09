import { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Home() {
    const { user } = useAuth();
    const [balance, setBalance] = useState(0);
    const [recentTx, setRecentTx] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);
            try {
                const txRes = await api.get('/api/transactions/');

                // ← SAFE HANDLING: make sure we have an array
                let transactions = [];
                if (Array.isArray(txRes.data)) {
                    transactions = txRes.data;
                } else if (txRes.data?.content && Array.isArray(txRes.data.content)) {
                    transactions = txRes.data.content;   // common paginated response
                } else if (txRes.data) {
                    transactions = [txRes.data];         // single object fallback
                }

                setRecentTx(transactions.slice(0, 5));

                // Calculate balance safely
                const total = transactions.reduce((sum, t) => {
                    if (!t || typeof t.amount !== 'number') return sum;
                    return t.transactionType === 'INCOME' || t.transactionType === 'CREDIT'
                        ? sum + t.amount
                        : sum - t.amount;
                }, 0);

                setBalance(total);
            } catch (err) {
                console.error("Failed to fetch transactions:", err);
                setError("Could not load dashboard data. Please try refreshing.");
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    if (loading) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading dashboard...</div>;
    }

    if (error) {
        return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;
    }

    return (
        <div>
            <h1>Welcome, {user?.username || user?.email || 'User'}!</h1>

            <div className="card">
                <h2>Total Balance</h2>
                <h1 style={{
                    fontSize: '3rem',
                    color: balance >= 0 ? '#16a34a' : '#ef4444',
                    margin: '0.5rem 0'
                }}>
                    ₹{balance.toFixed(2)}
                </h1>
            </div>

            <div className="card">
                <h2>Recent Transactions</h2>
                {recentTx.length === 0 ? (
                    <p>No transactions yet.</p>
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Amount</th>
                            <th>Type</th>
                        </tr>
                        </thead>
                        <tbody>
                        {recentTx.map((tx, index) => (
                            <tr key={tx.id || index}>
                                <td>{tx.date || tx.transactionDate || 'N/A'}</td>
                                <td>{tx.description || 'No description'}</td>
                                <td style={{
                                    fontWeight: 'bold',
                                    color: (tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT')
                                        ? '#16a34a' : '#ef4444'
                                }}>
                                    {(tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT') ? '+' : '-'}
                                    ₹{tx.amount || 0}
                                </td>
                                <td>{tx.transactionType || 'EXPENSE'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
}