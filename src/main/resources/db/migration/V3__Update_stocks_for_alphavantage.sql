-- Add US stocks alongside existing Indian stocks
INSERT INTO stocks (symbol, company_name, exchange, sector, current_price, previous_close, market_cap, volume, is_active) VALUES
('IBM', 'International Business Machines', 'NYSE', 'Technology', 150.00, 148.50, 138000.00, 5000000, TRUE),
('AAPL', 'Apple Inc', 'NASDAQ', 'Technology', 175.00, 174.20, 2800000.00, 80000000, TRUE),
('MSFT', 'Microsoft Corporation', 'NASDAQ', 'Technology', 380.00, 378.50, 2850000.00, 25000000, TRUE),
('GOOGL', 'Alphabet Inc', 'NASDAQ', 'Technology', 140.00, 139.20, 1750000.00, 28000000, TRUE),
('TSLA', 'Tesla Inc', 'NASDAQ', 'Automotive', 240.00, 238.50, 765000.00, 120000000, TRUE);
