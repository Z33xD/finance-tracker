import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AuthLayout from '../components/AuthLayout';

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
        <AuthLayout
            title={step === 1 ? 'Create your account' : 'Verify your email'}
            subtitle={step === 1 ? 'Start tracking your money in minutes' : 'We sent a code to your inbox'}
        >
            {error && (
                <div className="mb-5 rounded-lg bg-red-50 px-3.5 py-2.5 text-sm text-red-700 ring-1 ring-inset ring-red-600/20">
                    {error}
                </div>
            )}

            {step === 1 ? (
                <form onSubmit={handleSignup} className="space-y-4">
                    <div>
                        <label htmlFor="username" className="label">Username</label>
                        <input
                            id="username"
                            name="username"
                            placeholder="Username"
                            value={formData.username}
                            onChange={handleChange}
                            required
                            className="input"
                        />
                    </div>
                    <div>
                        <label htmlFor="email" className="label">Email</label>
                        <input
                            id="email"
                            name="email"
                            type="email"
                            placeholder="Email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                            className="input"
                        />
                    </div>
                    <div>
                        <label htmlFor="password" className="label">Password</label>
                        <input
                            id="password"
                            name="password"
                            type="password"
                            placeholder="Password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                            className="input"
                        />
                    </div>
                    <button type="submit" className="btn-primary w-full" disabled={loading}>
                        {loading ? 'Sending...' : 'Sign Up'}
                    </button>
                </form>
            ) : (
                <form onSubmit={handleVerify} className="space-y-4">
                    <div>
                        <label htmlFor="verificationCode" className="label">Verification Code</label>
                        <input
                            id="verificationCode"
                            placeholder="Verification Code"
                            value={verificationCode}
                            onChange={(e) => setVerificationCode(e.target.value)}
                            required
                            className="input"
                        />
                    </div>
                    <button type="submit" className="btn-primary w-full" disabled={loading}>
                        {loading ? 'Verifying...' : 'Verify'}
                    </button>
                </form>
            )}

            <p className="mt-6 text-center text-sm text-slate-500">
                Already have an account? <Link to="/login" className="link">Login</Link>
            </p>
        </AuthLayout>
    );
}
