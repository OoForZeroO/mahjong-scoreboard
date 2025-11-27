-- 创建测试表
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `email` VARCHAR(100) NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO `users` (`username`, `email`) VALUES
('testuser1', 'test1@example.com'),
('testuser2', 'test2@example.com'),
('admin', 'admin@example.com');

-- 创建另一个测试表
CREATE TABLE IF NOT EXISTS `products` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `description` TEXT
);

-- 插入产品测试数据
INSERT INTO `products` (`name`, `price`, `description`) VALUES
('测试产品1', 99.99, '这是第一个测试产品'),
('测试产品2', 199.99, '这是第二个测试产品'),
('测试产品3', 299.99, '这是第三个测试产品');

-- 创建索引以提高性能
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_products_name ON products(name);

-- 显示创建的表
SHOW TABLES;