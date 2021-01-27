CREATE TABLE `api_source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `type` varchar(20)  DEFAULT NULL COMMENT '【类型】mysql,redis',
  `name` varchar(20) NOT NULL COMMENT '【名称】 名称 唯一',
  `note` varchar(2000)  DEFAULT NULL COMMENT '【说明】',
  `config` varchar(5000)  DEFAULT NULL COMMENT '【配置参数】JSON，YAML',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口资源表';