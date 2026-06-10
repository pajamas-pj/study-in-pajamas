CREATE DATABASE if not exists pj_stock;
use pj_stock;
-- 商品库存
CREATE TABLE stock (
                       sku_id     BIGINT NOT NULL PRIMARY KEY COMMENT 'SKU id',
                       quantity   INT    NOT NULL DEFAULT 0 COMMENT '总库存',
                       sold       INT    NOT NULL DEFAULT 0 COMMENT '已售',
                       frozen     INT    NOT NULL DEFAULT 0 COMMENT '预扣(未支付)',
                       version    INT    NOT NULL DEFAULT 0 COMMENT '乐观锁',
                       update_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                       KEY idx_sold (sold)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品库存';

-- 预扣流水（TCC Try 记录，用于 Confirm/Cancel）
CREATE TABLE stock_deduct (
                              id           BIGINT    NOT NULL  COMMENT '主键',
                              tx_id        VARCHAR(64) NOT NULL COMMENT '全局事务ID',
                              branch_id    BIGINT NOT NULL COMMENT '分支事务ID',
                              order_no     BIGINT    NOT NULL COMMENT '订单号用于查询',
                              sku_id       BIGINT    NOT NULL COMMENT 'SKU id',
                              quantity     INT       NOT NULL COMMENT '预扣数量',
                              version    INT    NOT NULL DEFAULT 0 COMMENT '乐观锁',
                              status       TINYINT   NOT NULL DEFAULT 1 COMMENT '1-TRIED 2-CONFIRMED 3-CANCELLED',
                              expire_time  DATETIME(3)  NOT NULL COMMENT '过期时间(订单超时时间)',
                              create_time  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                              update_time  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                              PRIMARY KEY (id),
                              KEY idx_expire_time (expire_time),
                              UNIQUE KEY `uk_tx_branch_product` (`tx_id`, `branch_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TCC事务记录';

