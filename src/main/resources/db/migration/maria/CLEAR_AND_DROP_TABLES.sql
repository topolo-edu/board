-- 외래키 의존성을 고려한 삭제 순서

-- 12. discount_rules (할인 규칙)
DROP TABLE IF EXISTS discount_rules;

-- 11. discounts (할인)
DROP TABLE IF EXISTS discounts;

-- 10. order_items (주문 상품)
DROP TABLE IF EXISTS order_items;

-- 9. orders (주문)
DROP TABLE IF EXISTS orders;

-- 8. customers (고객)
DROP TABLE IF EXISTS customers;

-- 7. stock_movements (재고 이동 이력)
DROP TABLE IF EXISTS stock_movements;

-- 6. inventory (재고)
DROP TABLE IF EXISTS inventory;

-- 5. products (상품)
DROP TABLE IF EXISTS products;

-- 4. suppliers (공급업체)
DROP TABLE IF EXISTS suppliers;

-- 3. categories (카테고리)
DROP TABLE IF EXISTS categories;

-- 2. posts (게시글)
DROP TABLE IF EXISTS posts;

-- 1. users (사용자)
DROP TABLE IF EXISTS users;


delete from flyway_schema_history;