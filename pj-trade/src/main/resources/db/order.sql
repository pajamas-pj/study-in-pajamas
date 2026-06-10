CREATE TABLE `order` (
                         id                BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
                         order_no          VARCHAR(32)  NOT NULL COMMENT '业务订单号',
                         user_id           BIGINT       NOT NULL COMMENT '用户id',
                         shop_id           BIGINT       NOT NULL DEFAULT 0 COMMENT '店铺id',
                         total_amount      BIGINT       NOT NULL COMMENT '商品总金额(分)',
                         discount_amount   BIGINT       NOT NULL DEFAULT 0 COMMENT '优惠总金额(分)',
                         pay_amount        BIGINT       NOT NULL COMMENT '实付金额(分)',
                         order_status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1-待支付 2-已支付 3-已完成 4-已关闭 5-退款中 6-已退款',
                         payment_method    TINYINT      DEFAULT NULL COMMENT '支付方式:1-微信 2-支付宝',
                         source            TINYINT      NOT NULL DEFAULT 0 COMMENT '来源:0-购物车 1-直接购买 2-活动',
                         remark            VARCHAR(200) DEFAULT NULL COMMENT '买家备注',
                         version           INT          NOT NULL DEFAULT 0 COMMENT '乐观锁',
                         extend            JSON         DEFAULT NULL COMMENT '扩展字段',
                         pay_time          DATETIME(3)  DEFAULT NULL COMMENT '支付时间',
                         close_time        DATETIME(3)  DEFAULT NULL COMMENT '关闭时间',
                         finish_time       DATETIME(3)  DEFAULT NULL COMMENT '完成时间',
                         create_time       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                         update_time       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
                         UNIQUE KEY uk_order_no (order_no),
                         KEY idx_user_id (user_id),
                         KEY idx_shop_id (shop_id),
                         KEY idx_pay_time (pay_time),
                         KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

CREATE TABLE order_item (
                            id               BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
                            order_id         BIGINT       NOT NULL COMMENT '订单id',
                            user_id          BIGINT       NOT NULL COMMENT '用户id(冗余,分片)',
                            shop_id          BIGINT       NOT NULL DEFAULT 0 COMMENT '店铺id(冗余)',
                            spu_id           BIGINT       NOT NULL COMMENT 'SPU id',
                            sku_id           BIGINT       NOT NULL COMMENT 'SKU id',
                            item_name        VARCHAR(200) NOT NULL COMMENT '商品名称(快照)',
                            item_cover       VARCHAR(500) DEFAULT NULL COMMENT '商品封面(快照)'  ,
                            price            BIGINT       NOT NULL COMMENT '原价(分)',
                            discount_amount  BIGINT       NOT NULL DEFAULT 0 COMMENT '优惠分摊(分)',
                            pay_amount       BIGINT       NOT NULL COMMENT '实付小计(分)',
                            quantity         INT          NOT NULL DEFAULT 1 COMMENT '数量',
                            activity_id      BIGINT       DEFAULT NULL COMMENT '活动id',
                            extend           JSON         DEFAULT NULL COMMENT '扩展字段',
                            create_time      DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                            KEY idx_order_id (order_id),
                            KEY idx_user_id (user_id),
                            KEY idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

CREATE TABLE order_log (
                           id          BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
                           order_id    BIGINT       NOT NULL COMMENT '订单id',
                           from_status TINYINT      DEFAULT NULL COMMENT '原状态',
                           to_status   TINYINT      NOT NULL COMMENT '新状态',
                           operator    VARCHAR(50)  DEFAULT 'SYSTEM' COMMENT '操作人',
                           reason      VARCHAR(200) DEFAULT NULL COMMENT '操作原因',
                           create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                           KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态日志';

CREATE TABLE refund_order (
                              id              BIGINT       NOT NULL PRIMARY KEY COMMENT '主键',
                              refund_no       VARCHAR(32)  NOT NULL COMMENT '退款单号',
                              order_id        BIGINT       NOT NULL COMMENT '原订单id',
                              order_item_id   BIGINT       DEFAULT NULL COMMENT '明细id(null=整单退款)',
                              user_id         BIGINT       NOT NULL COMMENT '用户id',
                              refund_amount   BIGINT       NOT NULL COMMENT '退款金额(分)',
                              refund_status   TINYINT      NOT NULL DEFAULT 1 COMMENT '状态:1-待处理 2-已同意 3-已退款 4-已拒绝',
                              refund_reason   VARCHAR(500) DEFAULT NULL COMMENT '退款原因',
                              reject_reason   VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
                              audit_time      DATETIME(3)  DEFAULT NULL COMMENT '审核时间',
                              finish_time     DATETIME(3)  DEFAULT NULL COMMENT '退款完成时间',
                              create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                              update_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

                              UNIQUE KEY uk_refund_no (refund_no),
                              KEY idx_order_id (order_id),
                              KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款单';