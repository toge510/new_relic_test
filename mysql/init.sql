-- init.sql: create history table for weather data
-- This script runs when the MySQL container is initialized (only on first run)

CREATE TABLE IF NOT EXISTS weather_history (
  id INT AUTO_INCREMENT PRIMARY KEY,
  city VARCHAR(128) NOT NULL,
  body LONGTEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_city_created_at (city, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
