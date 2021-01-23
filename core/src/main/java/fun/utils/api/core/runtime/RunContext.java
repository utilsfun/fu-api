package fun.utils.api.core.runtime;


import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.InterfaceDO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RunContext {

    @Getter
    private final long enterTime = System.currentTimeMillis();

    @Getter @Setter
    private RestTemplate restTemplate = new RestTemplate();

    @Getter
    private WebApplicationContext webApplicationContext;

    @Getter
    private ApplicationDO applicationDO;

    @Getter
    private InterfaceDO interfaceDO;

    @Getter
    private JSONObject config;

    @Getter @Setter
    private JSONObject input;

    @Getter @Setter
    private JSONObject parameters;

    @Getter
    private HttpServletResponse response;

    @Getter
    private HttpServletRequest request;

    @Getter @Setter
    private Object result;

    @Getter
    private Logger logger;

    protected final Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();
    protected final Map<String, RedisTemplate> redisTemplates = new HashMap<>();
    protected final Map<String, Object> attributes = new HashMap<>();

    private RunContext(){

    }

    public RunContext(RestTemplate restTemplate, WebApplicationContext webApplicationContext,ApplicationDO applicationDO, InterfaceDO interfaceDO, HttpServletResponse response, HttpServletRequest request) {

        this.restTemplate = restTemplate;
        this.webApplicationContext = webApplicationContext;
        this.interfaceDO = interfaceDO;
        this.applicationDO = applicationDO;
        this.config = interfaceDO.getConfig();
        this.response = response;
        this.request = request;

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

    public void putJdbcTemplate(String name, JdbcTemplate obj) {
        jdbcTemplates.put(name, obj);
    }

    public void putJdbcTemplate(JdbcTemplate obj) {
        jdbcTemplates.put("default", obj);
    }

    public JdbcTemplate getJdbcTemplate(String name) {
        return jdbcTemplates.get(name);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplates.get("default");
    }

    public JdbcTemplate getJdbc() {
        return getJdbcTemplate();
    }

    public void putRedisTemplate(String name, RedisTemplate obj) {
        redisTemplates.put(name, obj);
    }

    public void putRedisTemplate(RedisTemplate obj) {
        redisTemplates.put("default", obj);
    }

    public RedisTemplate getRedisTemplate(String name) {
        return redisTemplates.get(name);
    }

    public RedisTemplate getRedisTemplate() {
        return redisTemplates.get("default");
    }

    public RedisTemplate getRedis() {
        return redisTemplates.get("default");
    }


    /* ********** spring bean ********** */

    public <T> T getBean(String beanId, Class<T> clazz) {
        return getWebApplicationContext().getBean(beanId,  clazz);
    }

    public <T> T getBean(Class<T> clazz) {
        return getWebApplicationContext().getBean(clazz);
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

}
