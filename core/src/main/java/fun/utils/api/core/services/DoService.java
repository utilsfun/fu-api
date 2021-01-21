package fun.utils.api.core.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.runtime.RunApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;


import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DoService {


    private final Cache<String, RunApplication> runApplicationCache;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private GenericConversionService conversionService;

    private BeanPropertyRowMapper<ApplicationDO> applicationRowMapper;
    private BeanPropertyRowMapper<DocumentDO> documentRowMapper;
    private BeanPropertyRowMapper<FilterDO> filterRowMapper;
    private BeanPropertyRowMapper<ParameterDO> parameterRowMapper;

    public DoService() {

        runApplicationCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(10, TimeUnit.SECONDS).build();


        conversionService = new DefaultConversionService();
        conversionService.addConverter(new FastJsonConverter());


//        conversionService.addConverter(new JsonObjectConverter(conversionService));
//        conversionService.addConverter(new JsonArrayConverter(conversionService));



//        conversionService.addConverter(new GenericConverter() {
//
//           @Override
//           public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
//               return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, Object.class));
//           }
//
//           @Override
//           @Nullable
//           public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
//               if (source == null) {
//                   return null;
//               }
//
//               String string = (String) source;
//               return TypeUtils.castToJavaBean(string, targetType.getType());
//
//           }
//       });

//        conversionService.addConverter(new GenericConverter() {
//
//            @Override
//            public Set<GenericConverter.ConvertiblePair> getConvertibleTypes() {
//                return Collections.singleton(new GenericConverter.ConvertiblePair(String.class, List.class));
//            }
//
//            @Override
//            @Nullable
//            public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
//                if (source == null) {
//                    return null;
//                }
//
//                String string = (String) source;
//                TypeDescriptor targetElementType = targetType.getElementTypeDescriptor();
//                return JSON.parseArray(string, targetElementType.getType());
//
//            }
//        });

//
//        conversionService.addConverter(new Converter<String, List<String>>() {
//            @Override
//            public List<String> convert(String s) {
//                if (JSONValidator.from(s).validate()){
//                    return JSON.parseArray(s,String.class);
//                }else{
//                    return Arrays.asList(StringUtils.split(s,","));
//                }
//            }
//        });

//        conversionService.addConverter(new Converter<String, List<Validation>>() {
//            @Override
//            public List<Validation> convert(String s) {
//                return JSON.parseArray(s, Validation.class);
//            }
//        });

//        conversionService.addConverter(new Converter<String, JSON>() {
//            @Override
//            public JSON convert(String s) {
//                return (JSON)JSON.parse(s);
//            }
//        });
//
//        conversionService.addConverter(new Converter<String, JSONArray>() {
//            @Override
//            public JSONArray convert(String s) {
//                return JSON.parseArray(s);
//            }
//        });
//
//        conversionService.addConverter(new Converter<String, JSONObject>() {
//            @Override
//            public JSONObject convert(String s) {
//                return JSON.parseObject(s);
//            }
//        });


        applicationRowMapper = BeanPropertyRowMapper.newInstance(ApplicationDO.class,conversionService);

        documentRowMapper = BeanPropertyRowMapper.newInstance(DocumentDO.class,conversionService);

        filterRowMapper = BeanPropertyRowMapper.newInstance(FilterDO.class,conversionService);

        parameterRowMapper = BeanPropertyRowMapper.newInstance(ParameterDO.class,conversionService);


    }

    public RunApplication getRunApplication(String name) throws ExecutionException {
        return  runApplicationCache.get(name,()-> loadApplicationByName(name));
    }


    public RunApplication loadApplicationByName(String name) throws ExecutionException {

        RunApplication runApplication = new RunApplication();
        runApplication.setName(name);

        String appSql = " select id,name,title,note,owner,version,status,gmt_create,gmt_modified from api_application where name = ? " ;
        ApplicationDO applicationDO = jdbcTemplate.queryForObject(appSql,applicationRowMapper, name);
        runApplication.setApplication(applicationDO);

        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where parent_type = 'application' and parent_id = ? " ;
        List<DocumentDO> documentDOList = jdbcTemplate.query(documentSql,documentRowMapper, applicationDO.getId());
        runApplication.setDocuments(documentDOList);

        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where parent_type = 'application' and parent_id = ? order by sort " ;
        List<FilterDO> filterDOList = jdbcTemplate.query(filterSql,filterRowMapper, applicationDO.getId());
        runApplication.setFilters(filterDOList);

        String parameterSql = " select id,parent_type,parent_id,name,alias,title,sort,position,default_value,data_type,is_array,is_required,is_hidden,is_read_only,examples,validations,status,gmt_create,gmt_modified from api_parameter where parent_type = 'application' and parent_id = ? order by sort " ;
        List<ParameterDO> parameterDOList = jdbcTemplate.query(parameterSql,parameterRowMapper, applicationDO.getId());

        runApplication.setParameters(parameterDOList);


        return runApplication;

    }

}
