package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.DataUtils;
import fun.utils.api.core.common.ValidConfig;
import fun.utils.api.core.common.ValidUtils;
import fun.utils.api.core.controller.AppBean;
import fun.utils.api.core.common.ApiException;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.FilterDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.script.*;
import fun.utils.api.core.services.DoService;
import fun.utils.common.ClassUtils;
import fun.utils.api.core.common.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

        if ("groovy".equalsIgnoreCase(interfaceDO.getImplementType())) {

            String id = String.valueOf(interfaceDO.getId());
            JSONObject config = interfaceDO.getConfig();
            String version = interfaceDO.getGmtModified().toString();
            String groovy = interfaceDO.getImplementCode();
            executeGroovy(id,runContext.getParameterIds(),config,version,groovy);

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
                    executeGroovy(filterId,parameterIds,config,version,groovy);
                }
                else {
                    throw new Exception(MessageFormat.format("not supply implement type {0}", filterDO.getImplementType()));
                }

            }
        }
    }


    private void  executeGroovy(Object id, List<Long> parameterIds ,JSONObject config,String version,String groovy) throws Exception {

        GroovyScript method = new GroovyScript();
        method.setId(String.valueOf(id));
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
        Object result = groovyRunner.withProperty("$context", runContext).execute(runContext.getParameters());

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
                //如果没有值从别名中取值,直到第一个有值的别称
                if (srcObject == null && parameterDO.getAlias() != null) {
                    for (String key : parameterDO.getAlias()) {
                        srcObject = superJo.get(key);
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
                            srcJo = JSON.parseObject(JSON.toJSONString(elementObj));
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





