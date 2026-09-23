-- RECRUITFLOW_SAFE_ADDITIVE_MIGRATION
-- Administrator-managed public/candidate homepage campaign images.
CREATE TABLE IF NOT EXISTS home_banners (
    id INT NOT NULL AUTO_INCREMENT,
    image_path VARCHAR(255) NOT NULL,
    title VARCHAR(120) NULL,
    subtitle VARCHAR(300) NULL,
    target_url VARCHAR(500) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_home_banners_active_order (is_active, display_order, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
