export function formatMoney(amount, currency = 'INR') {
    const value = Number(amount || 0);
    const code = (currency || 'INR').toUpperCase();
    try {
        return new Intl.NumberFormat(undefined, { style: 'currency', currency: code }).format(value);
    } catch {
        return `${code} ${value.toFixed(2)}`;
    }
}
