package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.exception.ApiException;
import fun.utils.api.core.persistence.ApplicationDO;
import fun.utils.api.core.persistence.InterfaceDO;
import fun.utils.api.core.persistence.ParameterDO;
import fun.utils.api.core.script.*;
import fun.utils.api.core.services.DoService;
import fun.utils.api.core.util.ClassUtils;
import fun.utils.api.core.util.RequestTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ApiRunner {

    private final DoService doService;
    private final GroovyService groovyService;

    private final RunContext runContext;
    private final ApplicationDO applicationDO;
    private final InterfaceDO interfaceDO;

    public ApiRunner(DoService doService, GroovyService groovyService, RunContext runContext) {
        this.doService = doService;
        this.groovyService = groovyService;
        this.runContext = runContext;
        this.applicationDO = runContext.getApplicationDO();
        this.interfaceDO = runContext.getInterfaceDO();
    }

    public void doInitialize() throws Exception {
        //打包原始数据到 input 对象
        runContext.setInput(RequestTools.getJsonByInput(runContext.getRequest()));
    }

    public void onEnter() throws Exception {

        //运行 application onEnter过滤器
        //运行 interface onEnter过滤器

    }

    public void doValidate() throws Exception {

        //运行 参数验证
        List<Long> parameterIds = new ArrayList<>();
        parameterIds.addAll(applicationDO.getParameterIds());
        parameterIds.addAll(interfaceDO.getParameterIds());
        JSONObject parameters = new JSONObject();
        genParameters(runContext.getInput(),parameters,parameterIds,runContext.getRequest());
        runContext.setParameters(parameters);

    }

    public void onExecute() throws Exception {

        //运行 application onExecute
        //运行 interface onExecute
    }

    public void execute() throws Exception {

        //运行 interface 方法

        if ("groovy".equalsIgnoreCase(interfaceDO.getImplementType())){

            GroovyScript  method  = new GroovyScript();
            method.setId(String.valueOf(interfaceDO.getId()));
            method.setConfig(interfaceDO.getConfig());
            method.setVersion(interfaceDO.getGmtModified().toString());
            method.setTitle(interfaceDO.getTitle());

            String groovy = interfaceDO.getImplementCode();
            GroovySource groovySource = GroovyUtils.sourceOf(method.getId(),groovy);
            method.getImports().addAll(groovySource.getImports());
            method.setSource(groovySource.getSource());

            GroovyRunner groovyRunner = groovyService.getRunner(method);
            Object result = groovyRunner.execute(runContext,runContext.getParameters());
            runContext.setResult(result);

        }

    }

    public void onReturn() throws Exception {

        //运行 interface onReturn
        //运行 application onReturn

    }

    public void run() throws Exception {

        doInitialize();

        onEnter();

        doValidate();

        onExecute();

        execute();

        onReturn();

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
            } else if ("cookie".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("cookies");
            } else if ("query".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("parameters");
            } else if ("body".equalsIgnoreCase(parameterDO.getPosition())) {
                superJo = input.getJSONObject("body");
            } else {
                superJo = input.getJSONObject("body");
            }

            Object srcObject = null;

            if (parameterDO.getIsReadOnly() == 0 && superJo != null) {
                //为不只读,并且有数据

                //按名称从输入中取值
                srcObject = superJo.get(parameterDO.getName());
                //如果没有值从别名中取值,直到第一个有值的别称
                if (srcObject == null) {
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
                    throw ApiException.parameterRequiredException(parameterDO.getName());
                }

            } else {
                //有值不为空
                List<Object> srcArray = new ArrayList<>();
                List<Object> valueArray = new ArrayList<>();

                if (parameterDO.getIsArray() == 1) {
                    if (srcObject instanceof List) {
                        srcArray = (List<Object>) srcObject;
                    } else {
                        if (String.valueOf(srcObject).trim().matches("\\[.+\\]")) {
                            srcArray = JSON.parseArray(JSON.toJSONString(srcObject));
                        } else {
                            srcArray = Arrays.asList(StringUtils.split(String.valueOf(srcObject)));
                        }
                    }
                } else {
                    srcArray.add(srcObject);
                }

                for (Object elementObj : srcArray) {

                    if ("object".equalsIgnoreCase(parameterDO.getDataType())) {
                        //是对象

                        JSONObject srcJo;
                        if (elementObj instanceof JSONObject) {
                            srcJo = (JSONObject) elementObj;
                        } else {
                            srcJo = JSON.parseObject(JSON.toJSONString(elementObj));
                        }

                        JSONObject valueJo = new JSONObject();
                        genParameters(srcJo, valueJo, parameterDO.getParameterIds(), request);
                        valueArray.add(valueJo);

                    } else {
                        //基础类型

                        Object value = ClassUtils.castValue(elementObj, parameterDO.getDataType());
                        if (value == null) {
                            //类型转换错误
                            throw ApiException.parameterTypeException(parameterDO.getName(), parameterDO.getDataType());
                        } else {
                            valueArray.add(value);
                        }
                    }

                }

                if (parameterDO.getIsArray() == 1) {
                    parameters.put(parameterDO.getName(), JSONArray.toJSON(valueArray));
                } else {
                    parameters.put(parameterDO.getName(), JSONArray.toJSON(valueArray.get(0)));
                }
            }
        }
    }
}





