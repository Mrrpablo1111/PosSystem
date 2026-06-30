-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: pos_system
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `branch_working_days`
--

DROP TABLE IF EXISTS `branch_working_days`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branch_working_days` (
  `branch_id` bigint NOT NULL,
  `working_days` varchar(255) DEFAULT NULL,
  KEY `FK1pw0iposltokqnwyygxhix9oy` (`branch_id`),
  CONSTRAINT `FK1pw0iposltokqnwyygxhix9oy` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `branches`
--

DROP TABLE IF EXISTS `branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branches` (
  `id` bigint NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `branch_type` enum('STORE','WAREHOUSE') NOT NULL,
  `close_time` time(6) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `open_time` time(6) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9sgoag24qgk3nabu6g7416ijm` (`manager_id`),
  KEY `FKc2bywqbckf4tnouya6o5q9rbg` (`store_id`),
  CONSTRAINT `FKaxphe54ft6x2k2ndo8t5vsvjo` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKc2bywqbckf4tnouya6o5q9rbg` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `branches_seq`
--

DROP TABLE IF EXISTS `branches_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branches_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKswh8ov7e46aj6bf053xla2c6b` (`store_id`),
  CONSTRAINT `FKswh8ov7e46aj6bf053xla2c6b` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `currencies`
--

DROP TABLE IF EXISTS `currencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `currencies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `code` varchar(10) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `symbol` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5r2dfxl1m7vus47ma0y05sflt` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer`
--

DROP TABLE IF EXISTS `customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `price_group_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfurqob8mvxje4dflgwjdkr6n2` (`price_group_id`),
  CONSTRAINT `FKfurqob8mvxje4dflgwjdkr6n2` FOREIGN KEY (`price_group_id`) REFERENCES `price_groups` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `exchange_rates`
--

DROP TABLE IF EXISTS `exchange_rates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exchange_rates` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `rate` decimal(19,6) NOT NULL,
  `rate_date` date NOT NULL,
  `source` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `from_currency_id` bigint NOT NULL,
  `to_currency_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhkch9xmcqrklukbwkv5q3jf7p` (`from_currency_id`),
  KEY `FK7mxjkyobm460mictds6935plq` (`to_currency_id`),
  CONSTRAINT `FK7mxjkyobm460mictds6935plq` FOREIGN KEY (`to_currency_id`) REFERENCES `currencies` (`id`),
  CONSTRAINT `FKhkch9xmcqrklukbwkv5q3jf7p` FOREIGN KEY (`from_currency_id`) REFERENCES `currencies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventories`
--

