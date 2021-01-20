CREATE TABLE `api_document` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `parent_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【上级类型】application,interface',
  `parent_id` bigint(20) NOT NULL COMMENT '【上级ID】',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【标题】',
  `note` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【说明】',
  `format` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'text' COMMENT '【内容格式】text,java,js,markdown,html,json,xml,...',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '【内容体】',
  `permission` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'public' COMMENT '【权限】公开（public），保护（protected），私有（private）',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='【接口文档表】';

