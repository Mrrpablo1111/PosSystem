-- =========================================================
-- CREATE STORE TYPES
-- =========================================================

CREATE TABLE store_types (
    id BIGINT NOT NULL AUTO_INCREMENT,

    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_store_types_code
        UNIQUE (code)
);

-- =========================================================
-- SEED INITIAL STORE TYPES
-- =========================================================

INSERT INTO store_types (
    code,
    name,
    description,
    active,
    created_at
)
VALUES
(
    'RICE_SHOP',
    'Rice Shop',
    'Rice retail and wholesale businesses',
    TRUE,
    CURRENT_TIMESTAMP(6)
),
(
    'CLOTHING',
    'Clothing Store',
    'Clothing, apparel, and fashion retail',
    TRUE,
    CURRENT_TIMESTAMP(6)
),
(
    'RESTAURANT',
    'Restaurant',
    'Restaurants and food service businesses',
    TRUE,
    CURRENT_TIMESTAMP(6)
),
(
    'COFFEE_SHOP',
    'Coffee Shop',
    'Coffee shops and beverage businesses',
    TRUE,
    CURRENT_TIMESTAMP(6)
),
(
    'GROCERY',
    'Grocery Store',
    'General grocery and convenience retail',
    TRUE,
    CURRENT_TIMESTAMP(6)
);