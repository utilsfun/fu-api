package fun.utils.api.core.runtime;


import com.alibaba.fastjson.JSONObject;
import fun.utils.api.apijson.ApiJsonCaller;
import fun.utils.common.DataUtils;
import fun.utils.api.core.common.MyJdbcTemplate;
import fun.utils.api.core.common.MyRedisTemplate;
import fun.utils.api.core.controller.AppBean;
import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.services.DoService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class RunContext {


    public class BeanBridge {

        public Object get(String name){
            return webApplicationContext.getBean(name);
        }

    }

    @Getter
    private final long enterTime = System.currentTimeMillis();

    @Getter
    private final RestTemplate restTemplate;

    @Getter
    private final WebApplicationContext webApplicationContext;

    @Getter
    private final AppBean appBean;

    @Getter
    private final BeanBridge beans = new BeanBridge();

    @Getter
    private final DoService doService;

    @Getter
    private final ApplicationDO applicationDO;

    @Getter
    private final InterfaceDO interfaceDO;

    @Getter
    private final HttpServletResponse response;

    @Getter
    private final HttpServletRequest request;

    @Getter
    private final Logger logger;

    @Getter
    private final List<Long> parameterIds = new ArrayList<>();

    @Getter
    private final JSONObject config;

    @Getter
    private JSONObject additional = new JSONObject();

    @Getter @Setter
    private JSONObject input;

    @Getter @Setter
    private JSONObject parameters;

    @Getter
    private final Map<String,String> responseHeaders = new HashMap<>();

    @Getter @Setter
    private Object result;


    @Getter @Setter
    private boolean isVoid = false;

    protected final Map<String, Object> attributes = new HashMap<>();

    public RunContext(AppBean appBean, String applicationName, String interfaceName, HttpServletResponse response, HttpServletRequest request) throws ExecutionException {


        this.appBean = appBean;
        this.restTemplate = appBean.getRestTemplate();
        this.webApplicationContext = appBean.getWebApplicationContext();
        this.doService = appBean.getDoService();

        this.applicationDO = doService.getApplicationDO(applicationName);
        this.interfaceDO = doService.getInterfaceDO(applicationName, interfaceName,request.getMethod().toLowerCase());

        this.config = interfaceDO.getConfig();
        this.response = response;
        this.request = request;


        for (Long parameterId:DataUtils.getEmptyIfNull(applicationDO.getParameterIds())) {
          if (!parameterIds.contains(parameterId)){
              parameterIds.add(parameterId);
          }
        }

        for (Long parameterId:DataUtils.getEmptyIfNull(interfaceDO.getParameterIds())) {
            if (!parameterIds.contains(parameterId)){
                parameterIds.add(parameterId);
            }
        }

        String myName = interfaceDO.getApplicationName() + "." + interfaceDO.getName();
        this.logger = LoggerFactory.getLogger(myName);

    }


    public HttpSession getSession() {
        return getRequest().getSession();
    }

    public ServletContext getServletContext() {
        return getRequest().getServletContext();
    }


    /* **********  Attribute ********** */

    public void setAttribute(String name, Object value){
        attributes.put(name,value);
    }

    public Object getAttribute(String name){
        return attributes.get(name);
    }

    public void setServletAttribute(String name, Object value){
        getServletContext().setAttribute(name,value);
    }

    public Object getServletAttribute(String name){
        return getServletContext().getAttribute(name);
    }

    public void setSessionAttribute(String name, Object value){
        getSession().setAttribute(name,value);
    }

    public Object getSessionAttribute(String name){
        return getSession().getAttribute(name);
    }


    /* ********** jdbc ,redis Template ********** */

    public DataSource getDataSource(String name) throws ExecutionException, ApiException {
      return appBean.getDataSource(applicationDO.getName(),name);
    }

    public DataSource getDataSource() throws ExecutionException, ApiException {
        return getDataSource("default");
    }


    public JdbcTemplate getJdbcTemplate(String name) throws ExecutionException, ApiException {
        return new MyJdbcTemplate(getDataSource(name));
    }

    public JdbcTemplate getJdbcTemplate() throws ExecutionException, ApiException {
        return new MyJdbcTemplate(getDataSource());
    }

    public JdbcTemplate getJdbc() throws ExecutionException, ApiException {
        return getJdbcTemplate();
    }

    public RedisTemplate getRedisTemplate() throws ExecutionException, ApiException, IOException {
        return new MyRedisTemplate(getRedissonClient());
    }

    public RedisTemplate getRedisTemplate(String name) throws ExecutionException, ApiException, IOException {
        return new MyRedisTemplate(getRedissonClient(name));
    }

    public RedissonClient getRedissonClient(String name) throws ExecutionException, ApiException, IOException {
        return appBean.getRedissonClient(applicationDO.getName(),name);
    }

    public RedissonClient getRedissonClient() throws ExecutionException, ApiException, IOException {
       return getRedissonClient("default");
    }

    public RedisTemplate getRedis() throws ExecutionException, ApiException, IOException {
        return getRedisTemplate();
    }


    /* ********** spring bean ********** */

    public Object getBean(String beanName) {
        return webApplicationContext.getBean(beanName);
    }

    public <T> T getBean(String beanId, Class<T> clazz) {
        return webApplicationContext.getBean(beanId,  clazz);
    }

    public <T> T getBean(Class<T> clazz) {
        return webApplicationContext.getBean(clazz);
    }


    /* ********** logger ********** */


    public void logError(String message, Object... objects){
        logger.error(message,objects);
    }

    public void logWarn(String message, Object... objects){
        logger.warn(message,objects);
    }

    public void logInfo(String message, Object... objects){
        logger.info(message,objects);
    }

    public void logDebug(String message, Object... objects){
        logger.debug(message,objects);
    }

    public void logTrace(String message, Object... objects){
        logger.trace(message,objects);
    }


    /* ********** responseHeaders  ********** */

    public void putHeader(String key,String value){
        responseHeaders.put(key,value);
    }

    /* ********** ApiJson ********** */
    public ApiJsonCaller getApiJson() throws SQLException, ExecutionException, ApiException {
        return getApiJson("default");
    }

    public ApiJsonCaller getApiJson(String databaseName) throws SQLException, ExecutionException, ApiException {
        return new ApiJsonCaller(getDataSource(databaseName));
    }

    //是否接口调动已经失败
    public boolean isFailed() {
        return getResult() instanceof Exception;
    }

}
