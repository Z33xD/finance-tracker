import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navItems = [
    { to: '/', label: 'Home' },
    { to: '/accounts', label: 'Accounts' },
    { to: '/transactions', label: 'Transactions' },
    { to: '/categories', label: 'Categories' },
    { to: '/budgets', label: 'Budgets' },
    { to: '/profile', label: 'Profile' },
];

export default function Navbar() {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <header className="sticky top-0 z-50 border-b border-ivory-200 bg-ivory-50/80 backdrop-blur">
            <div className="mx-auto flex h-16 max-w-6xl items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
                <div className="flex min-w-0 items-center gap-2 sm:gap-6">
                    <NavLink to="/" className="flex shrink-0 items-center gap-2">
                        <span className="hidden font-display text-lg font-semibold tracking-tight text-slate-900 md:inline">
                            Finance Tracker
                        </span>
                    </NavLink>

                    <nav className="flex items-center gap-1 overflow-x-auto">
                        {navItems.map(({ to, label }) => (
                            <NavLink
                                key={to}
                                to={to}
                                end={to === '/'}
                                className={({ isActive }) =>
                                    `whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                                        isActive
                                            ? 'bg-brand-50 text-brand-700'
                                            : 'text-slate-600 hover:bg-ivory-200 hover:text-slate-900'
                                    }`
                                }
                            >
                                {label}
                            </NavLink>
                        ))}
                    </nav>
                </div>

                <button onClick={handleLogout} className="btn-ghost shrink-0">Logout</button>
            </div>
        </header>
    );
}
