import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

import Login from './pages/Login';
import Signup from './pages/Signup';
import Home from './pages/Home';
import Accounts from './pages/Accounts';
import Transactions from './pages/Transactions';
import Budgets from './pages/Budgets';
import Profile from './pages/Profile';

function App() {
  return (
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            <Route
                path="/*"
                element={
                  <ProtectedRoute>
                    <Navbar />
                    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
                      <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/accounts" element={<Accounts />} />
                        <Route path="/transactions" element={<Transactions />} />
                        <Route path="/budgets" element={<Budgets />} />
                        <Route path="/profile" element={<Profile />} />
                      </Routes>
                    </div>
                  </ProtectedRoute>
                }
            />
          </Routes>
        </Router>
      </AuthProvider>
  );
}

export default App;