CREATE TABLE `api_filter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `parent_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【上级类型】application,interface',
  `parent_id` bigint(20) NOT NULL COMMENT '【上级ID】',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【标题】',
  `config` varchar(4000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【配置】 json',
  `sort` int NOT NULL DEFAULT '0' COMMENT '【排序】',
  `implement_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现类型】groovy/bean,...',
  `implement_code` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现代码】',
  `point` varchar(20) COLLATE utf8mb4_general_ci  NOT NULL COMMENT '【过滤位置】enter,execute,return',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='【接口过滤器表】';