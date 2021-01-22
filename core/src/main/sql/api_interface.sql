CREATE TABLE `api_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `application_id` bigint(20) NOT NULL COMMENT '【应用ID】',
  `name` varchar(20) NOT NULL COMMENT '【名称】 应用 + 名称 唯一',
  `group` varchar(20)  DEFAULT NULL COMMENT '【分组名称】',
  `title` varchar(200)  DEFAULT NULL COMMENT '【标题】',
  `note` varchar(2000)  DEFAULT NULL COMMENT '【说明】',
  `sort` int NOT NULL DEFAULT '0' COMMENT '【排序】',
  `method` varchar(50)  DEFAULT NULL COMMENT '【方法】get/put/post/delete',
  `request_example` varchar(5000)  DEFAULT NULL COMMENT '【调用示例】支持mock变量',
  `response_example` varchar(5000)  DEFAULT NULL COMMENT '【返回示例】支持mock变量',
  `implement_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现类型】groovy/bean,...',
  `implement_code` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现代码】',
  `error_codes` varchar(2000)  DEFAULT NULL COMMENT '【错误码】',
  `version` varchar(20)  DEFAULT NULL COMMENT '【版本号】',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口方法表';