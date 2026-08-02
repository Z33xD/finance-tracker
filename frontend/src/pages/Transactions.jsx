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

    if (loading) {
        return (
            <div className="page">
                <div className="flex items-center justify-center py-24 text-sm text-slate-400">
                    Loading transactions...
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page">
                <div className="rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-600/20">
                    {error}
                </div>
            </div>
        );
    }

    const noAccountsWarning = (
        <div className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-inset ring-red-600/20">
            No accounts found. Please go to the <strong>Accounts</strong> tab and create at least one account first.
        </div>
    );

    const accountOptions = accounts.map(acc => (
        <option key={acc.id} value={acc.id}>
            {acc.account_name} ({formatMoney(acc.balance ?? acc.initial_balance ?? 0, acc.currency || 'INR')})
        </option>
    ));

    const batchStatusBadge = (status) => {
        if (status === 'Processed') return 'badge-emerald';
        if (status === 'Failed') return 'badge-red';
        return 'bg-amber-50 text-amber-700 ring-amber-600/20';
    };

    return (
        <div className="page">
            <header className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl text-slate-900">Transactions</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Record entries, import CSVs, and review your full ledger
                    </p>
                </div>
                <span className="badge badge-slate w-fit">{transactions.length} entries</span>
            </header>

            {/* Add Transaction + Import forms */}
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                {/* Add New Transaction */}
                <div className="card h-fit">
                    <h2 className="card-title">Add New Transaction</h2>
                    <p className="card-subtitle">Log a single income or expense</p>

                    {accounts.length === 0 ? (
                        noAccountsWarning
                    ) : (
                        <form onSubmit={handleSubmit}>
                            <div className="field">
                                <label htmlFor="account_id" className="label">Account</label>
                                <select
                                    id="account_id"
                                    value={form.account_id}
                                    onChange={e => setForm({...form, account_id: e.target.value})}
                                    required
                                    className="input"
                                >
                                    <option value="">Select Account</option>
                                    {accountOptions}
                                </select>
                            </div>

                            <div className="field">
                                <label htmlFor="description" className="label">Description</label>
                                <input
                                    id="description"
                                    placeholder="e.g. Grocery run, Salary credit"
                                    value={form.description}
                                    onChange={e => setForm({...form, description: e.target.value})}
                                    required
                                    className="input"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-3">
                                <div className="field">
                                    <label htmlFor="amount" className="label">Amount</label>
                                    <input
                                        id="amount"
                                        type="number"
                                        step="0.01"
                                        placeholder="0.00"
                                        value={form.amount}
                                        onChange={e => setForm({...form, amount: parseFloat(e.target.value) || 0})}
                                        required
                                        className="input"
                                    />
                                </div>
                                <div className="field">
                                    <label htmlFor="transactionType" className="label">Type</label>
                                    <select
                                        id="transactionType"
                                        value={form.transactionType}
                                        onChange={e => setForm({...form, transactionType: e.target.value})}
                                        className="input"
                                    >
                                        <option value="INCOME">Income</option>
                                        <option value="EXPENSE">Expense</option>
                                    </select>
                                </div>
                            </div>

                            <div className="field">
                                <label htmlFor="category_id" className="label">Category</label>
                                <select
                                    id="category_id"
                                    value={form.category_id}
                                    onChange={e => setForm({...form, category_id: e.target.value})}
                                    className="input"
                                >
                                    <option value="">No Category</option>
                                    {filteredCategories.map(cat => (
                                        <option key={cat.id} value={cat.id}>
                                            {cat.name}{cat.type ? ` (${cat.type})` : ''}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="field">
                                <label htmlFor="transactionDate" className="label">Date</label>
                                <input
                                    id="transactionDate"
                                    type="date"
                                    value={form.transactionDate}
                                    onChange={e => setForm({...form, transactionDate: e.target.value})}
                                    className="input"
                                />
                            </div>

                            <button type="submit" className="btn-primary w-full">Add Transaction</button>
                        </form>
                    )}
                </div>

                {/* Import Transactions */}
                <div className="card h-fit">
                    <h2 className="card-title">Import Transactions (CSV)</h2>
                    <p className="card-subtitle">
                        Bulk-import rows using the format{' '}
                        <code className="rounded bg-ivory-200 px-1.5 py-0.5 font-mono text-xs text-slate-600">
                            category_id, amount, transaction_date (yyyy-MM-dd), description, transaction_type
                        </code>
                    </p>

                    <button type="button" onClick={downloadTemplate} className="btn-outline mb-4">
                        Download CSV Template
                    </button>

                    {accounts.length === 0 ? (
                        noAccountsWarning
                    ) : (
                        <form onSubmit={handleImport}>
                            <div className="field">
                                <label htmlFor="importAccountId" className="label">Account</label>
                                <select
                                    id="importAccountId"
                                    value={importAccountId}
                                    onChange={e => setImportAccountId(e.target.value)}
                                    required
                                    className="input"
                                >
                                    <option value="">Select Account</option>
                                    {accountOptions}
                                </select>
                            </div>

                            <div className="field">
                                <label htmlFor="importFile" className="label">CSV File</label>
                                <input
                                    id="importFile"
                                    ref={fileInputRef}
                                    type="file"
                                    accept=".csv"
                                    onChange={e => setImportFile(e.target.files[0] || null)}
                                    required
                                    className="input file:mr-3 file:rounded-lg file:border-0 file:bg-brand-50 file:px-3 file:py-1.5 file:text-sm file:font-medium file:text-brand-700 hover:file:bg-brand-100"
                                />
                            </div>

                            <button type="submit" className="btn-primary w-full" disabled={importing}>
                                {importing ? 'Importing...' : 'Import CSV'}
                            </button>
                        </form>
                    )}

                    {importMessage && (
                        <div className={`mt-4 rounded-lg px-3.5 py-2.5 text-sm ring-1 ring-inset ${
                            importMessage.startsWith('Import failed')
                                ? 'bg-red-50 text-red-700 ring-red-600/20'
                                : 'bg-brand-50 text-brand-700 ring-brand-600/20'
                        }`}>
                            {importMessage}
                        </div>
                    )}
                </div>
            </div>

            {/* All Transactions */}
            <div className="card mt-6">
                <div className="mb-4 flex items-center justify-between gap-3">
                    <div>
                        <h2 className="card-title">All Transactions</h2>
                        <p className="text-sm text-slate-500">Your complete financial ledger</p>
                    </div>
                </div>

                {transactions.length === 0 ? (
                    <p className="py-8 text-center text-sm text-slate-400">
                        No transactions found yet. Add one using the form above.
                    </p>
                ) : (
                    <table className="table">
                        <thead>
                        <tr>
                            <th>Date</th>
                            <th>Description</th>
                            <th>Account</th>
                            <th>Category</th>
                            <th>Type</th>
                            <th className="text-right">Amount</th>
                            <th className="text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {transactions.map((tx, index) => {
                            const isCredit = tx.transactionType === 'INCOME' || tx.transactionType === 'CREDIT';
                            return (
                                <tr key={tx.id || index}>
                                    <td className="whitespace-nowrap text-slate-500">{tx.date || tx.transactionDate || 'N/A'}</td>
                                    <td>{tx.description || 'No description'}</td>
                                    <td className="text-slate-500">{tx.accountName || `Account #${tx.account_id}`}</td>
                                    <td className="text-slate-500">{getCategoryName(tx.category_id) || '—'}</td>
                                    <td>
                                        <span className={`badge ${isCredit ? 'badge-emerald' : 'badge-slate'}`}>
                                            {tx.transactionType || 'EXPENSE'}
                                        </span>
                                    </td>
                                    <td className={`text-right ${isCredit ? 'amount-pos' : 'amount-neg'}`}>
                                        {isCredit ? '+' : '-'}
                                        {formatMoney(tx.amount || 0, accountCurrencyMap[tx.account_id] || 'INR')}
                                    </td>
                                    <td className="text-right">
                                        <button type="button" className="btn-danger btn-sm" onClick={() => deleteTx(tx.id)}>
                                            Delete
                                        </button>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Recent Imports */}
            <div className="card mt-6">
                <div className="mb-4">
                    <h2 className="card-title">Recent Imports</h2>
                    <p className="text-sm text-slate-500">Audit log of your CSV import jobs</p>
                </div>

                {batches.length === 0 ? (
                    <p className="py-8 text-center text-sm text-slate-400">
                        No imports yet. Use the form above to import transactions from a CSV file.
                    </p>
                ) : (
                    <table className="table">
                        <thead>
                        <tr>
                            <th>File</th>
                            <th>Status</th>
                            <th className="text-right">Total</th>
                            <th className="text-right">Success</th>
                            <th className="text-right">Failed</th>
                            <th>Started</th>
                            <th className="text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {batches.map(batch => (
                            <tr key={batch.id}>
                                <td className="text-slate-500">{batch.file_name || 'N/A'}</td>
                                <td>
                                    <span className={`badge ${batchStatusBadge(batch.status)}`}>
                                        {batch.status || 'N/A'}
                                    </span>
                                </td>
                                <td className="text-right tabular-nums text-slate-700">{batch.total_records}</td>
                                <td className="text-right tabular-nums text-brand-700">{batch.successful_records}</td>
                                <td className="text-right tabular-nums text-red-600">{batch.failed_records}</td>
                                <td className="whitespace-nowrap text-slate-500">
                                    {batch.started_at ? new Date(batch.started_at).toLocaleString() : 'N/A'}
                                </td>
                                <td className="text-right">
                                    <button type="button" className="btn-danger btn-sm" onClick={() => deleteBatch(batch.id)}>
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
