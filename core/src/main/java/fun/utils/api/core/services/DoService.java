package fun.utils.api.core.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.DocumentDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.runtime.RunApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DoService {


    private final Cache<String, RunApplication> runApplicationCache;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public DoService() {
        runApplicationCache = CacheBuilder.newBuilder().maximumSize(10).expireAfterAccess(2, TimeUnit.MINUTES).build();
    }

    public RunApplication getRunApplication(String name) throws ExecutionException {
        return  runApplicationCache.get(name,()-> loadApplicationByName(name));
    }


    public RunApplication loadApplicationByName(String name) throws ExecutionException {

        RunApplication runApplication = new RunApplication();
        runApplication.setName(name);

        String appSql = " select id,name,title,note,owner,version,status,gmt_create,gmt_modified from api_application where name = ? " ;
        ApplicationDO applicationDO = jdbcTemplate.queryForObject(appSql,BeanPropertyRowMapper.newInstance(ApplicationDO.class), name);
        runApplication.setApplication(applicationDO);

        String documentSql = " select id,parent_type,parent_id,title,note,format,content,permission,status,gmt_create,gmt_modified from api_document where parent_type = 'application' and parent_id = ? " ;
        List<DocumentDO> documentDOList = jdbcTemplate.queryForList(documentSql,DocumentDO.class, applicationDO.getId());
        runApplication.setDocuments(documentDOList);

        String filterSql = " select id,parent_type,parent_id,title,config,sort,implement_type,implement_code,point,status,gmt_create,gmt_modified from api_filter where parent_type = 'application' and parent_id = ? order by sort " ;
        List<FilterDO> filterDOList = jdbcTemplate.queryForList(filterSql,FilterDO.class, applicationDO.getId());
        runApplication.setFilters(filterDOList);

        return runApplication;

    }

}
