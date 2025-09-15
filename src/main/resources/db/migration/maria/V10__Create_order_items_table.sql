-- Create order_items table (주문 상품)
CREATE TABLE order_items (
    order_item_seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_seq BIGINT NOT NULL,
    product_seq BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    note TEXT,

    INDEX idx_order_items_order_seq (order_seq),
    INDEX idx_order_items_product_seq (product_seq),

    CONSTRAINT fk_order_items_order_seq
        FOREIGN KEY (order_seq)
        REFERENCES orders(order_seq)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_order_items_product_seq
        FOREIGN KEY (product_seq)
        REFERENCES products(product_seq)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;