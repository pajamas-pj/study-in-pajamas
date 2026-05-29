-- 题目主表
CREATE TABLE `question` (
                            `id` BIGINT NOT NULL COMMENT '题目ID',
                            `type` TINYINT NOT NULL COMMENT '题目类型：1-单选题 2-多选题 3-判断题 4-简答题',
                            `content` TEXT NOT NULL COMMENT '题干内容',
                            `score` INT NOT NULL DEFAULT 0 COMMENT '分值',
                            `difficulty` TINYINT NOT NULL DEFAULT 1 COMMENT '难度：1-简单 2-普通 3-困难',
                            `answer_times` INT NOT NULL DEFAULT 0 COMMENT '答题次数',
                            `correct_times` INT NOT NULL DEFAULT 0 COMMENT '正确次数',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            PRIMARY KEY (`id`),
                            KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试题目表';

CREATE TABLE `question_detail` (
                                   `question_id` BIGINT NOT NULL COMMENT '题目ID',
                                   `reference_answer` TEXT COMMENT '简答题参考答案',
                                   `explanation` TEXT COMMENT '答案解析',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目详情表';

CREATE TABLE `question_option` (
                                   `id` BIGINT NOT NULL COMMENT '选项ID',
                                   `question_id` BIGINT NOT NULL COMMENT '所属题目ID',
                                   `option_label` VARCHAR(10) NOT NULL COMMENT '选项标签：A、B、C、D',
                                   `option_content` VARCHAR(1000) NOT NULL COMMENT '选项内容，或"正确""错误"',
                                   `is_correct` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为正确答案：0-否 1-是',
                                   `sort_order` INT NOT NULL DEFAULT 0 COMMENT '选项排序（如A→B→C→D）',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`id`),
                                   KEY `idx_question_id` (`question_id`),
                                   KEY `idx_question_sort` (`question_id`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';
