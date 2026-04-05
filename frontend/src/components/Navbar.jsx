import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav style={navStyle}>
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                <Link to="/" style={linkStyle}>Home</Link>
                <Link to="/accounts" style={linkStyle}>Accounts</Link>
                <Link to="/transactions" style={linkStyle}>Transactions</Link>
                <Link to="/budgets" style={linkStyle}>Budgets</Link>
                <Link to="/profile" style={linkStyle}>Profile</Link>
            </div>
            <button onClick={handleLogout} style={btnStyle}>Logout</button>
        </nav>
    );
}

const navStyle = {
    background: '#1f2937',
    color: 'white',
    padding: '1rem',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    position: 'sticky',
    top: 0,
    zIndex: 100,
};

const linkStyle = { color: 'white', textDecoration: 'none', fontWeight: 500 };
const btnStyle = { background: '#ef4444', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer' };