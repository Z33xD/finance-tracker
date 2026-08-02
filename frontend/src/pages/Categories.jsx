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

    if (loading) return <div style={{ padding: '2rem' }}>Loading categories...</div>;
    if (error) return <div style={{ padding: '2rem', color: 'red' }}>{error}</div>;

    return (
        <div>
            <h1>My Categories</h1>

            {/* Add Category Form */}
            <div className="card">
                <h2>Add New Category</h2>
                <form onSubmit={handleSubmit} style={{ display: 'grid', gap: '12px', maxWidth: '450px' }}>
                    <input
                        placeholder="Category name (e.g. Food, Transport, Salary)"
                        value={form.name}
                        onChange={e => setForm({ ...form, name: e.target.value })}
                        required
                    />

                    <select
                        value={form.type}
                        onChange={e => setForm({ ...form, type: e.target.value })}
                    >
                        <option value="expense">Expense</option>
                        <option value="income">Income</option>
                    </select>

                    <input
                        placeholder="Icon (e.g. 🍔, 💰, 🚗)"
                        value={form.icon}
                        maxLength={4}
                        onChange={e => setForm({ ...form, icon: e.target.value })}
                    />

                    <label style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                        <span>Colour</span>
                        <input
                            type="color"
                            value={form.colour}
                            onChange={e => setForm({ ...form, colour: e.target.value })}
                            style={{ width: '60px', height: '40px', padding: '2px', cursor: 'pointer', marginBottom: 0 }}
                        />
                        <span style={{ fontFamily: 'monospace', color: '#64748b' }}>{form.colour}</span>
                    </label>

                    <button type="submit" className="primary">Add Category</button>
                </form>
            </div>

            {/* Categories List */}
            <div className="card">
                <h2>Your Categories ({categories.length})</h2>

                {categories.length === 0 ? (
                    <p>No categories found. Add your first category above.</p>
                ) : (
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: '12px' }}>
                        {categories.map((cat, index) => {
                            const colour = cat.colour || DEFAULT_COLOUR;
                            return (
                                <div
                                    key={cat.id || index}
                                    style={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '12px',
                                        padding: '12px 16px',
                                        border: `1px solid ${colour}55`,
                                        background: `${colour}1A`,
                                        borderRadius: '10px',
                                        minWidth: '180px'
                                    }}
                                >
                                    <span style={{ fontSize: '1.6rem' }}>{cat.icon || '📁'}</span>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ fontWeight: 600 }}>{cat.name}</div>
                                        <div style={{ fontSize: '0.8rem', color: '#64748b', textTransform: 'capitalize' }}>
                                            {cat.type || 'general'}
                                        </div>
                                    </div>
                                    <span
                                        title={colour}
                                        style={{
                                            display: 'inline-block',
                                            width: '18px',
                                            height: '18px',
                                            borderRadius: '50%',
                                            background: colour,
                                            border: '1px solid rgba(0,0,0,0.1)',
                                            flexShrink: 0
                                        }}
                                    />
                                    <button
                                        className="danger"
                                        style={{ padding: '6px 12px' }}
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
    );
}
