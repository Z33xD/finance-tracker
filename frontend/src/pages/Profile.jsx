import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Profile() {
    const { user, logout, refreshUser } = useAuth();
    const [form, setForm] = useState({});
    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(false);

    useEffect(() => {
        if (user) setForm({ username: user.username || '', email: user.email || '' });
    }, [user]);

    const handleUpdate = async (e) => {
        e.preventDefault();
        if (!form.username.trim() || !form.email.trim()) {
            alert('Username and email are required');
            return;
        }

        setSaving(true);
        try {
            await api.put('/api/users/me', { username: form.username, email: form.email });
            await refreshUser();
            alert('Profile updated!');
        } catch (err) {
            alert('Failed to update profile: ' + (err.response?.data?.message || err.message));
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async () => {
        if (!window.confirm('Delete your entire account? This cannot be undone!')) return;

        setDeleting(true);
        try {
            await api.delete('/api/users/me');
            logout();
        } catch (err) {
            alert('Failed to delete account: ' + (err.response?.data?.message || err.message));
            setDeleting(false);
        }
    };

    return (
        <div className="page">
            <header className="mb-6">
                <h1 className="text-2xl text-slate-900">Profile</h1>
                <p className="mt-1 text-sm text-slate-500">Manage your account details and settings</p>
            </header>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                {/* Account details */}
                <div className="card h-fit lg:col-span-2">
                    <h2 className="card-title">Account Details</h2>
                    <p className="card-subtitle">Update the information associated with your login</p>

                    <form onSubmit={handleUpdate} className="max-w-md">
                        <div className="field">
                            <label htmlFor="username" className="label">Username</label>
                            <input
                                id="username"
                                value={form.username || ''}
                                onChange={e => setForm({ ...form, username: e.target.value })}
                                placeholder="Username"
                                required
                                className="input"
                            />
                        </div>

                        <div className="field">
                            <label htmlFor="email" className="label">Email</label>
                            <input
                                id="email"
                                type="email"
                                value={form.email || ''}
                                onChange={e => setForm({ ...form, email: e.target.value })}
                                placeholder="Email"
                                required
                                className="input"
                            />
                        </div>

                        <button type="submit" className="btn-primary" disabled={saving}>
                            {saving ? 'Saving...' : 'Update Profile'}
                        </button>
                    </form>
                </div>

                {/* Danger zone */}
                <div className="card h-fit lg:col-span-1">
                    <h2 className="card-title text-red-600">Danger Zone</h2>
                    <p className="card-subtitle">
                        Deleting your account is permanent. This action cannot be reversed.
                    </p>

                    <button
                        type="button"
                        className="btn-danger w-full"
                        onClick={handleDelete}
                        disabled={deleting}
                    >
                        {deleting ? 'Deleting...' : 'Delete Account'}
                    </button>
                </div>
            </div>
        </div>
    );
}
