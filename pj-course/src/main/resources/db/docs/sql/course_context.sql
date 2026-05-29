DROP TABLE IF EXISTS `course_content`;
CREATE TABLE `course_content` (
                                  `id` BIGINT NOT NULL COMMENT '课程id',
                                  `course_introduce` VARCHAR(500) DEFAULT NULL COMMENT '课程介绍',
                                  `use_people` VARCHAR(255) DEFAULT NULL COMMENT '适用人群',
                                  `course_detail`MEDIUMTEXT  COMMENT '课程详情',
                                  `dep_id` BIGINT DEFAULT NULL COMMENT '部门id',
                                  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `creator` BIGINT DEFAULT NULL COMMENT '创建人',
                                  `updater` BIGINT DEFAULT NULL COMMENT '更新人',
                                  `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0-未删除，1-已删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_dep_id` (`dep_id`),
                                  CONSTRAINT `pj_content_course` FOREIGN KEY (`id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程内容详情表';
