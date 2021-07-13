/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_application   */
/******************************************/
CREATE TABLE `api_application` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【名称】唯一',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【标题】',
  `note` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '【说明】',
  `owner` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【作者】',
  `config` text COLLATE utf8mb4_general_ci COMMENT '【配置】json',
  `options` varchar(4000) COLLATE utf8mb4_general_ci DEFAULT '{}',
  `error_codes` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【错误码】',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【版本号】',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口应用表'
;

/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_document   */
/******************************************/
CREATE TABLE `api_document` (
  `id` bigint(20) NOT NULL COMMENT '【编号】自增长编号',
  `parent_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【上级类型】application,interface',
  `parent_id` bigint(20) NOT NULL COMMENT '【上级ID】',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【标题】',
  `note` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【说明】',
  `format` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'text' COMMENT '【内容格式】text,java,js,markdown,html,json,xml,...',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '【内容体】',
  `permission` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'public' COMMENT '【权限】公开（public），保护（protected），私有（private）',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `sort` int(11) DEFAULT '0',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='【接口文档表】'
;

/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_filter   */
/******************************************/
CREATE TABLE `api_filter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `parent_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【上级类型】application,interface',
  `parent_id` bigint(20) NOT NULL COMMENT '【上级ID】',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【标题】',
  `config` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【配置】 json',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '【排序】',
  `implement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现类型】groovy/bean,...',
  `implement_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现代码】',
  `point` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【过滤位置】enter,execute,return',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1616728435388 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='【接口过滤器表】'
;

/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_interface   */
/******************************************/
CREATE TABLE `api_interface` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `application_id` bigint(20) NOT NULL COMMENT '【应用ID】',
  `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【名称】 应用 + 名称 唯一',
  `group_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【分组名称】',
  `title` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【标题】',
  `note` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【说明】',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '【排序】',
  `method` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【方法】get/put/post/delete',
  `request_example` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【调用示例】支持mock变量',
  `response_example` varchar(5000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【返回示例】支持mock变量',
  `config` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【配置】json',
  `implement_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现类型】groovy/bean,...',
  `implement_code` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【实现代码】',
  `error_codes` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【错误码】',
  `qos_config` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cache_config` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `version` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【版本号】',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mena_unique` (`method`,`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1617423717195 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口方法表'
;

/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_parameter   */
/******************************************/
CREATE TABLE `api_parameter` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `parent_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【上级类型】application,interface,parameter',
  `parent_id` bigint(20) NOT NULL COMMENT '【上级ID】',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【名称】上级+名称 唯一',
  `alias` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【别称】json array 别称,多个可用参数值将映射到第一个区配的值如 参数名称id,别称uid,gid,uuid传入id=&uid=&gid=100&uuid=200,最后参数id取值为100 ',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【标题】',
  `note` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【说明】',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '【排序】',
  `position` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'body' COMMENT '【取值位置】query header cookie form body 默认"body" , 为子参数不填,时同上级',
  `default_value` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【默认值】',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT 'string' COMMENT '【数据类型】string,int,long,double,boolean,date,object',
  `is_array` int(2) NOT NULL DEFAULT '0' COMMENT '【是否数组】0否 1是',
  `is_required` int(2) NOT NULL DEFAULT '0' COMMENT '【是否必填】0否 1是',
  `is_hidden` int(2) NOT NULL DEFAULT '0' COMMENT '【是否隐藏】0否 1是 隐藏时文档中不显示参数,但可以传入 ',
  `is_read_only` int(2) NOT NULL DEFAULT '0' COMMENT '【是否只读】0否 1是 只读时不能传入参数,只取默认值',
  `examples` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【示例值】json array 支持mock变量',
  `validations` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【参数验证】json Validation格式',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_p_name` (`parent_type`,`parent_id`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1617424046049 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='【接口参数表】'
;

/******************************************/
/*   DatabaseName = fuapi   */
/*   TableName = api_source   */
/******************************************/
CREATE TABLE `api_source` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '【编号】自增长编号',
  `application_id` bigint(20) NOT NULL COMMENT '【应用ID】',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '【类型】database,redis',
  `name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '【名称】 名称 唯一',
  `note` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【说明】',
  `config` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '【配置参数】YAML',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '【状态】0正常 1停用',
  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '【创建时间】',
  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '【修改时间】',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_unique` (`application_id`,`type`,`name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1617263861553 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口资源表'
;
