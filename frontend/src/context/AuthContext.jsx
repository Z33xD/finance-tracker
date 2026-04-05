import { createContext, useContext, useState } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));

    const login = async (username, password) => {
        const res = await api.post('/api/auth/login', { username, password });

        const receivedToken = res.data.token;           // ← exactly matches your response
        localStorage.setItem('token', receivedToken);
        setToken(receivedToken);

        // fetch current user
        const userRes = await api.get('/api/users/me');
        setUser(userRes.data);
    };

    const signup = async (userData) => {
        await api.post('/api/users/', userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, token, login, signup, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);