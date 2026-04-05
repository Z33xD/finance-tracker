import { useEffect, useState } from 'react';
import api from '../services/api';

export default function Budgets() {
    const [budgets, setBudgets] = useState([]);

    useEffect(() => {
        api.get('/api/budgets/').then(res => setBudgets(res.data));
    }, []);

    return (
        <div>
            <h1>Budgets</h1>
            <div className="card">
                <h2>Budgets (coming soon - basic list for now)</h2>
                <pre>{JSON.stringify(budgets, null, 2)}</pre>
            </div>
            {/* You can expand this later with charts if you want */}
        </div>
    );
}