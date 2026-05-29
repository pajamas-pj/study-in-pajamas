CREATE DATABASE IF NOT EXISTS `pj_course`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
-- 使用数据库
USE `pj_course`;
-- 课程分类表
CREATE TABLE `category` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程分类id',
                            `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
                            `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类id，一级分类父id为0',
                            `level` TINYINT NOT NULL COMMENT '分类级别，1,2,3：代表一级分类，二级分类，三级分类',
                            `priority` INT NOT NULL DEFAULT 0 COMMENT '同级目录优先级，数字越小优先级越高，可以重复',
                            `status` TINYINT NOT NULL DEFAULT 1 COMMENT '课程分类状态，1：正常，0：禁用',
                            `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `creator` BIGINT COMMENT '创建者',
                            `updater` BIGINT COMMENT '更新者',
                            `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识，0：未删除，1：已删除',
                            PRIMARY KEY (`id`),
                            INDEX `idx_parent_id` (`parent_id`),
                            INDEX `idx_level` (`level`),
                            INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程分类表';


