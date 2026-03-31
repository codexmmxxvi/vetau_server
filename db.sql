CREATE TABLE user (
                      id CHAR(36) PRIMARY KEY,
                      username VARCHAR(50) NOT NULL,
                      email VARCHAR(50) NOT NULL UNIQUE,
                      role TINYINT NOT NULL DEFAULT 0, --0: customer, 1: admin
                      password VARCHAR(50) NOT NULL,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      deleted_at TIMESTAMP NULL
);

CREATE TABLE ticket (
                        id CHAR(36) PRIMARY KEY,
                        user_id CHAR(36) NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        date_start DATETIME NOT NULL,
                        date_end DATETIME NOT NULL,
                        status INT NOT NULL DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted_at TIMESTAMP NULL,
                        FOREIGN KEY (user_id) REFERENCES user(id),
                        INDEX idx_user_id (user_id),
                        INDEX idx_date_start (date_start),
                        INDEX idx_date_end (date_end),
                        INDEX idx_status (status)
);


CREATE TABLE ticket_item (
                             id CHAR(36) PRIMARY KEY,
                             ticket_id CHAR(36) NOT NULL,
                             name VARCHAR(255) NOT NULL,
                             description TEXT,
                             stock_initial INT NOT NULL,
                             stock_available INT NOT NULL,
                             is_stock_prepared BOOLEAN NOT NULL DEFAULT FALSE,
                             price_original BIGINT NOT NULL,
                             price_flash BIGINT NOT NULL,
                             sale_start_time DATETIME NOT NULL,
                             sale_end_time DATETIME NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             deleted_at TIMESTAMP NULL,
                             FOREIGN KEY (ticket_id) REFERENCES ticket(id),
                             INDEX idx_ticket_id (ticket_id),
                             INDEX idx_sale_start_time (sale_start_time),
                             INDEX idx_sale_end_time (sale_end_time)
);


CREATE TABLE IF EXISTS orders (
                                  id CHAR(36) PRIMARY KEY,
                                  user_id CHAR(36) NOT NULL,
                                  ticket_item_id CHAR(36) NOT NULL,
                                  quantity INT NOT NULL,
                                  unit_price BIGINT NOT NULL,
                                  total_price BIGINT NOT NULL,
                                  status INT NOT NULL DEFAULT 0,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  deleted_at TIMESTAMP NULL,
                                  INDEX idx_orders_user_id (user_id),
                                  INDEX idx_orders_ticket_item_id (ticket_item_id),
                                  INDEX idx_orders_status (status),
                                  INDEX idx_orders_created_at (created_at)
);

CREATE TABLE IF EXISTS payments (
                                    id CHAR(36) PRIMARY KEY,
                                    order_id CHAR(36) NOT NULL,
                                    amount BIGINT NOT NULL,
                                    payment_method VARCHAR(50) NOT NULL,
                                    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
                                    transaction_id VARCHAR(100) NULL,
                                    paid_at DATETIME NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    UNIQUE KEY uq_payments_transaction_id (transaction_id),
                                    INDEX idx_payments_order_id (order_id),
                                    INDEX idx_payments_status (status),
                                    INDEX idx_payments_paid_at (paid_at),
                                    CONSTRAINT fk_payments_order_id
                                        FOREIGN KEY (order_id) REFERENCES orders(id)
);




