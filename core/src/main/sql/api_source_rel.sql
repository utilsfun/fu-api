CREATE TABLE `api_source_rel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `application_id` bigint(20) NOT NULL COMMENT '【应用ID】',
  `source_id` bigint(20) NOT NULL COMMENT '【应用ID】',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `application_source_id_unique` (`application_id`,source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口应用引用资源表';