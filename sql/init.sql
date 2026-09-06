-- ============================================================
-- 智能零售客服与订单协同平台 - 数据库初始化脚本
-- MySQL 8.x
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_retail
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_retail;

-- ---------------------------- 用户表 ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `password`    VARCHAR(64)  NOT NULL COMMENT 'MD5 密码',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端用户';

-- ---------------------------- 管理员表 ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录名',
    `name`        VARCHAR(50)  DEFAULT NULL COMMENT '姓名',
    `password`    VARCHAR(64)  NOT NULL COMMENT 'MD5 密码',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'STAFF' COMMENT 'ADMIN/STAFF',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端用户';

-- ---------------------------- 商品分类 ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(50) NOT NULL COMMENT '分类名',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
    `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类';

-- ---------------------------- 商品表 ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `category_id` BIGINT        NOT NULL COMMENT '分类ID',
    `name`        VARCHAR(100)  NOT NULL COMMENT '商品名',
    `price`       DECIMAL(10,2) NOT NULL COMMENT '单价',
    `description` VARCHAR(500)  DEFAULT NULL COMMENT '描述',
    `image`       VARCHAR(255)  DEFAULT NULL COMMENT '图片URL',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1起售 0停售',
    `sales`       INT           NOT NULL DEFAULT 0 COMMENT '累计销量（高频读取，缓存）',
    `stock`       INT           NOT NULL DEFAULT 999 COMMENT '库存',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品';

-- ---------------------------- 套餐表 ----------------------------
DROP TABLE IF EXISTS `setmeal`;
CREATE TABLE `setmeal` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100)  NOT NULL,
    `category_id` BIGINT        DEFAULT NULL,
    `price`       DECIMAL(10,2) NOT NULL,
    `description` VARCHAR(500)  DEFAULT NULL,
    `image`       VARCHAR(255)  DEFAULT NULL,
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1起售 0停售',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_setmeal_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐';

DROP TABLE IF EXISTS `setmeal_item`;
CREATE TABLE `setmeal_item` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `setmeal_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `copies`     INT    NOT NULL DEFAULT 1 COMMENT '份数',
    PRIMARY KEY (`id`),
    KEY `idx_setmeal` (`setmeal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐明细';

-- ---------------------------- 订单表 ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `order_no`    VARCHAR(32)   NOT NULL COMMENT '订单号（业务唯一）',
    `user_id`     BIGINT        NOT NULL COMMENT '下单用户',
    `amount`      DECIMAL(10,2) NOT NULL COMMENT '订单金额',
    `status`      TINYINT       NOT NULL DEFAULT 2 COMMENT '1待付款 2待接单 3制作中 5已完成 6已取消（线下点单，无配送环节）',
    `pay_status`  TINYINT       NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
    `remark`      VARCHAR(255)  DEFAULT NULL COMMENT '订单备注，如：少冰',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `order_id`      BIGINT        NOT NULL,
    `product_id`    BIGINT        NOT NULL,
    `product_name`  VARCHAR(100)  NOT NULL COMMENT '商品名快照',
    `product_image` VARCHAR(255)  DEFAULT NULL,
    `amount`        DECIMAL(10,2) NOT NULL COMMENT '下单单价快照',
    `number`        INT           NOT NULL DEFAULT 1 COMMENT '数量',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';

-- ---------------------------- 优惠活动 ----------------------------
DROP TABLE IF EXISTS `promotion`;
CREATE TABLE `promotion` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(100) NOT NULL COMMENT '活动标题',
    `type`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1满减 2折扣 3赠品',
    `content`     VARCHAR(255) NOT NULL COMMENT '规则描述',
    `start_time`  DATETIME     NOT NULL,
    `end_time`    DATETIME     NOT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1进行中 0停用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_promo_status_time` (`status`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠活动';

-- ---------------------------- AI 客服会话与消息 ----------------------------
DROP TABLE IF EXISTS `ai_chat_session`;
CREATE TABLE `ai_chat_session` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`              BIGINT       NOT NULL,
    `title`                VARCHAR(100) DEFAULT NULL COMMENT '会话标题（取首条提问）',
    `dify_conversation_id` VARCHAR(64)  DEFAULT NULL COMMENT 'Dify 会话ID',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_session_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 会话';

DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`  BIGINT       NOT NULL,
    `role`        VARCHAR(20)  NOT NULL COMMENT 'user/assistant/tool',
    `content`     TEXT         NOT NULL,
    `tool_name`   VARCHAR(50)  DEFAULT NULL COMMENT '命中的 Agent Tool',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_message_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 消息';

-- ============================================================
-- 演示数据
-- ============================================================

-- 账号：admin / 123456（MD5）
INSERT INTO `admin_user` (`username`, `name`, `password`, `role`, `status`)
VALUES ('admin', '管理员', 'e10adc3949ba59abbe56e057f20f883e', 'ADMIN', 1);

-- 演示用户：demo / 123456
INSERT INTO `user` (`username`, `nickname`, `phone`, `password`, `status`)
VALUES ('demo', '演示用户', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 1);

INSERT INTO `category` (`name`, `sort`, `status`) VALUES
('茶饮', 1, 1),
('冰淇淋', 2, 1),
('咖啡', 3, 1),
('小食', 4, 1);

INSERT INTO `product` (`category_id`, `name`, `price`, `description`, `status`, `sales`, `stock`) VALUES
(1, '冰鲜柠檬水', 4.00, '新鲜柠檬切片，清爽解暑', 1, 999, 999),
(1, '珍珠奶茶', 8.00, 'Q弹珍珠配香浓奶茶', 1, 860, 999),
(1, '满杯百香果', 7.00, '满满百香果，酸甜开胃', 1, 520, 999),
(1, '蜜桃四季春', 7.00, '蜜桃果香配春茶底', 1, 430, 999),
(2, '脆皮冰淇淋', 3.00, '香脆外皮香草冰淇淋', 1, 700, 999),
(2, '雪王大圣代', 6.00, '满满一杯圣代，多种小料', 1, 610, 999),
(3, '拿铁咖啡', 8.00, '浓缩咖啡配鲜奶', 1, 320, 999),
(3, '美式咖啡', 6.00, '经典美式，提神醒脑', 1, 280, 999),
(4, '脆薯条', 5.00, '金黄酥脆薯条', 1, 410, 999),
(4, '鸡米花', 8.00, '外酥里嫩一口一个', 1, 360, 999);

INSERT INTO `setmeal` (`name`, `category_id`, `price`, `description`, `status`) VALUES
('下午茶双人餐', 1, 12.90, '冰鲜柠檬水2杯 + 脆薯条1份', 1);

INSERT INTO `promotion` (`title`, `type`, `content`, `start_time`, `end_time`, `status`) VALUES
('满20减3', 1, '订单满20元立减3元', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('第二杯半价', 2, '茶饮类第二杯半价', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1);

-- 演示订单（归属 demo 用户 id=1）：一单制作中、一单已完成，便于 AI 客服 Agent 演示订单查询
INSERT INTO `orders` (`id`, `order_no`, `user_id`, `amount`, `status`, `pay_status`, `remark`, `create_time`) VALUES
(1, CONCAT('DEMO', DATE_FORMAT(NOW(), '%Y%m%d'), '001'), 1, 8.00, 3, 1, '少冰、三分糖', DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
(2, CONCAT('DEMO', DATE_FORMAT(NOW(), '%Y%m%d'), '002'), 1, 13.00, 5, 1, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR));

INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `amount`, `number`) VALUES
(1, 2, '珍珠奶茶', 8.00, 1),
(2, 7, '拿铁咖啡', 8.00, 1),
(2, 9, '脆薯条', 5.00, 1);
