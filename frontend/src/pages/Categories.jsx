import { useEffect, useState } from 'react';
import api from '../services/api';

const DEFAULT_COLOUR = '#3b82f6';

export default function Categories() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [form, setForm] = useState({
        name: '',
        type: 'expense',
        icon: '',
        colour: DEFAULT_COLOUR
    });

    const fetchCategories = async () => {
        setLoading(true);
        setError(null);
        try {
            const res = await api.get('/api/categories/');

            let categoryList = [];
            if (Array.isArray(res.data)) {
                categoryList = res.data;
            } else if (res.data?.content && Array.isArray(res.data.content)) {
                categoryList = res.data.content;
            } else if (res.data) {
                categoryList = [res.data];
            }

            setCategories(categoryList);
        } catch (err) {
            console.error("Failed to fetch categories:", err);
            setError("Failed to load categories. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCategories();
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!form.name.trim()) {
            alert("Category name is required");
            return;
        }

        try {
            await api.post('/api/categories/', {
                ...form,
                name: form.name.trim(),
                colour: form.colour || DEFAULT_COLOUR
            });
            setForm({ name: '', type: 'expense', icon: '', colour: DEFAULT_COLOUR });
            fetchCategories();
            alert("Category added successfully!");
        } catch (err) {
            alert("Failed to add category: " + (err.response?.data?.message || err.message));
        }
    };

    const deleteCategory = async (id) => {
        if (!window.confirm("Delete this category?")) return;

        try {
            await api.delete(`/api/categories/${id}`);
            fetchCategories();
        } catch (err) {
            alert("Failed to delete category: " + (err.response?.data?.message || "Unknown error"));
        }
    };

    if (loading) {
        return (
            <div className="page">
                <div className="flex items-center justify-center py-24 text-sm text-slate-400">
                    Loading categories...
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

    return (
        <div className="page">
            <header className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-2xl text-slate-900">My Categories</h1>
                    <p className="mt-1 text-sm text-slate-500">
                        Organise income and expenses with your own labels
                    </p>
                </div>
                <span className="badge badge-slate w-fit">{categories.length} total</span>
            </header>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                {/* Add Category form */}
                <div className="card h-fit lg:col-span-1">
                    <h2 className="card-title">Add New Category</h2>
                    <p className="card-subtitle">Create a reusable tag for transactions</p>

                    <form onSubmit={handleSubmit}>
                        <div className="field">
                            <label htmlFor="name" className="label">Name</label>
                            <input
                                id="name"
                                placeholder="e.g. Food, Transport, Salary"
                                value={form.name}
                                onChange={e => setForm({ ...form, name: e.target.value })}
                                required
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="type" className="label">Type</label>
                            <select
                                id="type"
                                value={form.type}
                                onChange={e => setForm({ ...form, type: e.target.value })}
                                className="input"
                            >
                                <option value="expense">Expense</option>
                                <option value="income">Income</option>
                            </select>
                        </div>

                        <div className="field">
                            <label htmlFor="icon" className="label">Icon</label>
                            <input
                                id="icon"
                                placeholder="e.g. 🍔, 💰, 🚗"
                                value={form.icon}
                                maxLength={4}
                                onChange={e => setForm({ ...form, icon: e.target.value })}
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="colour" className="label">Colour</label>
                            <div className="flex items-center gap-3 rounded-lg bg-slate-50 px-3 py-2.5 ring-1 ring-inset ring-slate-200">
                                <input
                                    id="colour"
                                    type="color"
                                    value={form.colour}
                                    onChange={e => setForm({ ...form, colour: e.target.value })}
                                    className="h-8 w-10 cursor-pointer rounded border-0 bg-transparent p-0"
                                />
                                <span className="font-mono text-xs text-slate-500">{form.colour}</span>
                            </div>
                        </div>

                        <button type="submit" className="btn-primary w-full">Add Category</button>
                    </form>
                </div>

                {/* Categories matrix */}
                <div className="lg:col-span-2">
                    <h2 className="mb-4 text-lg font-semibold text-slate-900">Your Categories</h2>

                    {categories.length === 0 ? (
                        <div className="card flex flex-col items-center justify-center py-16 text-center">
                            <p className="text-sm font-medium text-slate-600">No categories found.</p>
                            <p className="mt-1 text-sm text-slate-400">Add your first category above.</p>
                        </div>
                    ) : (
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                            {categories.map((cat, index) => {
                                const colour = cat.colour || DEFAULT_COLOUR;
                                return (
                                    <div key={cat.id || index} className="card flex flex-col p-4 transition-shadow duration-200 hover:shadow-pop">
                                        <div className="flex items-start justify-between gap-3">
                                            <span className="text-2xl">{cat.icon || '📁'}</span>
                                            <span className={`badge shrink-0 ${
                                                cat.type === 'income'
                                                    ? 'badge-emerald'
                                                    : cat.type === 'expense'
                                                        ? 'badge-red'
                                                        : 'badge-slate'
                                            }`}>
                                                {cat.type || 'general'}
                                            </span>
                                        </div>

                                        <p className="mt-3 truncate font-semibold text-slate-900">{cat.name}</p>

                                        <div className="mt-4 flex items-center justify-between">
                                            <span
                                                title={colour}
                                                className="inline-block h-3.5 w-3.5 rounded-full ring-1 ring-inset ring-slate-900/10"
                                                style={{ backgroundColor: colour }}
                                            />
                                            <span className="font-mono text-xs text-slate-400">{colour}</span>
                                        </div>

                                        <button
                                            type="button"
                                            className="btn-danger btn-sm mt-4 w-full"
                                            onClick={() => deleteCategory(cat.id)}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
