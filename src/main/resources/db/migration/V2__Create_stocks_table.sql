CREATE TABLE stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    exchange VARCHAR(10) NOT NULL,
    sector VARCHAR(50),
    market_cap DECIMAL(20,2),
    current_price DECIMAL(10,2),
    previous_close DECIMAL(10,2),
    day_high DECIMAL(10,2),
    day_low DECIMAL(10,2),
    volume BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_symbol (symbol),
    INDEX idx_exchange (exchange),
    INDEX idx_sector (sector),
    INDEX idx_is_active (is_active)
);

-- Insert sample data
INSERT INTO stocks (symbol, company_name, exchange, sector, current_price, previous_close, market_cap, volume, is_active) VALUES
('RELIANCE', 'Reliance Industries Ltd', 'NSE', 'Oil & Gas', 2500.00, 2480.00, 1700000.00, 1500000, TRUE),
('TCS', 'Tata Consultancy Services', 'NSE', 'IT', 3200.00, 3180.00, 1200000.00, 800000, TRUE),
('INFY', 'Infosys Limited', 'NSE', 'IT', 1400.00, 1390.00, 600000.00, 1200000, TRUE),
('HDFCBANK', 'HDFC Bank Limited', 'NSE', 'Banking', 1600.00, 1590.00, 900000.00, 900000, TRUE),
('ICICIBANK', 'ICICI Bank Limited', 'NSE', 'Banking', 950.00, 940.00, 700000.00, 1100000, TRUE);
