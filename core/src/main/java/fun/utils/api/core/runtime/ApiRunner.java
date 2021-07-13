package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.common.ValidConfig;
import fun.utils.api.core.common.ValidUtils;
import fun.utils.common.WebUtils;
import fun.utils.api.core.controller.AppBean;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.script.*;
import fun.utils.api.core.services.DoService;
import fun.utils.common.ClassUtils;
import fun.utils.common.DataUtils;
import fun.utils.jsontemplate.GroovyConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSemaphore;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static fun.utils.api.core.common.ApiConst.PUBLIC_GROOVY_IMPORTS;

@Slf4j
public class ApiRunner {

    private final AppBean appBean;
    private final DoService doService;
    private final GroovyService groovyService;

    private final RunContext runContext;
    private final ApplicationDO applicationDO;
    private final InterfaceDO interfaceDO;

    public ApiRunner(AppBean appBean, RunContext runContext) {

        this.appBean = appBean;

        this.doService = appBean.getDoService();
        this.groovyService = appBean.getGroovyService();

        this.runContext = runContext;
        this.applicationDO = runContext.getApplicationDO();
        this.interfaceDO = runContext.getInterfaceDO();

    }

    public void doInitialize() throws Exception {

        //打包原始数据到 input 对象
        runContext.setInput(WebUtils.getJsonByInput(runContext.getRequest()));
    }

    public void onEnter() throws Exception {

        //运行 application onEnter过滤器 过滤器
        executeFilters("enter",applicationDO.getParameterIds(), applicationDO.getFilterIds());

        //运行 interface onEnter过滤器 过滤器
        executeFilters("enter",runContext.getParameterIds(),interfaceDO.getFilterIds());

    }

    public void doValidate() throws Exception {

        //运行 参数验证
        JSONObject parameters = new JSONObject();
        genParameters(runContext.getInput(), parameters, runContext.getParameterIds(), runContext.getRequest());
        runContext.setParameters(parameters);

    }

    public void onExecute() throws Exception {

        //运行 application onExecute 过滤器
        executeFilters("execute",applicationDO.getParameterIds(), applicationDO.getFilterIds());

        //运行 interface onExecute 过滤器
        executeFilters("execute",runContext.getParameterIds(),interfaceDO.getFilterIds());

    }

    public void execute() throws Exception {

        //运行 interface 方法

        //调用接时任何地方失败都不再继续
        if (runContext.isFailed()){
            return;
        }

        if ("jsontemplate".equalsIgnoreCase(interfaceDO.getImplementType())) {


            // converter
            GroovyConverter converter = runContext.getConverter();

            // data
            JSONObject data = new JSONObject();
            data.put( "parameters", runContext.getParameters());
            data.put( "config", runContext.getConfig());


            // template
            String jtString = interfaceDO.getImplementCode();
            Object jsonTemplate = JSON.parse(jtString);


            //convert
            Object result =  converter.convert(jsonTemplate,data);
            runContext.setResult(result);


        }
        else if ("groovy".equalsIgnoreCase(interfaceDO.getImplementType())) {

            String id = String.valueOf(interfaceDO.getId());
            JSONObject config = interfaceDO.getConfig();
            String version = interfaceDO.getGmtModified().toString();
            String groovy = interfaceDO.getImplementCode();
            String title = applicationDO.getName() + "/" + interfaceDO.getMethod() + ":" + interfaceDO.getName() + "/main";
            executeGroovy("interface:" + id,runContext.getParameterIds(),config,version,title,groovy);

        }
        else {
            throw new Exception(MessageFormat.format("not supply implement type {0}", interfaceDO.getImplementType()));
        }


    }

    public void onReturn() throws Exception {

        //运行 interface onReturn 过滤器
        executeFilters("return",runContext.getParameterIds(), interfaceDO.getFilterIds());

        //运行 application onReturn 过滤器
        executeFilters("return",applicationDO.getParameterIds(), applicationDO.getFilterIds());

   }

    public void run() throws Exception {

        //1.初始化环境数据,参数,资源等
        doInitialize();

        //1.1 应用级限流
        doApplicationQos();

        //1.1 接口级限流
        doInterfaceQos();


        //2.接口进入过滤器
        //2.1 应用过滤器
        //2.2 接口过滤器
        onEnter();

        //3.参数验证
        doValidate();

        //4.接口方法执行过滤器
        //4.1 应用过滤器
        //4.2 接口过滤器
        onExecute();

        //5.接口方法执行
        execute();
       // runContext.setResult(true);

        //6.接口返回过滤器
        //6.1 接口过滤器
        //6.2 应用过滤器
        onReturn();

    }