DROP TABLE IF EXISTS `inventories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventories` (
  `id` bigint NOT NULL,
  `last_update` datetime(6) DEFAULT NULL,
  `low_stock_alert` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `reorder_level` int NOT NULL,
  `reserved_quantity` int NOT NULL,
  `branch_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKp5ux0ijhbxmbxcp04ae1t4kdx` (`branch_id`,`product_id`),
  KEY `idx_inventory_branch_product` (`branch_id`,`product_id`),
  KEY `FK8drmqyx629j3oo8ct9jnc5y3y` (`product_id`),
  CONSTRAINT `FK8drmqyx629j3oo8ct9jnc5y3y` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKgs563w23gys2co425wraxaj1o` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventories_seq`
--

DROP TABLE IF EXISTS `inventories_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventories_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventory_movements`
--

DROP TABLE IF EXISTS `inventory_movements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_movements` (
  `id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL,
  `type` enum('ADJUSTMENT','DAMAGE','PURCHASE','RETURN','SALE','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
  `branch_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXroxi7j4pmeibyk0wbe6pcpw4a` (`branch_id`,`product_id`,`created_at`),
  KEY `FK7lws1ve8g6b9itc054wj06uit` (`product_id`),
  CONSTRAINT `FK7lws1ve8g6b9itc054wj06uit` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKns3158juqxfbpdo8l6pr1faqd` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventory_movements_seq`
--

DROP TABLE IF EXISTS `inventory_movements_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory_movements_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(38,2) DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `grand_total` decimal(38,2) DEFAULT NULL,
  `payment_type` tinyint DEFAULT NULL,
  `receipt_number` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT NULL,
  `tax_amount` decimal(38,2) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `cashier_id` bigint DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjcfql8n80mxan8of04c8sd8k2` (`branch_id`),
  KEY `FKmb88e3a35geg9rxiefr1qma88` (`cashier_id`),
  KEY `FK624gtjin3po807j3vix093tlf` (`customer_id`),
  CONSTRAINT `FK624gtjin3po807j3vix093tlf` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`),
  CONSTRAINT `FKjcfql8n80mxan8of04c8sd8k2` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKmb88e3a35geg9rxiefr1qma88` FOREIGN KEY (`cashier_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `po_email_logs`
--

DROP TABLE IF EXISTS `po_email_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `po_email_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email_type` enum('DAILY_SALES_REPORT','EXPIRY_ALERT','GOODS_RECEIVED','LOW_STOCK_ALERT','MONTHLY_REPORT','OVERDUE_PO_ALERT','PURCHASE_ORDER') DEFAULT NULL,
  `error_message` text,
  `sent_at` datetime(6) DEFAULT NULL,
  `sent_to` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  `subject` varchar(255) DEFAULT NULL,
  `purchase_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi6l9njjb6khevtddh0q35iguu` (`purchase_order_id`),
  CONSTRAINT `FKi6l9njjb6khevtddh0q35iguu` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_groups`
--

DROP TABLE IF EXISTS `price_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKikvh5cwq7em3v1hatlel8tmn8` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `price_rules`
--

DROP TABLE IF EXISTS `price_rules`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_percent` decimal(38,2) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `fixed_price` decimal(38,2) DEFAULT NULL,
  `markup_percent` decimal(38,2) DEFAULT NULL,
  `min_quantity` int NOT NULL,
  `priority` int NOT NULL,
  `start_date` date DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `price_group_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_price_rule_product_group` (`product_id`,`price_group_id`),
  KEY `idx_price_rule_branch` (`branch_id`),
  KEY `FKjni5juuni5k8c1o8l1qhsnix4` (`price_group_id`),
  KEY `FKdc3wyx27me3hk2364rjl62h3k` (`variant_id`),
  CONSTRAINT `FK21va9y7f5ev1rubbwjkfkby53` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKdc3wyx27me3hk2364rjl62h3k` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKjni5juuni5k8c1o8l1qhsnix4` FOREIGN KEY (`price_group_id`) REFERENCES `price_groups` (`id`),
  CONSTRAINT `FKmmi8ol5mtep5g07y6w1va7tg2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_batches`
--

DROP TABLE IF EXISTS `product_batches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_batches` (
  `id` bigint NOT NULL,
  `batch_no` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `expiry_date` date NOT NULL,
  `purchase_price` decimal(38,2) NOT NULL,
  `quantity` int NOT NULL,
  `remaining_quantity` int NOT NULL,
  `branch_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `supplier_id` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK2tnhovdebfcy2v4ao8y89xbau` (`branch_id`,`product_id`,`batch_no`),
  KEY `IDX92wbprj2yj4werp7atox8ur2q` (`expiry_date`),
  KEY `FKo2hwf6cltkf4qkdim5w29rbgq` (`product_id`),
  KEY `FK66p4q4vitmrt0cb7pjsl8bg3s` (`supplier_id`),
  KEY `FK1rxcu7i0iwtunbconfqkuev6s` (`variant_id`),
  CONSTRAINT `FK1rxcu7i0iwtunbconfqkuev6s` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FK66p4q4vitmrt0cb7pjsl8bg3s` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `FKlt93axdvwcerhn5e8dfh2lglg` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKo2hwf6cltkf4qkdim5w29rbgq` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_batches_seq`
--

DROP TABLE IF EXISTS `product_batches_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_batches_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_option_values`
--

DROP TABLE IF EXISTS `product_option_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_option_values` (
  `id` bigint NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `option_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7orjj0rl1umkara7dt22kt21c` (`option_id`,`value`),
  CONSTRAINT `FKmre6ippw97evhwrbl15ushuw` FOREIGN KEY (`option_id`) REFERENCES `product_options` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_option_values_seq`
--

DROP TABLE IF EXISTS `product_option_values_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_option_values_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_options`
--

DROP TABLE IF EXISTS `product_options`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_options` (
  `id` bigint NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmd35q3h2hhpkwuvy4hlwixcyf` (`product_id`,`name`),
  CONSTRAINT `FK8vv4f8fru80wxocwgxwsrow61` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_options_seq`
--

DROP TABLE IF EXISTS `product_options_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_options_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_variant_values`
--

DROP TABLE IF EXISTS `product_variant_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variant_values` (
  `id` bigint NOT NULL,
  `option_value_id` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK70gqh3qpx3ni5mgt3ax93a35p` (`option_value_id`),
  KEY `FKpbp54igkobql1af2fpow658fu` (`variant_id`),
  CONSTRAINT `FK70gqh3qpx3ni5mgt3ax93a35p` FOREIGN KEY (`option_value_id`) REFERENCES `product_option_values` (`id`),
  CONSTRAINT `FKpbp54igkobql1af2fpow658fu` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_variant_values_seq`
--

DROP TABLE IF EXISTS `product_variant_values_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variant_values_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `id` bigint NOT NULL,
  `active` bit(1) DEFAULT NULL,
  `barcode` varchar(255) DEFAULT NULL,
  `cost_price` decimal(38,2) DEFAULT NULL,
  `item_code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `selling_price` decimal(38,2) DEFAULT NULL,
  `sku` varchar(255) NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq935p2d1pbjm39n0063ghnfgn` (`sku`),
  UNIQUE KEY `UKt4d1u786mvkr03045sjt6ebus` (`barcode`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_variants_seq`
--

DROP TABLE IF EXISTS `product_variants_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL,
  `active` bit(1) NOT NULL,
  `barcode` varchar(255) DEFAULT NULL,
  `brand` varchar(255) DEFAULT NULL,
  `cost_price` decimal(38,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `default_reorder_level` int NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `mrp` decimal(38,2) NOT NULL,
  `name` varchar(255) NOT NULL,
  `selling_price` decimal(38,2) NOT NULL,
  `sku` varchar(255) NOT NULL,
  `unit` enum('BOTTLE','BOX','CAN','CUP','GRAM','KG','LITER','ML','PACK','PIECE','PLATE') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  `supplier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
  UNIQUE KEY `UKqfr8vf85k3q1xinifvsl1eynf` (`barcode`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  KEY `FKgcyffheofvmy2x5l78xam63mc` (`store_id`),
  KEY `FK6i174ixi9087gcvvut45em7fd` (`supplier_id`),
  CONSTRAINT `FK6i174ixi9087gcvvut45em7fd` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `FKgcyffheofvmy2x5l78xam63mc` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products_seq`
--

DROP TABLE IF EXISTS `products_seq`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products_seq` (
  `next_val` bigint DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_invoice_items`
--

DROP TABLE IF EXISTS `purchase_invoice_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_invoice_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `batch_no` varchar(255) DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `total_cost` decimal(38,2) DEFAULT NULL,
  `unit_cost` decimal(38,2) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `purchase_invoice_id` bigint DEFAULT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnrkxxlg2k74bmf8vo1nf155f` (`product_id`),
  KEY `FK3p8c0w7amab2k7i0nr5shosjb` (`purchase_invoice_id`),
  KEY `FKb0ek293yqipdwvtciuaad1wj4` (`variant_id`),
  CONSTRAINT `FK3p8c0w7amab2k7i0nr5shosjb` FOREIGN KEY (`purchase_invoice_id`) REFERENCES `purchase_invoices` (`id`),
  CONSTRAINT `FKb0ek293yqipdwvtciuaad1wj4` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKnrkxxlg2k74bmf8vo1nf155f` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_invoices`
--

DROP TABLE IF EXISTS `purchase_invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `balance_amount` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount` decimal(38,2) DEFAULT NULL,
  `invoice_date` date DEFAULT NULL,
  `invoice_number` varchar(255) NOT NULL,
  `paid_amount` decimal(38,2) DEFAULT NULL,
  `payment_status` enum('PAID','PARTIALLY_PAID','UNPAID') DEFAULT NULL,
  `status` enum('CANCELLED','DRAFT','POSTED') DEFAULT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `supplier_invoice_no` varchar(255) DEFAULT NULL,
  `tax` decimal(38,2) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `branch_id` bigint NOT NULL,
  `purchase_order_id` bigint DEFAULT NULL,
  `store_id` bigint NOT NULL,
  `supplier_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5liy2dbo5i8bfli5lcyoafg87` (`invoice_number`),
  KEY `FK6rjkl1qwbmwmgp5c9d62yteio` (`branch_id`),
  KEY `FK4rsbcp6ows95jtaabdspmml4q` (`purchase_order_id`),
  KEY `FK1wjhj55ehtxm86566k3m2vsb9` (`store_id`),
  KEY `FKa0fe6yai855h0xkk72fhmpk2a` (`supplier_id`),
  CONSTRAINT `FK1wjhj55ehtxm86566k3m2vsb9` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FK4rsbcp6ows95jtaabdspmml4q` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`),
  CONSTRAINT `FK6rjkl1qwbmwmgp5c9d62yteio` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKa0fe6yai855h0xkk72fhmpk2a` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_order_items`
--

DROP TABLE IF EXISTS `purchase_order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount_pct` decimal(38,2) NOT NULL,
  `note` varchar(300) DEFAULT NULL,
  `received_quantity` int NOT NULL,
  `remaining_quantity` int NOT NULL,
  `requested_quantity` int NOT NULL,
  `total_cost` decimal(38,2) NOT NULL,
  `unit_cost` decimal(38,2) NOT NULL,
  `product_id` bigint NOT NULL,
  `purchase_order_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKs16e5vrvsp8alu0xp8m3a2ol5` (`product_id`),
  KEY `FKo3yj8ocbw2kav38548t22hgh8` (`purchase_order_id`),
  KEY `FK8vvrbcsu89a1ddsrfygegjio` (`variant_id`),
  CONSTRAINT `FK8vvrbcsu89a1ddsrfygegjio` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKo3yj8ocbw2kav38548t22hgh8` FOREIGN KEY (`purchase_order_id`) REFERENCES `purchase_orders` (`id`),
  CONSTRAINT `FKs16e5vrvsp8alu0xp8m3a2ol5` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_orders`
--

DROP TABLE IF EXISTS `purchase_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `carrier_id` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount` decimal(38,2) DEFAULT NULL,
  `expected_delivery` date DEFAULT NULL,
  `notes` text,
  `order_date` date NOT NULL,
  `payment_due_date` date DEFAULT NULL,
  `payment_term_days` int DEFAULT NULL,
  `po_number` varchar(255) DEFAULT NULL,
  `receipt_confirmation_sent_at` datetime(6) DEFAULT NULL,
  `received_date` date DEFAULT NULL,
  `reference_no` varchar(255) DEFAULT NULL,
  `sent_to_supplier_at` datetime(6) DEFAULT NULL,
  `shipping_cost` decimal(38,2) DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','CLOSED','COMPLETED','DRAFT','OVERDUE','PARTIALLY_INVOICED','PARTIALLY_RECEIVED','PENDING','RECEIVED') NOT NULL,
  `subtotal` decimal(38,2) DEFAULT NULL,
  `tax` decimal(38,2) DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `tracking_no` varchar(100) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `branch_id` bigint NOT NULL,
  `currency_id` bigint DEFAULT NULL,
  `supplier_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpbiykvcpyg0jslne4gviyeuc2` (`po_number`),
  KEY `FKcro9u222fvejvt89euowf35pq` (`branch_id`),
  KEY `FKqpg5h9tfoy19i8bt6a1ju2fmb` (`currency_id`),
  KEY `FKrpdasmb8y8xs5tiy4369xpinq` (`supplier_id`),
  CONSTRAINT `FKcro9u222fvejvt89euowf35pq` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKqpg5h9tfoy19i8bt6a1ju2fmb` FOREIGN KEY (`currency_id`) REFERENCES `currencies` (`id`),
  CONSTRAINT `FKrpdasmb8y8xs5tiy4369xpinq` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refund`
--

DROP TABLE IF EXISTS `refund`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `payment_type` tinyint DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `cashier_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `shift_report_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9k4bp7f3ke2e8kch7pcc23w94` (`branch_id`),
  KEY `FKaxo80pd4rmxa36iuyojfq2u58` (`cashier_id`),
  KEY `FK80vls36avhp4yl7h8apkqm0ek` (`order_id`),
  KEY `FKatogwtp6icia1k4qqpsf2th1r` (`shift_report_id`),
  CONSTRAINT `FK80vls36avhp4yl7h8apkqm0ek` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FK9k4bp7f3ke2e8kch7pcc23w94` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKatogwtp6icia1k4qqpsf2th1r` FOREIGN KEY (`shift_report_id`) REFERENCES `shift_report` (`id`),
  CONSTRAINT `FKaxo80pd4rmxa36iuyojfq2u58` FOREIGN KEY (`cashier_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shift_report`
--

DROP TABLE IF EXISTS `shift_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cash_difference` decimal(38,2) DEFAULT NULL,
  `closing_cash` decimal(38,2) DEFAULT NULL,
  `expected_cash` decimal(38,2) DEFAULT NULL,
  `net_sale` decimal(38,2) DEFAULT NULL,
  `opening_cash` decimal(38,2) DEFAULT NULL,
  `shift_end` datetime(6) DEFAULT NULL,
  `shift_start` datetime(6) DEFAULT NULL,
  `total_order` int NOT NULL,
  `total_refunds` decimal(38,2) DEFAULT NULL,
  `total_sales` decimal(38,2) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `cashier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6w19s2mrbru4cx0kh7ieuqwag` (`branch_id`),
  KEY `FKs4b2eu1cee26h7r2qq8gdnouk` (`cashier_id`),
  CONSTRAINT `FK6w19s2mrbru4cx0kh7ieuqwag` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKs4b2eu1cee26h7r2qq8gdnouk` FOREIGN KEY (`cashier_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shift_report_recent_orders`
--

DROP TABLE IF EXISTS `shift_report_recent_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_report_recent_orders` (
  `shift_report_id` bigint NOT NULL,
  `recent_orders_id` bigint NOT NULL,
  UNIQUE KEY `UK4qcxery98b5mrxrjxsw7lk0ji` (`recent_orders_id`),
  KEY `FK615h2mu7ahop8urx9pcsgnwm3` (`shift_report_id`),
  CONSTRAINT `FK615h2mu7ahop8urx9pcsgnwm3` FOREIGN KEY (`shift_report_id`) REFERENCES `shift_report` (`id`),
  CONSTRAINT `FK93b47pajiyh5ivu5uegctjmp9` FOREIGN KEY (`recent_orders_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shift_report_top_selling_products`
--

DROP TABLE IF EXISTS `shift_report_top_selling_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shift_report_top_selling_products` (
  `shift_report_id` bigint NOT NULL,
  `top_selling_products_id` bigint NOT NULL,
  UNIQUE KEY `UKbokfws1v9iwho24l3q6uqgk9h` (`top_selling_products_id`),
  KEY `FKlrgy6u0ka32yjmyqkbbmoda9g` (`shift_report_id`),
  CONSTRAINT `FKhm8uivkc820l6jknmj3d3qf99` FOREIGN KEY (`top_selling_products_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKlrgy6u0ka32yjmyqkbbmoda9g` FOREIGN KEY (`shift_report_id`) REFERENCES `shift_report` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_movements`
--

DROP TABLE IF EXISTS `stock_movements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_movements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `movement_type` enum('ADJUSTMENT','DAMAGE_OUT','EXPIRED_OUT','IN','OUT','RETURN_IN','RETURN_OUT','TRANSFER_IN','TRANSFER_OUT') NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `quantity` int NOT NULL,
  `reference_id` bigint DEFAULT NULL,
  `reference_type` enum('DAMAGE','EXPIRED','OPENING_STOCK','PURCHASE','PURCHASE_INVOICE','PURCHASE_RETURN','SALE','SALE_RETURN','STOCK_ADJUSTMENT','STOCK_TRANSFER') NOT NULL,
  `total_cost` decimal(38,2) NOT NULL,
  `unit_cost` decimal(38,2) NOT NULL,
  `batch_id` bigint DEFAULT NULL,
  `branch_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_stock_branch_product` (`branch_id`,`product_id`),
  KEY `idx_stock_variant` (`variant_id`),
  KEY `idx_stock_batch` (`batch_id`),
  KEY `idx_stock_created_at` (`created_at`),
  KEY `FKjcaag8ogfjxpwmqypi1wfdaog` (`product_id`),
  CONSTRAINT `FKjcaag8ogfjxpwmqypi1wfdaog` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKknptp2x5ng8ghcyikkxn5v7rj` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKo3rdw1xgft64g9fr8d3rnbt1q` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKqpfwbaxwvri2cxtahci1qjq6e` FOREIGN KEY (`batch_id`) REFERENCES `product_batches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_request_items`
--

DROP TABLE IF EXISTS `stock_request_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_request_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `product_id` bigint NOT NULL,
  `request_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKf68qjx4h82qifxnwmaoi0qns1` (`product_id`),
  KEY `FK3cbtuqer2q8fmv51i8fkopfeu` (`request_id`),
  KEY `FKlcaphl0ecnro14jpik8n2htpx` (`variant_id`),
  CONSTRAINT `FK3cbtuqer2q8fmv51i8fkopfeu` FOREIGN KEY (`request_id`) REFERENCES `stock_requests` (`id`),
  CONSTRAINT `FKf68qjx4h82qifxnwmaoi0qns1` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKlcaphl0ecnro14jpik8n2htpx` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_requests`
--

DROP TABLE IF EXISTS `stock_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `request_no` varchar(255) DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','CONVERTED_TO_TRANSFER','PENDING','REJECTED') NOT NULL,
  `approved_by_id` bigint DEFAULT NULL,
  `from_branch_id` bigint NOT NULL,
  `requested_by_id` bigint DEFAULT NULL,
  `to_branch_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb34dnbc31kny2be6296hvins5` (`approved_by_id`),
  KEY `FK915cprdvkpgwajmkv22c1il83` (`from_branch_id`),
  KEY `FKetd5y1qjbj2giaer605a0i0tv` (`requested_by_id`),
  KEY `FKger3a7ehpfyw0d6yruoxsngo3` (`to_branch_id`),
  CONSTRAINT `FK915cprdvkpgwajmkv22c1il83` FOREIGN KEY (`from_branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKb34dnbc31kny2be6296hvins5` FOREIGN KEY (`approved_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKetd5y1qjbj2giaer605a0i0tv` FOREIGN KEY (`requested_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKger3a7ehpfyw0d6yruoxsngo3` FOREIGN KEY (`to_branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_transfer_histories`
--

DROP TABLE IF EXISTS `stock_transfer_histories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transfer_histories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','DRAFT','IN_TRANSIT','PACKED','PICKING','RECEIVED','SHIPPED') NOT NULL,
  `changed_by_id` bigint DEFAULT NULL,
  `transfer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4doaqmt4pihfhxx4ba6prxppd` (`changed_by_id`),
  KEY `FKhcyrs5t2rik33rcjr1paoa5jh` (`transfer_id`),
  CONSTRAINT `FK4doaqmt4pihfhxx4ba6prxppd` FOREIGN KEY (`changed_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhcyrs5t2rik33rcjr1paoa5jh` FOREIGN KEY (`transfer_id`) REFERENCES `stock_transfers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_transfer_items`
--

DROP TABLE IF EXISTS `stock_transfer_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transfer_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `damaged_qty` int DEFAULT NULL,
  `missing_qty` int DEFAULT NULL,
  `received_qty` int DEFAULT NULL,
  `requested_qty` int NOT NULL,
  `shipped_qty` int NOT NULL,
  `batch_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `transfer_id` bigint NOT NULL,
  `variant_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl9byggf8s47llg9rnthwn5b0q` (`batch_id`),
  KEY `FKheah6y1jsmwmbtttmip3hm2q` (`product_id`),
  KEY `FKertcckucd49nmw8ley2j5pxmh` (`transfer_id`),
  KEY `FKgd0ayfrcw3qwdt2p65cymuvw6` (`variant_id`),
  CONSTRAINT `FKertcckucd49nmw8ley2j5pxmh` FOREIGN KEY (`transfer_id`) REFERENCES `stock_transfers` (`id`),
  CONSTRAINT `FKgd0ayfrcw3qwdt2p65cymuvw6` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`id`),
  CONSTRAINT `FKheah6y1jsmwmbtttmip3hm2q` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKl9byggf8s47llg9rnthwn5b0q` FOREIGN KEY (`batch_id`) REFERENCES `product_batches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_transfers`
--

DROP TABLE IF EXISTS `stock_transfers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_transfers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `carrier` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `driver_name` varchar(255) DEFAULT NULL,
  `driver_phone` varchar(255) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `received_at` datetime(6) DEFAULT NULL,
  `shipped_at` datetime(6) DEFAULT NULL,
  `status` enum('APPROVED','CANCELLED','DRAFT','IN_TRANSIT','PACKED','PICKING','RECEIVED','SHIPPED') NOT NULL,
  `tracking_no` varchar(255) DEFAULT NULL,
  `transfer_no` varchar(255) DEFAULT NULL,
  `vehicle_no` varchar(255) DEFAULT NULL,
  `from_branch_id` bigint NOT NULL,
  `received_by_id` bigint DEFAULT NULL,
  `request_id` bigint DEFAULT NULL,
  `shipped_by_id` bigint DEFAULT NULL,
  `to_branch_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfl7cgq3gi53g313v51dky6pd5` (`from_branch_id`),
  KEY `FKley5eaw6g9bq4u4pu52yy8rvp` (`received_by_id`),
  KEY `FKpbh5sfxek37v6grbrb3w6crdn` (`request_id`),
  KEY `FKbfi7r9ywlp3onulcwh8sopy0m` (`shipped_by_id`),
  KEY `FKsqcl96ejd42qe81pqw69wj1vc` (`to_branch_id`),
  CONSTRAINT `FKbfi7r9ywlp3onulcwh8sopy0m` FOREIGN KEY (`shipped_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKfl7cgq3gi53g313v51dky6pd5` FOREIGN KEY (`from_branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `FKley5eaw6g9bq4u4pu52yy8rvp` FOREIGN KEY (`received_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpbh5sfxek37v6grbrb3w6crdn` FOREIGN KEY (`request_id`) REFERENCES `stock_requests` (`id`),
  CONSTRAINT `FKsqcl96ejd42qe81pqw69wj1vc` FOREIGN KEY (`to_branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `store_mail_configs`
--

DROP TABLE IF EXISTS `store_mail_configs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store_mail_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `app_password` text NOT NULL,
  `from_email` varchar(255) NOT NULL,
  `from_name` varchar(255) DEFAULT NULL,
  `smtp_host` varchar(255) DEFAULT NULL,
  `smtp_port` int DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcqadglkad4vht51eny7edlawm` (`store_id`),
  CONSTRAINT `FKsotqpkd43ud5c0hppbu2fpkf5` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stores`
--

DROP TABLE IF EXISTS `stores`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stores` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `brand` varchar(255) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','BLOCK','PENDING') NOT NULL,
  `store_type` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_admin_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKb1981d6rapqjv3hdqbg0h03l2` (`store_admin_id`),
  CONSTRAINT `FKn34o6e266esj3ickq4ecol23k` FOREIGN KEY (`store_admin_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_payments`
--

DROP TABLE IF EXISTS `supplier_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_paid` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `note` varchar(255) DEFAULT NULL,
  `payment_date` date DEFAULT NULL,
  `payment_method` enum('ABA','ACLEDA','BANK_TRANSFER','CASH','CHEQUE','WING') DEFAULT NULL,
  `payment_number` varchar(255) DEFAULT NULL,
  `purchase_invoice_id` bigint DEFAULT NULL,
  `supplier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKggimbjtbl8xa5w8ft0iwyh8na` (`purchase_invoice_id`),
  KEY `FKdwv3fhnvnbuvd6h2ri8iuiw2q` (`supplier_id`),
  CONSTRAINT `FKdwv3fhnvnbuvd6h2ri8iuiw2q` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `FKggimbjtbl8xa5w8ft0iwyh8na` FOREIGN KEY (`purchase_invoice_id`) REFERENCES `purchase_invoices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `contact_person` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `credit_limit` decimal(38,2) DEFAULT NULL,
  `current_balance` decimal(38,2) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `opening_balance` decimal(38,2) DEFAULT NULL,
  `payment_term_days` int DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT NULL,
  `tax_number` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `store_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8kh5crh75ye2imfi5yv37p61o` (`code`),
  KEY `FK31osod3me2lknglh15pxr5ilb` (`store_id`),
  CONSTRAINT `FK31osod3me2lknglh15pxr5ilb` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` tinyint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `verified` bit(1) NOT NULL,
  `branch_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  KEY `FK9o70sp9ku40077y38fk4wieyk` (`branch_id`),
  KEY `FK7wra86jadsraitoewujbjj1pd` (`store_id`),
  CONSTRAINT `FK7wra86jadsraitoewujbjj1pd` FOREIGN KEY (`store_id`) REFERENCES `stores` (`id`),
  CONSTRAINT `FK9o70sp9ku40077y38fk4wieyk` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `warehouses`
--

DROP TABLE IF EXISTS `warehouses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warehouses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_default` bit(1) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `branch_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6herdbg4x5wp6gkor8epv73oc` (`code`),
  KEY `FKcyjuy4jv0nys4buvnm452bxmn` (`branch_id`),
  CONSTRAINT `FKcyjuy4jv0nys4buvnm452bxmn` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-30  4:59:36
