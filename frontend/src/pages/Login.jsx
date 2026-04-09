import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await login(email, password);
            navigate('/');
        } catch (err) {
            console.error(err);
            setError(err.response?.data?.message || 'Login failed. Check email/password.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '100px auto', padding: '2rem' }}>
            <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>Finance Tracker</h1>
            <div className="card">
                <h2>Login</h2>
                {error && <p style={{ color: 'red' }}>{error}</p>}

                <form onSubmit={handleSubmit}>
                    <input
                        type="email"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                    <button type="submit" className="primary" disabled={loading} style={{ width: '100%' }}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>

                <p style={{ textAlign: 'center', marginTop: '1rem' }}>
                    Don't have an account? <Link to="/signup">Sign up</Link>
                </p>
            </div>
        </div>
    );
}