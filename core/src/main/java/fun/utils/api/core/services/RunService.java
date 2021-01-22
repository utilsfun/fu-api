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


import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RunService {

    private final Cache<String, RunApplication> runApplicationCache;
    private final Cache<String, RunInterface> runInterfaceCache;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private DefaultConversionService defaultConversionService  = new DefaultConversionService();

    private BeanPropertyRowMapper<ApplicationDO> applicationRowMapper;
    private BeanPropertyRowMapper<InterfaceDO> interfaceRowMapper;
    private BeanPropertyRowMapper<DocumentDO> documentRowMapper;
    private BeanPropertyRowMapper<FilterDO> filterRowMapper;
    private BeanPropertyRowMapper<ParameterDO> parameterRowMapper;



    public RunService() {

        runApplicationCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(10, TimeUnit.SECONDS).build();
        runInterfaceCache = CacheBuilder.newBuilder().maximumSize(200).expireAfterAccess(10, TimeUnit.SECONDS).build();

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

    public RunApplication getRunApplication(String applicationName) throws ExecutionException {
        return  runApplicationCache.get(applicationName,()-> loadApplicationByName(applicationName));
    }

    public RunInterface getRunInterface(String applicationName, String interfaceName) throws ExecutionException {
        return  runInterfaceCache.get(interfaceName,()-> loadRunInterfaceByName(applicationName, interfaceName));
    }

    public RunInterface loadRunInterfaceByName(String applicationName, String interfaceName) throws ExecutionException {
        RunInterface runInterface = new RunInterface();
        runInterface.setName(interfaceName);

        RunApplication runApplication = getRunApplication(applicationName);
        long applicationId = runApplication.getApplicationDO().getId();

        runInterface.setRunApplication(runApplication);

        String interfaceSql = "  select id,application_id,name,group,title,note,sort,method,request_example,response_example,config,implement_type,implement_code,error_codes,version,status,gmt_create,gmt_modified from api_interface where status = 0 and application_id = ? and name = ? " ;
        InterfaceDO interfaceDO = jdbcTemplate.queryForObject(interfaceSql,interfaceRowMapper,applicationId , applicationName);
        runInterface.setInterfaceDO(interfaceDO);

        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where status = 0 and parent_type = 'interface' and parent_id = ? " ;
        List<DocumentDO> documentDOList = jdbcTemplate.query(documentSql,documentRowMapper, interfaceDO.getId());
        runInterface.setDocuments(documentDOList);

        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where status = 0 and parent_type = 'interface' and parent_id = ? order by sort " ;
        List<FilterDO> filterDOList = jdbcTemplate.query(filterSql,filterRowMapper, interfaceDO.getId());
        runInterface.setFilters(filterDOList);

        String parameterSql = " select id,parent_type,parent_id,name,alias,title,sort,position,default_value,data_type,is_array,is_required,is_hidden,is_read_only,examples,validations,status,gmt_create,gmt_modified from api_parameter where status = 0 and parent_type = 'interface' and parent_id = ? order by sort " ;
        List<ParameterDO> parameterDOList = jdbcTemplate.query(parameterSql,parameterRowMapper, interfaceDO.getId());
        runInterface.setParameters(parameterDOList);

        return  runInterface;

    }

    public ApplicationDO loadApplicationDO(String applicationName) throws ExecutionException {
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

    public RunApplication loadApplicationByName(String applicationName) throws ExecutionException {

        RunApplication runApplication = new RunApplication();
        runApplication.setName(applicationName);

        String appSql = " select id,name,title,note,owner,config,error_codes,version,status,gmt_create,gmt_modified from api_application where status = 0 and name = ? " ;


        ApplicationDO applicationDO = loadApplicationDO(applicationName);
        runApplication.setApplicationDO(applicationDO);

        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where status = 0 and parent_type = 'application' and parent_id = ? " ;
        List<DocumentDO> documentDOList = jdbcTemplate.query(documentSql,documentRowMapper, applicationDO.getId());
        runApplication.setDocuments(documentDOList);

        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where status = 0 and parent_type = 'application' and parent_id = ? order by sort " ;
        List<FilterDO> filterDOList = jdbcTemplate.query(filterSql,filterRowMapper, applicationDO.getId());
        runApplication.setFilters(filterDOList);

        String parameterSql = " select id,parent_type,parent_id,name,alias,title,sort,position,default_value,data_type,is_array,is_required,is_hidden,is_read_only,examples,validations,status,gmt_create,gmt_modified from api_parameter where status = 0 and parent_type = 'application' and parent_id = ? order by sort " ;
        List<ParameterDO> parameterDOList = jdbcTemplate.query(parameterSql,parameterRowMapper, applicationDO.getId());
        runApplication.setParameters(parameterDOList);

        String interfaceSql = " select name from api_interface where status = 0 and application_id = ? order by sort " ;
        List<String> interfaceNames = jdbcTemplate.queryForList(interfaceSql,String.class, applicationDO.getId());
        runApplication.setInterfaceNames(interfaceNames);


        return runApplication;

    }

}