    private void executeFilters(String point,List<Long> parameterIds,List<Long> filterIds) throws Exception {

        if (filterIds != null){

            for ( Long filterId : filterIds){

                //调用接时任何地方失败都不再继续
                if (runContext.isFailed()){
                    break;
                }

                FilterDO filterDO =  doService.getFilterDO(filterId);

                if (!point.equalsIgnoreCase(filterDO.getPoint())){
                    continue;
                }

                if ("groovy".equalsIgnoreCase(filterDO.getImplementType())) {
                    JSONObject config = filterDO.getConfig();
                    String version = filterDO.getGmtModified().toString();
                    String groovy = filterDO.getImplementCode();
                    String title = applicationDO.getName()  + ("application".equalsIgnoreCase(filterDO.getParentType()) ? "": "/" +  interfaceDO.getMethod() + ":" + interfaceDO.getName()) +  "/filter:" +  filterDO.getTitle();
                    executeGroovy("filter:" + filterId,parameterIds,config,version,title,groovy);
                }
                else {
                    throw new Exception(MessageFormat.format("not supply implement type {0}", filterDO.getImplementType()));
                }

            }
        }
    }


    private void doApplicationQos() throws Exception {

        if (null ==  applicationDO.getConfig()){
            return;
        }

        doQos(applicationDO.getConfig());
    }

    private void doInterfaceQos() throws Exception {

        if (null ==  interfaceDO.getConfig()){
            return;
        }

        doQos(interfaceDO.getConfig());
    }

    private void doQos(JSONObject config) throws Exception {

        JSONArray limitConfigs = config.getJSONArray("qos") ;

        if (null == limitConfigs){
            return;
        }

        for (Object item: limitConfigs) {

            JSONObject limitConfig = (JSONObject)item;
            String keyExpr = limitConfig.getString("key");
            String key = (String) runContext.getConverter().convert(keyExpr,runContext.getInput());
            String mode = limitConfig.getString("mode");
            int maxSpeed = ClassUtils.castValue(limitConfig.getOrDefault("max_speed",0),Integer.class);
            int maxThreads = ClassUtils.castValue(limitConfig.getOrDefault("max_threads",0),Integer.class);
            int timeOut =  ClassUtils.castValue(limitConfig.getOrDefault("time_out",60),Integer.class);

            if ("local".equalsIgnoreCase(mode)){

                if (maxSpeed > 0){

                    boolean isReady = QosUtils.localRateLimit(key,maxSpeed,timeOut);
                    if (!isReady){
                        throw new Exception("QOS local RateLimit " + key + " max:" + maxSpeed );
                    }

                }

                if (maxThreads > 0){

                    Semaphore semaphore = QosUtils.localThreadLimit(key,maxThreads,timeOut);

                    if (semaphore != null){

                        runContext.getCompletedActions().add(new Runnable() {
                            @Override
                            public void run() {
                                semaphore.release();
                            }
                        });

//                        new Thread(() -> {
//                            try {
//                                while (!runContext.getResponse().isCommitted()) {
//                                    Thread.sleep(100);
//                                }
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } finally {
//                                semaphore.release();
//                            }
//                        }).start();


                    }else{
                        throw new Exception("QOS local ThreadsLimit " + key + " max:" + maxThreads );
                    }

                }


            }else if ("global".equalsIgnoreCase(mode)) {

                if (maxSpeed > 0) {

                    String globalKey = applicationDO.getName() + ":rate-limiter:" + key;
                    boolean isReady = QosUtils.globalRateLimit(doService.getRedissonClient(),globalKey, maxSpeed, timeOut);
                    if (!isReady) {
                        throw new Exception("QOS global RateLimit " + key + " max:" + maxSpeed);
                    }

                }

                if (maxThreads > 0){

                    RSemaphore semaphore = QosUtils.globalThreadLimit(doService.getRedissonClient(),key,maxThreads,timeOut);

                    if (semaphore != null){

                        runContext.getCompletedActions().add(new Runnable() {
                            @Override
                            public void run() {
                                semaphore.release();
                            }
                        });

//                        new Thread(() -> {
//                            try {
//                                while (!runContext.getResponse().isCommitted()) {
//                                    Thread.sleep(100);
//                                }
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            } finally {
//                                semaphore.release();
//                            }
//                        }).start();


                    }else{
                        throw new Exception("QOS global ThreadsLimit " + key + " max:" + maxThreads );
                    }

                }
            }

        }

    }

    private void  executeGroovy(Object id, List<Long> parameterIds ,JSONObject config,String version,String title,String groovy) throws Exception {

        GroovyScript method = new GroovyScript();
        method.setId(String.valueOf(id));
        method.setTitle(title);
        method.setConfig(config);
        method.setVersion(version);

        GroovySource groovySource = GroovyUtils.sourceOf(method.getId(), groovy);

        Map<String, GroovyVariable> declaredVariables = method.getDeclaredVariables();

        for (Long parameterId : DataUtils.getEmptyIfNull(parameterIds)) {
            ParameterDO parameterDO = doService.getParameterDO(parameterId);
            GroovyVariable groovyVariable = GroovyUtils.parameterOf(parameterDO.getDataType(), parameterDO.getIsArray() == 1);
            declaredVariables.put(parameterDO.getName(), groovyVariable);
        }

        method.getImports().addAll(groovySource.getImports());
        method.setSource(groovySource.getSource());

        method.getImports().addAll(PUBLIC_GROOVY_IMPORTS);

        GroovyRunner groovyRunner = groovyService.getRunner(method);
        Object result = groovyRunner.withProperty("my", runContext).execute(runContext.getParameters());

        if (result instanceof Exception){
            throw (Exception) result;
        }

        runContext.setResult(result);

    }

