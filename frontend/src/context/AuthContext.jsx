import { createContext, useContext, useState } from 'react';
import api from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));

    const signup = async (userData) => {
        // Step 1: Register → triggers email
        await api.post('/auth/signup', userData);
        return true; // success, now ask for verification code
    };

    const verifyEmail = async (email, verificationCode) => {
        await api.post('/auth/verify', { email, verificationCode });
    };

    const login = async (email, password) => {
        const res = await api.post('/auth/login', { email, password });

        const receivedToken = res.data.token;
        localStorage.setItem('token', receivedToken);
        setToken(receivedToken);

        // Fetch current user
        const userRes = await api.get('/api/users/me');
        setUser(userRes.data);
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, token, signup, verifyEmail, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);