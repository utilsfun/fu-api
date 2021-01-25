package fun.utils.api.core.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.persistence.*;
import fun.utils.api.core.runtime.RunApplication;
import fun.utils.api.core.runtime.RunInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DoService {


    private final Cache<String, ApplicationDO> applicationCache;
    private final Cache<String, InterfaceDO> interfaceCache;
    private final Cache<String, DocumentDO> documentCache;
    private final Cache<String, FilterDO> filterCache;
    private final Cache<String, ParameterDO> parameterCache;

    private final BeanPropertyRowMapper<ApplicationDO> applicationRowMapper;
    private final BeanPropertyRowMapper<InterfaceDO> interfaceRowMapper;
    private final BeanPropertyRowMapper<DocumentDO> documentRowMapper;
    private final BeanPropertyRowMapper<FilterDO> filterRowMapper;
    private final BeanPropertyRowMapper<ParameterDO> parameterRowMapper;


    private final DefaultConversionService defaultConversionService  = new DefaultConversionService();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DoService() {

        applicationCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(10, TimeUnit.SECONDS).build();
        interfaceCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(10, TimeUnit.SECONDS).build();
        documentCache = CacheBuilder.newBuilder().maximumSize(500).expireAfterAccess(10, TimeUnit.SECONDS).build();
        filterCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(10, TimeUnit.SECONDS).build();
        parameterCache = CacheBuilder.newBuilder().maximumSize(5000).expireAfterAccess(10, TimeUnit.SECONDS).build();

        GenericConversionService conversionService = new DefaultConversionService();

        conversionService.addConverter(new JsonObjectConverter(defaultConversionService));
        conversionService.addConverter(new JsonArrayConverter(defaultConversionService));

        conversionService.addConverter(new Converter<String, JSON>() {
            @Override
            public JSON convert(String s) {
                return (JSON)JSON.parse(s);
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
        interfaceRowMapper = BeanPropertyRowMapper.newInstance(InterfaceDO.class, conversionService);
        documentRowMapper = BeanPropertyRowMapper.newInstance(DocumentDO.class, conversionService);
        filterRowMapper = BeanPropertyRowMapper.newInstance(FilterDO.class, conversionService);
        parameterRowMapper = BeanPropertyRowMapper.newInstance(ParameterDO.class, conversionService);

    }


    public ApplicationDO getApplicationDO(String applicationName) throws ExecutionException {
        return  applicationCache.get(applicationName,()-> loadApplicationDO(applicationName));
    }

    public ApplicationDO loadApplicationDO(String applicationName) {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,name,title,note,owner,config,error_codes,version,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `title` SEPARATOR ',') from `api_document` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS document_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_filter` WHERE `status` = 0 and `parent_type` = 'application' and `parent_id` = api_application.id GROUP BY `parent_id` ) AS filter_ids ");
        sqlBuffer.append(" ,(select group_concat(name ORDER BY `sort` SEPARATOR ',') from `api_interface` WHERE `status` = 0 and `application_id` = api_application.id GROUP BY `application_id` ) AS interface_names ");
        sqlBuffer.append(" from api_application ");
        sqlBuffer.append(" where status = 0 and name = ? ");

        return jdbcTemplate.queryForObject(sqlBuffer.toString(),applicationRowMapper, applicationName);
    }

    public InterfaceDO getInterfaceDO(String applicationName,String interfaceName) throws ExecutionException {
        return interfaceCache.get(applicationName + "/" + interfaceName ,()-> loadInterfaceDO(applicationName,interfaceName));
    }

    public InterfaceDO loadInterfaceDO(String applicationName,String interfaceName) throws ExecutionException {

        ApplicationDO applicationDO = getApplicationDO(applicationName);

        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,application_id,name,group_name,title,note,sort,method,request_example,response_example,config,implement_type,implement_code,error_codes,version,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select name from `api_application` WHERE `id` = api_interface.application_id) AS application_name ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `title` SEPARATOR ',') from `api_document` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS document_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_filter` WHERE `status` = 0 and `parent_type` = 'interface' and `parent_id` = api_interface.id GROUP BY `parent_id` ) AS filter_ids ");
        sqlBuffer.append(" from api_interface ");
        sqlBuffer.append(" where status = 0 and application_id = ?  and name = ? ");

        return jdbcTemplate.queryForObject(sqlBuffer.toString(),interfaceRowMapper,applicationDO.getId(), interfaceName);
    }

    public DocumentDO getDocumentDO(Long id) throws ExecutionException {
        return  documentCache.get(String.valueOf(id),()-> loadDocumentDO(id));
    }

    public DocumentDO loadDocumentDO(Long id)  {
        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where status = 0 and id = ? " ;
        return jdbcTemplate.queryForObject(documentSql,documentRowMapper,id);
    }

    public FilterDO getFilterDO(Long id) throws ExecutionException {
        return  filterCache.get(String.valueOf(id),()-> loadFilterDO(id));
    }

    public FilterDO loadFilterDO(Long id)  {
        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where status = 0 and id = ? " ;
        return jdbcTemplate.queryForObject(filterSql,filterRowMapper,id);
    }

    public ParameterDO getParameterDO(Long id) throws ExecutionException {
        return  parameterCache.get(String.valueOf(id),()-> loadParameterDO(id));
    }

    public ParameterDO loadParameterDO(Long id)  {
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append(" select id,parent_type,parent_id,name,alias,title,sort,position,default_value,data_type,is_array,is_required,is_hidden,is_read_only,examples,validations,status,gmt_create,gmt_modified ");
        sqlBuffer.append(" ,(select group_concat(id ORDER BY `sort` SEPARATOR ',') from `api_parameter` WHERE `status` = 0 and `parent_type` = 'parameter' and `parent_id` = api_parameter.id GROUP BY `parent_id` ) AS parameter_ids ");
        sqlBuffer.append(" from api_parameter ");
        sqlBuffer.append(" where status = 0 and id = ? ");
        return jdbcTemplate.queryForObject(sqlBuffer.toString(),parameterRowMapper,id);
    }

}