    private void genParameters(JSONObject input, JSONObject parameters, List<Long> parameterIds, HttpServletRequest request) throws ExecutionException, ApiException {

        for (Long parameterId : parameterIds) {

            ParameterDO parameterDO = doService.getParameterDO(parameterId);
            if (parameterDO == null) {
                continue;
            }

            JSONObject superJo;
            //为子参数时,从上级参数中取值
            if ("parameter".equalsIgnoreCase(parameterDO.getParentType())) {
                superJo = input;
            }
            //为一级参数时,从指定位置中取值
            else if ("header".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("headers");
            }
            else if ("cookie".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("cookies");
            }
            else if ("query".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("parameters");
            }
            else if ("body".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("body");
            }
            else {
                superJo = input.getJSONObject("body");
            }

            Object srcObject = null;

            if (parameterDO.getIsReadOnly() == 0 && superJo != null) {
                //为不只读,并且有数据

                //按名称从输入中取值
                srcObject = superJo.get(parameterDO.getName());
                if (srcObject instanceof String){
                    srcObject = StringUtils.defaultIfBlank((String)srcObject,null);
                }

                //如果没有值从别名中取值,直到第一个有值的别称
                if (srcObject == null && parameterDO.getAlias() != null) {
                    for (String key : parameterDO.getAlias()) {
                        srcObject = superJo.get(key);
                        if (srcObject instanceof String){
                            srcObject = StringUtils.defaultIfBlank((String)srcObject,null);
                        }
                        if (srcObject != null) {
                            break;
                        }
                    }
                }

            }

            if (srcObject == null) {
                //取默认值
                srcObject = ClassUtils.castValue(parameterDO.getDefaultValue(), parameterDO.getDataType());
            }

            if (srcObject == null) {
                //也无默认值
                if (parameterDO.getIsRequired() == 1) {
                    //如果参数定义为必填,则报错.
                    throw ApiException.parameterRequiredException(parameterDO.getName());
                }

            }
            else {
                //有值不为空
                List<Object> srcArray = new ArrayList<>();
                List<Object> valueArray = new ArrayList<>();

                //无论是单值还是多值(数组)都入加到列表中处理
                if (parameterDO.getIsArray() == 1) {
                    if (srcObject instanceof List) {
                        srcArray = (List<Object>) srcObject;
                    }
                    else {
                        if ( DataUtils.isJSONArray(srcObject)) {
                            srcArray = JSON.parseArray(String.valueOf(srcObject));
                        }
                        else {
                            srcArray = Arrays.asList(StringUtils.split(String.valueOf(srcObject)));
                        }
                    }
                }
                else {
                    srcArray.add(srcObject);
                }

                for (Object elementObj : srcArray) {

                    if ("object".equalsIgnoreCase(parameterDO.getDataType())) {
                        //是对象

                        JSONObject srcJo;
                        if (elementObj instanceof JSONObject) {
                            srcJo = (JSONObject) elementObj;
                        }
                        else {
                            srcJo = ClassUtils.castValue(elementObj,JSONObject.class);
                        }

                        JSONObject valueJo = new JSONObject();
                        genParameters(srcJo, valueJo, parameterDO.getParameterIds(), request);
                        valueArray.add(valueJo);

                    }
                    else {
                        //基础类型

                        Object value = ClassUtils.castValue(elementObj, parameterDO.getDataType());
                        if (value == null) {
                            //类型转换错误
                            throw ApiException.parameterTypeException(parameterDO.getName(), parameterDO.getDataType());
                        }
                        else {
                            //加入到值列表前进行参数验证 *******************
                            for (ValidConfig validConfig : DataUtils.getEmptyIfNull(parameterDO.getValidations())) {
                                ValidUtils.validateValue(parameterDO.getName(), value, validConfig.getType(), validConfig.getData(), validConfig.getMessage());
                            }
                            //加入到值列表
                            valueArray.add(value);
                        }
                    }
                }

                //加入到json参数值包对象中
                if (parameterDO.getIsArray() == 1) {
                    parameters.put(parameterDO.getName(), JSONArray.toJSON(valueArray));
                }
                else {
                    parameters.put(parameterDO.getName(), JSONArray.toJSON(valueArray.get(0)));
                }
            }

        }
    }
}





