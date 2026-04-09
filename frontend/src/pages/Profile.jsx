import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export default function Profile() {
    const { user, logout } = useAuth();
    const [form, setForm] = useState({});

    useEffect(() => {
        if (user) setForm(user);
    }, [user]);

    const handleUpdate = async (e) => {
        e.preventDefault();
        await api.put('/api/users/me', form);
        alert('Profile updated!');
    };

    const handleDelete = async () => {
        if (window.confirm('Delete your entire account? This cannot be undone!')) {
            await api.delete('/api/users/me');
            logout();
        }
    };

    return (
        <div>
            <h1>Profile</h1>
            <div className="card">
                {/*
                <form onSubmit={handleUpdate}>
                    <input value={form.username || ''} onChange={e => setForm({...form, username: e.target.value})} placeholder="Username" />
                    <input value={form.email || ''} onChange={e => setForm({...form, email: e.target.value})} placeholder="Email" />
                    <button type="submit" className="primary">Update Profile</button>
                </form>
                */}
                Note: Deleting your account is permanent. This action cannot be reversed.
                <div>
                <button className="danger" style={{ marginTop: '20px' }} onClick={handleDelete}>Delete Account</button>
                </div>
            </div>
        </div>
    );
}