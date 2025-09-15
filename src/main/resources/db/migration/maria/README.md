# Database Schema Documentation

## 테이블 구조 개요

이 디렉토리는 MariaDB용 Flyway 마이그레이션 파일들을 포함합니다.

## 마이그레이션 순서

| 순서 | 파일명 | 테이블명 | 설명 |
|------|--------|----------|------|
| V1   | Create_users_table.sql | users | 사용자 정보 |
| V2   | Create_posts_table.sql | posts | 게시글 정보 |
| V3   | Create_categories_table.sql | categories | 상품 카테고리 |
| V4   | Create_suppliers_table.sql | suppliers | 공급업체 |
| V5   | Create_products_table.sql | products | 상품 정보 |
| V6   | Create_inventory_table.sql | inventory | 재고 관리 |
| V7   | Create_stock_movements_table.sql | stock_movements | 재고 이동 이력 |
| V8   | Create_customers_table.sql | customers | 고객 정보 |
| V9   | Create_orders_table.sql | orders | 주문 정보 |
| V10  | Create_order_items_table.sql | order_items | 주문 상품 상세 |
| V11  | Create_discounts_table.sql | discounts | 할인 정책 |
| V12  | Create_discount_rules_table.sql | discount_rules | 할인 적용 규칙 |

## 테이블별 상세 명세

### 1. users (사용자)
- **PK**: user_seq
- **주요 컬럼**: email, password, nickname
- **특징**: Spring Security UserDetails 구현체와 매핑

### 2. posts (게시글)
- **PK**: seq
- **FK**: user_seq → users.user_seq
- **주요 컬럼**: title, content, view_count
- **특징**: 게시판 시스템의 핵심 테이블

### 3. categories (상품 카테고리)
- **PK**: category_seq
- **FK**: parent_category_seq → categories.category_seq (Self Reference)
- **주요 컬럼**: name, description, sort_order
- **특징**: 계층형 카테고리 구조 지원

### 4. suppliers (공급업체)
- **PK**: supplier_seq
- **주요 컬럼**: name, contact_person, email, phone, address
- **특징**: 상품 공급업체 관리

### 5. products (상품)
- **PK**: product_seq
- **FK**:
  - category_seq → categories.category_seq
  - supplier_seq → suppliers.supplier_seq
- **주요 컬럼**: code, name, unit_price, unit_cost, sku, barcode
- **특징**: 재고 관리 시스템의 핵심 상품 정보

### 6. inventory (재고)
- **PK**: inventory_seq
- **FK**: product_seq → products.product_seq
- **주요 컬럼**: current_stock, reserved_stock, available_stock (계산 컬럼)
- **특징**: 실시간 재고 수량 관리

### 7. stock_movements (재고 이동 이력)
- **PK**: stock_movement_seq
- **FK**:
  - product_seq → products.product_seq
  - user_id → users.user_seq
- **주요 컬럼**: movement_type, quantity, reference_type
- **특징**: 모든 재고 변동 이력 추적

### 8. customers (고객)
- **PK**: customer_seq
- **주요 컬럼**: customer_code, name, email, company, customer_type
- **특징**: 개인/기업 고객 구분 관리

### 9. orders (주문)
- **PK**: order_seq
- **FK**:
  - customer_seq → customers.customer_seq
  - user_id → users.user_seq
- **주요 컬럼**: order_number, status, total_amount, payment_status
- **특징**: 주문 헤더 정보

### 10. order_items (주문 상품 상세)
- **PK**: order_item_seq
- **FK**:
  - order_seq → orders.order_seq
  - product_seq → products.product_seq
- **주요 컬럼**: quantity, unit_price, total_amount
- **특징**: 주문별 상품 상세 정보

### 11. discounts (할인 정책)
- **PK**: discount_seq
- **주요 컬럼**: name, discount_type, discount_value, start_date, end_date
- **특징**: 퍼센트/고정금액 할인 지원

### 12. discount_rules (할인 적용 규칙)
- **PK**: discount_rule_seq
- **FK**: discount_seq → discounts.discount_seq
- **주요 컬럼**: target_type, target_id
- **특징**: 카테고리/상품/고객별 할인 적용 규칙

## 명명 규칙

- **PK 컬럼**: `테이블명_seq` (단, posts는 `seq`)
- **FK 컬럼**: `참조테이블명_seq`
- **인덱스**: `idx_테이블명_컬럼명`
- **제약조건**: `fk_테이블명_참조테이블명_seq`

## 테이블 삭제 쿼리 (역순)

```sql
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
```

## 개발 참고사항

1. **Character Set**: 모든 테이블은 `utf8mb4` 사용
2. **Collation**: `utf8mb4_unicode_ci` 사용
3. **Engine**: InnoDB 사용
4. **Timestamp**: 자동 관리 (`created_at`, `updated_at`)
5. **Flyway 설정**: `baseline-on-migrate: true` 적용

## 데이터베이스 초기화

전체 스키마를 초기화하려면 위의 DROP TABLE 쿼리를 순서대로 실행한 후,
Flyway를 다시 실행하면 됩니다.

```bash
# Flyway 마이그레이션 실행
./gradlew flywayMigrate
```