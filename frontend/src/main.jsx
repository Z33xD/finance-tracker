import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import favicon from './assets/money-with-wings.png';
import './index.css';

// Set the tab favicon from the bundled money-with-wings asset
const faviconLink = document.querySelector('link[rel="icon"]') || document.createElement('link');
faviconLink.rel = 'icon';
faviconLink.type = 'image/png';
faviconLink.href = favicon;
document.head.appendChild(faviconLink);

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);