CREATE DATABASE if not exists  pj_trade;
USE pj_trade;
-- 购物车主表
CREATE TABLE cart (
                      id               BIGINT   NOT NULL PRIMARY KEY,
                      user_id          BIGINT   NOT NULL COMMENT '用户id',
                      shop_id          BIGINT   NOT NULL DEFAULT 0 COMMENT '店铺id 0=平台自营',
                      total_quantity   INT      NOT NULL DEFAULT 0 COMMENT '总件数',
                      item_count       INT      NOT NULL DEFAULT 0 COMMENT '商品种数（不同SKU数量）',
                      status           TINYINT  NOT NULL DEFAULT 1 COMMENT '1-有效 0-失效',
                      version          INT      NOT NULL DEFAULT 0 COMMENT '乐观锁',
                      last_access_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
                      create_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      update_time      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      UNIQUE KEY uk_user_shop (user_id, shop_id),
                      KEY idx_last_access_time (last_access_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- 购物车项表
CREATE TABLE cart_item (
                           id          BIGINT   NOT NULL PRIMARY KEY,
                           cart_id     BIGINT   NOT NULL COMMENT '购物车id',
                           user_id     BIGINT   NOT NULL COMMENT '用户id（冗余，用于分库分表）',
                           shop_id     BIGINT   NOT NULL DEFAULT 0 COMMENT '店铺id（冗余）',
                           spu_id      BIGINT   NOT NULL COMMENT 'SPU id',
                           sku_id      BIGINT   NOT NULL COMMENT 'SKU id',
                           activity_id BIGINT   DEFAULT NULL COMMENT '活动id',
                           quantity    INT      NOT NULL DEFAULT 0 COMMENT '数量',
                           price_at_add INT NOT NULL COMMENT '加购时单价,分，仅展示参考，结算价实时查询',
                           checked     TINYINT  NOT NULL DEFAULT 0 COMMENT '选中 1-选中 0-未选',
                           status      TINYINT  NOT NULL DEFAULT 1 COMMENT '1-正常 2-已失效(商品删除/下架)',
                           version     INT      NOT NULL DEFAULT 0 COMMENT '乐观锁',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           UNIQUE KEY uk_cart_sku (cart_id, sku_id),
                           KEY idx_cart_id (cart_id),
                           KEY idx_user_id (user_id),
                           KEY idx_spu_id (spu_id),
                           KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车项';