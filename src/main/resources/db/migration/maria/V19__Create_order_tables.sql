-- 발주 관련 테이블 생성

-- 발주 테이블
CREATE TABLE IF NOT EXISTS orders (
    order_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_seq BIGINT NOT NULL COMMENT '발주 회사',
    user_seq BIGINT NOT NULL COMMENT '발주자',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '발주번호 (ORD-YYYYMMDD-0001)',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '발주일',
    status ENUM('PENDING', 'APPROVED') DEFAULT 'PENDING' COMMENT '발주 상태',
    delivery_status ENUM('ORDER_COMPLETED', 'DELIVERY_COMPLETED') DEFAULT 'ORDER_COMPLETED' COMMENT '배송 상태',
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '총 금액',
    discount_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '할인 금액',
    final_amount DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '최종 금액',
    discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT '적용된 할인율 (%)',
    approved_by VARCHAR(50) COMMENT '승인자',
    approved_at TIMESTAMP NULL COMMENT '승인일시',
    notes TEXT COMMENT '비고',
    version BIGINT DEFAULT 0 COMMENT '낙관적 락',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (company_seq) REFERENCES companies(company_seq),
    FOREIGN KEY (user_seq) REFERENCES users(user_seq),
    INDEX idx_company_status (company_seq, status),
    INDEX idx_delivery_status (delivery_status),
    INDEX idx_order_date (order_date),
    INDEX idx_order_number (order_number)
) COMMENT '발주 테이블';

-- 발주 상품 테이블
CREATE TABLE IF NOT EXISTS order_items (
    order_item_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_seq BIGINT NOT NULL COMMENT '발주 번호',
    product_seq BIGINT NOT NULL COMMENT '상품 번호',
    quantity INT NOT NULL COMMENT '주문 수량',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '단가',
    discount_rate DECIMAL(5,2) DEFAULT 0 COMMENT '할인율 (%)',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '할인 금액',
    line_total DECIMAL(12,2) NOT NULL COMMENT '라인 총액 (할인 적용 후)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (order_seq) REFERENCES orders(order_seq) ON DELETE CASCADE,
    FOREIGN KEY (product_seq) REFERENCES products(product_seq),
    INDEX idx_order_product (order_seq, product_seq)
) COMMENT '발주 상품 테이블';