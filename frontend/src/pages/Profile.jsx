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
        <div>
            <h1>Profile</h1>
            <div className="card">
                <h2>Account Details</h2>
                <form onSubmit={handleUpdate} style={{ display: 'grid', gap: '12px', maxWidth: '450px' }}>
                    <input
                        value={form.username || ''}
                        onChange={e => setForm({ ...form, username: e.target.value })}
                        placeholder="Username"
                        required
                    />
                    <input
                        type="email"
                        value={form.email || ''}
                        onChange={e => setForm({ ...form, email: e.target.value })}
                        placeholder="Email"
                        required
                    />
                    <button type="submit" className="primary" disabled={saving}>
                        {saving ? 'Saving...' : 'Update Profile'}
                    </button>
                </form>

                <hr style={{ margin: '24px 0', border: '1px solid #e5e7eb' }} />

                Note: Deleting your account is permanent. This action cannot be reversed.
                <div>
                    <button
                        className="danger"
                        style={{ marginTop: '20px' }}
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