import { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { formatMoney } from '../utils/format';

export default function Home() {
    const { user } = useAuth();
    const [balance, setBalance] = useState(0);
    const [baseCurrency, setBaseCurrency] = useState('INR');
    const [recentTx, setRecentTx] = useState([]);
    const [accountCurrencies, setAccountCurrencies] = useState({});
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
                    {formatMoney(balance, baseCurrency)}
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
                                    {formatMoney(tx.amount || 0, accountCurrencies[tx.account_id] || baseCurrency)}
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