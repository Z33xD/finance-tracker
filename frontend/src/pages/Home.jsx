import { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export default function Home() {
    const { user } = useAuth();
    const [balance, setBalance] = useState(0);
    const [recentTx, setRecentTx] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const txRes = await api.get('/api/transactions/');
                setRecentTx(txRes.data.slice(0, 5)); // last 5

                // Simple balance calculation (you can improve later)
                const total = txRes.data.reduce((sum, t) => {
                    return t.transactionType === 'INCOME' ? sum + t.amount : sum - t.amount;
                }, 0);
                setBalance(total);
            } catch (err) {
                console.error(err);
            }
        };
        fetchData();
    }, []);

    return (
        <div>
            <h1>Welcome, {user?.username || 'User'}!</h1>

            <div className="card">
                <h2>Total Balance</h2>
                <h1 style={{ fontSize: '3rem', color: balance >= 0 ? '#16a34a' : '#ef4444' }}>
                    ₹{balance.toFixed(2)}
                </h1>
            </div>

            <div className="card">
                <h2>Recent Transactions</h2>
                <table>
                    <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th>Amount</th>
                    </tr>
                    </thead>
                    <tbody>
                    {recentTx.map(tx => (
                        <tr key={tx.id}>
                            <td>{tx.date}</td>
                            <td>{tx.description}</td>
                            <td style={{ color: tx.transactionType === 'INCOME' ? '#16a34a' : '#ef4444' }}>
                                {tx.transactionType === 'INCOME' ? '+' : '-'}${tx.amount}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}