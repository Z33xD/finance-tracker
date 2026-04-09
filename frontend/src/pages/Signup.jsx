import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Signup() {
    const [step, setStep] = useState(1); // 1 = form, 2 = verify code
    const [formData, setFormData] = useState({ username: '', email: '', password: '' });
    const [verificationCode, setVerificationCode] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { signup, verifyEmail } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSignup = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await signup(formData);
            setStep(2); // move to verification
            alert('Verification code sent to your email!');
        } catch (err) {
            setError(err.response?.data?.message || 'Signup failed');
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            await verifyEmail(formData.email, verificationCode);
            alert('Account verified! You can now login.');
            navigate('/login');
        } catch (err) {
            setError(err.response?.data?.message || 'Verification failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ maxWidth: '400px', margin: '100px auto', padding: '2rem' }}>
            <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>Finance Tracker</h1>
            <div className="card">
                <h2>{step === 1 ? 'Sign Up' : 'Verify Email'}</h2>
                {error && <p style={{ color: 'red' }}>{error}</p>}

                {step === 1 ? (
                    <form onSubmit={handleSignup}>
                        <input name="username" placeholder="Username" value={formData.username} onChange={handleChange} required />
                        <input name="email" type="email" placeholder="Email" value={formData.email} onChange={handleChange} required />
                        <input name="password" type="password" placeholder="Password" value={formData.password} onChange={handleChange} required />
                        <button type="submit" className="primary" disabled={loading} style={{ width: '100%' }}>
                            {loading ? 'Sending...' : 'Sign Up'}
                        </button>
                    </form>
                ) : (
                    <form onSubmit={handleVerify}>
                        <input
                            placeholder="Verification Code"
                            value={verificationCode}
                            onChange={(e) => setVerificationCode(e.target.value)}
                            required
                        />
                        <button type="submit" className="primary" disabled={loading} style={{ width: '100%' }}>
                            {loading ? 'Verifying...' : 'Verify'}
                        </button>
                    </form>
                )}

                <p style={{ textAlign: 'center', marginTop: '1rem' }}>
                    Already have an account? <Link to="/login">Login</Link>
                </p>
            </div>
        </div>
    );
}