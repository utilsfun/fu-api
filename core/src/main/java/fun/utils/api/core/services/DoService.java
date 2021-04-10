package fun.utils.api.core.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.persistence.*;
import fun.utils.apijson.ApiJsonCaller;
import lombok.Getter;
import org.redisson.api.RedissonClient;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DoService {


    private final Cache<String, ApplicationDO> applicationCache;
    private final Cache<String, InterfaceDO> interfaceCache;
    private final Cache<String, DocumentDO> documentCache;
    private final Cache<String, FilterDO> filterCache;
    private final Cache<String, ParameterDO> parameterCache;
    private final Cache<String, SourceDO> databaseCache;
    private final Cache<String, SourceDO> redisCache;

    private final BeanPropertyRowMapper<ApplicationDO> applicationRowMapper;
    private final BeanPropertyRowMapper<SourceDO> sourceRowMapper;
    private final BeanPropertyRowMapper<InterfaceDO> interfaceRowMapper;
    private final BeanPropertyRowMapper<DocumentDO> documentRowMapper;
    private final BeanPropertyRowMapper<FilterDO> filterRowMapper;
    private final BeanPropertyRowMapper<ParameterDO> parameterRowMapper;


    @Resource(name = "fu-api.jdbc-template")
    @Getter
    private JdbcTemplate jdbcTemplate;

    @Resource(name = "fu-api.data-source")
    @Getter
    private DataSource dataSource;

    @Resource(name = "fu-api.redisson-client")
    @Getter
    private RedissonClient redissonClient;


    private ApiJsonCaller apiJsonCaller;

    private final long expireSeconds = 10;

    public DoService() {

        applicationCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();

        databaseCache = CacheBuilder.newBuilder().maximumSize(20).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
        redisCache = CacheBuilder.newBuilder().maximumSize(20).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();

        interfaceCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
        documentCache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
        filterCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();
        parameterCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterWrite(expireSeconds, TimeUnit.SECONDS).build();

        GenericConversionService conversionService = new DefaultConversionService();

        DefaultConversionService defaultConversionService = new DefaultConversionService();
        conversionService.addConverter(new JsonObjectConverter(defaultConversionService));
        conversionService.addConverter(new JsonArrayConverter(defaultConversionService));

        conversionService.addConverter(new Converter<String, JSON>() {
            @Override
            public JSON convert(String s) {
                return (JSON) JSON.parse(s);
            }
        });

        conversionService.addConverter(new Converter<String, JSONArray>() {
            @Override
            public JSONArray convert(String s) {
                return JSON.parseArray(s);
            }
        });

        conversionService.addConverter(new Converter<String, JSONObject>() {
            @Override
            public JSONObject convert(String s) {
                return JSON.parseObject(s);
            }
        });


        applicationRowMapper = BeanPropertyRowMapper.newInstance(ApplicationDO.class, conversionService);
        sourceRowMapper = BeanPropertyRowMapper.newInstance(SourceDO.class, conversionService);
        interfaceRowMapper = BeanPropertyRowMapper.newInstance(InterfaceDO.class, conversionService);
        documentRowMapper = BeanPropertyRowMapper.newInstance(DocumentDO.class, conversionService);
        filterRowMapper = BeanPropertyRowMapper.newInstance(FilterDO.class, conversionService);
        parameterRowMapper = BeanPropertyRowMapper.newInstance(ParameterDO.class, conversionService);

    }

    public ApiJsonCaller getApiJsonCaller() throws SQLException {
     if (this.apiJsonCaller== null) {
         this.apiJsonCaller = new ApiJsonCaller(dataSource);
     }
     return this.apiJsonCaller;
    }


    public ApplicationDO getApplicationDO(String applicationName) throws ExecutionException {
        return applicationCache.get(applicationName, () -> loadApplicationDO(applicationName));
    }

    public ApplicationDO reloadApplicationDO(String applicationName) throws ExecutionException {
        applicationCache.invalidate(applicationName);
        return applicationCache.get(applicationName, () -> loadApplicationDO(applicationName));
    }

    public ApplicationDO loadApplicationDO(String applicationName) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,name,title,note,owner,config,error_codes,version,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_document` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS document_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_filter` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS filter_ids ");
        sqlBuffer.append(" ,(select group_concat(concat(id,'@@',method,'@@',name,'@@',title) ORDER BY `sort` SEPARATOR ',') from `api_interface` WHERE `status` = 0 and `application_id` = api_application.id GROUP BY `application_id` ) AS interface_names ");
        sqlBuffer.append(" ,(select group_concat(name SEPARATOR ',') from `api_source` WHERE type = 'database' and `status` = 0 and `application_id` = api_application.id) AS database_names ");
        sqlBuffer.append(" ,(select group_concat(name SEPARATOR ',') from `api_source` WHERE type = 'redis' and `status` = 0 and `application_id` = api_application.id) AS redis_names ");
        sqlBuffer.append(" from api_application ");
        sqlBuffer.append(" where status = 0 and name = ? ");

        return jdbcTemplate.queryForObject(sqlBuffer.toString(), applicationRowMapper, applicationName);
    }

    public InterfaceDO getInterfaceDO(String applicationName, String interfaceName, String method) throws ExecutionException {
        String key = method + ":" + applicationName + "/" + interfaceName;
        return interfaceCache.get(key, () -> loadInterfaceDO(applicationName, interfaceName, method));
    }

    public InterfaceDO loadInterfaceDO(String applicationName, String interfaceName, String method) throws ExecutionException {

        ApplicationDO applicationDO = getApplicationDO(applicationName);

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,application_id,name,group_name,title,note,sort,method,request_example,response_example,config,implement_type,implement_code,error_codes,version,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select name from `api_application` WHERE `id` = api_interface.application_id) AS application_name ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `title` SEPARATOR ',') from `api_document` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS document_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_filter` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS filter_ids ");
        sqlBuffer.append(" from api_interface ");
        sqlBuffer.append(" where status = 0 and application_id = ?  and name = ? and method = ? ");

        return jdbcTemplate.queryForObject(sqlBuffer.toString(), interfaceRowMapper, applicationDO.getId(), interfaceName, method);

    }

    public DocumentDO getDocumentDO(Long id) throws ExecutionException {
        return documentCache.get(String.valueOf(id), () -> loadDocumentDO(id));
    }

    public DocumentDO loadDocumentDO(Long id) {
        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where status = 0 and id = ? ";
        return jdbcTemplate.queryForObject(documentSql, documentRowMapper, id);
    }


    public SourceDO getRedisSourceDO(String applicationName, String redisName) throws ExecutionException {
        String key = String.format("%s/redis/%s", applicationName, redisName);
        return redisCache.get(String.valueOf(key), () -> loadRedisSourceDO(applicationName, redisName));
    }

    public SourceDO loadRedisSourceDO(String applicationName, String redisName) throws ExecutionException {
        ApplicationDO applicationDO = getApplicationDO(applicationName);
        String sourceSql = " select id,application_id,name,note,type,config,status,gmt_create,gmt_modified from api_source where  status = 0 and type='redis' and application_id = ?  and name = ? ";
        return jdbcTemplate.queryForObject(sourceSql, sourceRowMapper, applicationDO.getId(), redisName);
    }

    public SourceDO getDatabaseSourceDO(String applicationName, String databaseName) throws ExecutionException {
        String key = String.format("%s/database/%s", applicationName, databaseName);
        return databaseCache.get(String.valueOf(key), () -> loadDatabaseSourceDO(applicationName, databaseName));
    }

    public SourceDO loadDatabaseSourceDO(String applicationName, String databaseName) throws ExecutionException {
        ApplicationDO applicationDO = getApplicationDO(applicationName);
        String sourceSql = " select id,application_id,name,note,type,config,status,gmt_create,gmt_modified from api_source where  status = 0 and type='database' and application_id = ?  and name = ? ";
        return jdbcTemplate.queryForObject(sourceSql, sourceRowMapper, applicationDO.getId(), databaseName);
    }

    public FilterDO getFilterDO(Long id) throws ExecutionException {
        return filterCache.get(String.valueOf(id), () -> loadFilterDO(id));
    }

    public FilterDO loadFilterDO(Long id) {
        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where status = 0 and id = ? ";
        return jdbcTemplate.queryForObject(filterSql, filterRowMapper, id);
    }

    public ParameterDO getParameterDO(Long id) throws ExecutionException {
        return parameterCache.get(String.valueOf(id), () -> loadParameterDO(id));
    }

    public ParameterDO loadParameterDO(Long id) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,parent_type,parent_id,name,alias,title,note,sort,position,default_value,data_type,is_array,is_required,is_hidden,is_read_only,examples,validations,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'parameter' and `parent_id` = ap.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" from api_parameter ap ");
        sqlBuffer.append(" where status = 0 and id = ? ");
        return jdbcTemplate.queryForObject(sqlBuffer.toString(), parameterRowMapper, id);
    }

}
