import { useEffect, useRef, useState } from 'react';
import api from '../services/api';
import { formatMoney } from '../utils/format';

export default function Transactions() {
    const [transactions, setTransactions] = useState([]);
    const [accounts, setAccounts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [batches, setBatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [importAccountId, setImportAccountId] = useState('');
    const [importFile, setImportFile] = useState(null);
    const [importing, setImporting] = useState(false);
    const [importMessage, setImportMessage] = useState(null);
    const fileInputRef = useRef(null);

    const accountCurrencyMap = {};
    accounts.forEach(acc => {
        if (acc && acc.id) accountCurrencyMap[acc.id] = acc.currency || 'INR';
    });

    const [form, setForm] = useState({
        description: '',
        amount: 0,
        transactionType: 'EXPENSE',
        account_id: '',           // ← important: linked to account
        category_id: '',          // ← optional: linked to a category
        transactionDate: new Date().toISOString().split('T')[0]
    });

    // Fetch accounts, categories and transactions
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

            // Fetch categories for the dropdown
            const catRes = await api.get('/api/categories/');
            let categoryList = [];
            if (Array.isArray(catRes.data)) categoryList = catRes.data;
            else if (catRes.data?.content && Array.isArray(catRes.data.content)) categoryList = catRes.data.content;
            else if (catRes.data) categoryList = [catRes.data];
            setCategories(categoryList);

            // Fetch transactions
            const txRes = await api.get('/api/transactions/');
            let txList = [];
            if (Array.isArray(txRes.data)) txList = txRes.data;
            else if (txRes.data?.content && Array.isArray(txRes.data.content)) txList = txRes.data.content;
            else if (txRes.data) txList = [txRes.data];
            setTransactions(txList);

            // Fetch import batches
            const batchRes = await api.get('/api/import-batches/');
            let batchList = [];
            if (Array.isArray(batchRes.data)) batchList = batchRes.data;
            else if (batchRes.data?.content && Array.isArray(batchRes.data.content)) batchList = batchRes.data.content;
            else if (batchRes.data) batchList = [batchRes.data];
            setBatches(batchList);
        } catch (err) {
            console.error(err);
            setError("Failed to load data. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    // Category name lookup for the transactions table
    const getCategoryName = (categoryId) => {
        if (!categoryId) return null;
        const cat = categories.find(c => String(c.id) === String(categoryId));
        return cat ? cat.name : null;
    };

    // Categories matching the selected transaction type (fallback to all)
    const filteredCategories = (() => {
        const matches = categories.filter(c => {
            if (!c.type) return true;
            const type = c.type.toLowerCase();
            return form.transactionType === 'INCOME' ? type === 'income' : type === 'expense';
        });
        return matches.length > 0 ? matches : categories;
    })();

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
                account_id: parseInt(form.account_id),   // ensure it's a number if your backend expects int
                category_id: form.category_id ? parseInt(form.category_id) : null
            });

            // Reset form
            setForm({
                description: '',
                amount: 0,
                transactionType: 'EXPENSE',
                account_id: '',
                category_id: '',
                transactionDate: new Date().toISOString().split('T')[0]
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
        } catch {
            alert("Failed to delete transaction");
        }
    };

    const handleImport = async (e) => {
        e.preventDefault();
        setImportMessage(null);

        if (!importAccountId) {
            alert("Please select an account to import into.");
            return;
        }
        if (!importFile) {
            alert("Please choose a CSV file.");
            return;
        }

        const fd = new FormData();
        fd.append('file', importFile);
        fd.append('account_id', parseInt(importAccountId));

        setImporting(true);
        try {
            const res = await api.post('/api/import-batches/upload', fd, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            const batch = res.data;
            setImportMessage(
                `Import complete: ${batch.successful_records || 0} succeeded, ${batch.failed_records || 0} failed (${batch.total_records || 0} total).`
            );
            setImportFile(null);
            if (fileInputRef.current) fileInputRef.current.value = '';
            fetchData();
        } catch (err) {
            setImportMessage(
                "Import failed: " + (err.response?.data?.message || err.response?.data?.error_message || err.message)
            );
        } finally {
            setImporting(false);
        }
    };

    const deleteBatch = async (id) => {
        if (!window.confirm('Delete this import record?')) return;
        try {
            await api.delete(`/api/import-batches/${id}`);
            fetchData();
        } catch {
            alert("Failed to delete import record");
        }
    };

    const downloadTemplate = () => {
        const sample = [
            'category_id,amount,transaction_date,description,transaction_type',
            '1,1500.00,2026-08-01,Salary,INCOME',
            '2,250.50,2026-08-02,Groceries,EXPENSE',
        ].join('\n');
        const blob = new Blob([sample], { type: 'text/csv' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'transactions-template.csv';
        a.click();
        URL.revokeObjectURL(url);
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
                                    {acc.account_name} ({formatMoney(acc.balance ?? acc.initial_balance ?? 0, acc.currency || 'INR')})
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

                        <select
                            value={form.category_id}
                            onChange={e => setForm({...form, category_id: e.target.value})}
                        >
                            <option value="">No Category</option>
                            {filteredCategories.map(cat => (
                                <option key={cat.id} value={cat.id}>
                                    {cat.name}{cat.type ? ` (${cat.type})` : ''}
                                </option>
                            ))}
                        </select>

                        <input
                            type="date"
                            value={form.transactionDate}
                            onChange={e => setForm({...form, transactionDate: e.target.value})}
                        />

                        <button type="submit" className="primary">Add Transaction</button>
                    </form>
                )}
            </div>

            {/* Import Transactions */}
            <div className="card">
                <h2>Import Transactions (CSV)</h2>
                <p style={{ fontSize: '0.9rem', color: '#666' }}>
                    CSV format: <code>category_id, amount, transaction_date (yyyy-MM-dd), description, transaction_type</code>
                </p>
                <button type="button" onClick={downloadTemplate} className="primary" style={{ marginBottom: '12px' }}>
                    Download CSV Template
                </button>

                {accounts.length === 0 ? (
                    <p style={{ color: '#ef4444' }}>
                        No accounts found. Please go to the <strong>Accounts</strong> tab and create at least one account first.
                    </p>
                ) : (
                    <form onSubmit={handleImport} style={{ display: 'grid', gap: '12px', maxWidth: '500px' }}>
                        <select
                            value={importAccountId}
                            onChange={e => setImportAccountId(e.target.value)}
                            required
                        >
                            <option value="">Select Account</option>
                            {accounts.map(acc => (
                                <option key={acc.id} value={acc.id}>
                                    {acc.account_name} ({formatMoney(acc.balance ?? acc.initial_balance ?? 0, acc.currency || 'INR')})
                                </option>
                            ))}
                        </select>

                        <input
                            ref={fileInputRef}
                            type="file"
                            accept=".csv"
                            onChange={e => setImportFile(e.target.files[0] || null)}
                            required
                        />

                        <button type="submit" className="primary" disabled={importing}>
                            {importing ? 'Importing...' : 'Import CSV'}
                        </button>
                    </form>
                )}

                {importMessage && (
                    <p style={{
                        marginTop: '12px',
                        padding: '8px 12px',
                        borderRadius: '6px',
                        background: importMessage.startsWith('Import failed') ? '#fef2f2' : '#f0fdf4',
                        color: importMessage.startsWith('Import failed') ? '#b91c1c' : '#166534'
                    }}>
                        {importMessage}
                    </p>
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
                            <th>Category</th>
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
                                <td>{getCategoryName(tx.category_id) || '—'}</td>
                                <td>{tx.transactionType}</td>
                                <td style={{
                                    fontWeight: 'bold',
                                    color: (tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT')
                                        ? '#16a34a' : '#ef4444'
                                }}>
                                    {(tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT') ? '+' : '-'}
                                    {formatMoney(tx.amount || 0, accountCurrencyMap[tx.account_id] || 'INR')}
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

            {/* Recent Imports */}
            <div className="card">
                <h2>Recent Imports</h2>
                {batches.length === 0 ? (
                    <p>No imports yet. Use the form above to import transactions from a CSV file.</p>
                ) : (
                    <table>
                        <thead>
                        <tr>
                            <th>File</th>
                            <th>Status</th>
                            <th>Total</th>
                            <th>Success</th>
                            <th>Failed</th>
                            <th>Started</th>
                            <th>Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {batches.map(batch => (
                            <tr key={batch.id}>
                                <td>{batch.file_name || 'N/A'}</td>
                                <td>
                                    <span style={{
                                        padding: '2px 8px',
                                        borderRadius: '4px',
                                        background: batch.status === 'Processed' ? '#dcfce7' : batch.status === 'Failed' ? '#fee2e2' : '#fef9c3',
                                        color: batch.status === 'Processed' ? '#166534' : batch.status === 'Failed' ? '#991b1b' : '#854d0e'
                                    }}>
                                        {batch.status || 'N/A'}
                                    </span>
                                </td>
                                <td>{batch.total_records}</td>
                                <td>{batch.successful_records}</td>
                                <td>{batch.failed_records}</td>
                                <td>{batch.started_at ? new Date(batch.started_at).toLocaleString() : 'N/A'}</td>
                                <td>
                                    <button className="danger" onClick={() => deleteBatch(batch.id)}>Delete</button>
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