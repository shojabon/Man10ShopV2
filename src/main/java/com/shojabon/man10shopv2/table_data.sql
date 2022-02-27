CREATE DATABASE IF NOT EXISTS `man10_shop` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `man10_shop`;

CREATE TABLE IF NOT EXISTS `man10shop_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(64) DEFAULT NULL,
  `log_type` varchar(64) DEFAULT NULL,
  `value` text,
  `name` varchar(64) DEFAULT NULL,
  `uuid` varchar(64) DEFAULT NULL,
  `date_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `man10shop_permissions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `uuid` varchar(64) DEFAULT NULL,
  `shop_id` varchar(64) DEFAULT NULL,
  `permission` varchar(64) DEFAULT NULL,
  `notification` varchar(25) DEFAULT NULL,
  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `man10shop_settings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(64) DEFAULT NULL,
  `unique_setting_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `key` varchar(256) DEFAULT NULL,
  `value` text,
  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UNIQUE` (`unique_setting_hash`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `man10shop_shops` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(64) DEFAULT NULL,
  `name` varchar(128) DEFAULT NULL,
  `money` int DEFAULT NULL,
  `target_item` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `target_item_hash` varchar(64) DEFAULT NULL,
  `target_item_count` int DEFAULT NULL,
  `price` int DEFAULT NULL,
  `shop_type` varchar(64) DEFAULT NULL,
  `deleted` tinyint DEFAULT NULL,
  `admin` varchar(64) DEFAULT 'false',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `man10shop_signs` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(64) NOT NULL,
  `location_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0',
  `world` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `x` int NOT NULL DEFAULT '0',
  `y` int NOT NULL DEFAULT '0',
  `z` int NOT NULL DEFAULT '0',
  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `インデックス 2` (`location_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE IF NOT EXISTS `man10shop_trade_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `shop_id` varchar(50) DEFAULT NULL,
  `action` varchar(50) DEFAULT NULL,
  `amount` int DEFAULT NULL,
  `total_price` int DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `uuid` varchar(50) DEFAULT NULL,
  `date_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `man10shop_lootbox_log` (
	`id` INT(10) NOT NULL AUTO_INCREMENT,
	`shop_id` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`win_item_hash` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`win_item_name` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`winner_name` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`winner_uuid` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`date_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
;
