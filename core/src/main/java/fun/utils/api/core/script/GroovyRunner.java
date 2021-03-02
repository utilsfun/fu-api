package fun.utils.api.core.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import fun.utils.api.core.common.DataUtils;
import fun.utils.api.core.common.ClassUtils;
import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class GroovyRunner {

    private final GroovyService groovyService;
    private final GroovyScript groovyScript;
    private final Script runner;
    private final Class<?> returnClass;

    @Getter
    private final String version;

    @Getter
    private final String id;

    @Getter
    private final Map<String,Object> properties = new HashMap<>();

    public GroovyRunner(GroovyService groovyService, GroovyScript groovyScript) throws Exception {

        this.groovyService = groovyService;
        this.groovyScript = groovyScript;
        this.id = groovyScript.getId();
        this.version = groovyScript.getVersion();

        Set<String> imports = new HashSet<>();
        Set<String> sourceIds = new HashSet<>();
        Set<GroovySource> sources = new HashSet<>();

        parseSources(sourceIds, sources, groovyScript.getSourceIds());

        for (String src : groovyScript.getImports()) {
            imports.add(formatImport(src));
        }

        for (GroovySource groovySource : sources) {
            for (String src : groovySource.getImports()) {
                imports.add(formatImport(src));
            }
        }


        StringBuffer sb = new StringBuffer();

        sb.append(formatCommentLine("id:" + groovyScript.getId()));
        sb.append(formatCommentLine("title:" + groovyScript.getTitle()));
        sb.append(formatCommentLine("version:" + groovyScript.getVersion()));
        sb.append("\r\n");
        sb.append("/* ******** imports ******** */");
        sb.append("\r\n");

        for (String s : imports) {
            if (StringUtils.isNotBlank(s)) {
                sb.append("import " + s + ";\r\n");
            }
        }

        sb.append("\r\n");
        sb.append("/* ******** Sources ******** */");
        sb.append("\r\n");

        for (GroovySource groovySource : sources) {

            sb.append(formatCommentLine(groovySource.getTitle()));
            sb.append(formatCommentText(groovySource.getNote()));
            sb.append(groovySource.getSource());
            sb.append("\r\n");
        }


        sb.append("\r\n");
        sb.append("/* ******** begin ******** */");
        sb.append("\r\n");

        sb.append(formatCommentText(groovyScript.getNote()));
        sb.append(groovyScript.getSource());

        sb.append("\r\n");
        sb.append("/* ******** end ******** */");
        sb.append("\r\n");


        log.debug(sb.toString());

        runner = groovyService.getShell().parse(sb.toString());

        String returnType = StringUtils.defaultIfBlank(groovyScript.getReturnType(), "Object");
        returnClass = ClassUtils.loadClass(returnType);
        if (returnClass == null) {
            throw new UnknownFormatConversionException("返回类型'" + returnType + "'不可识别");
        }

    }

    private void parseSources(Set<String> allIds, Set<GroovySource> allSources, List<String> sourceIds) throws Exception {
        for (String id : sourceIds) {
            if (!allIds.contains(id)) {
                allIds.add(id);
                GroovySource groovySource = groovyService.getSource(id);
                allSources.add(groovySource);
                parseSources(allIds, allSources, groovySource.getSourceIds());
            }
        }
    }

    private String formatImport(String source) throws Exception {
        return source.replaceFirst("^[\\s]*import[\\s]*", "").replaceAll("[\\s]*;?[\\s]*$", "");
    }

    private String formatCommentText(String source) throws Exception {
        if (StringUtils.isNotBlank(source)) {
            return "/* " + source.replaceAll("\\*/", "") + " */ \r\n";
        } else {
            return "";
        }
    }

    private String formatCommentLine(String source) throws Exception {
        if (StringUtils.isNotBlank(source)) {
            return "//" + source.replaceAll("[\r\n]+", "") + "\r\n";
        } else {
            return "";
        }
    }

    private Map<String,Object> parseVariables(JSONObject variables){

        Map<String,Object> result = new HashMap<>();

        groovyScript.getDeclaredVariables().forEach((name, parameter) -> {

            Object srcObject = null;

            String dataType = StringUtils.defaultIfBlank(parameter.getDataType(), "String");
            Class dataClass = ClassUtils.loadClass(dataType);

            if (dataClass == null) {
                throw new UnknownFormatConversionException("参数类型'" + dataType + "'不可识别");
            }

            if (variables != null && variables.containsKey(name)) {
                srcObject = variables.get(name);
            } else {
                if (StringUtils.isNotBlank(parameter.getDefaultValue())) {
                    srcObject = parameter.getDefaultValue();
                }
            }

            //有值不为空
            List<Object> srcArray = new ArrayList<>();
            List<Object> valueArray = new ArrayList<>();

            if (parameter.isArray()) {
                if (srcObject instanceof List) {
                    srcArray = (List<Object>) srcObject;
                } else {
                    if (DataUtils.isJSONArray(srcObject)) {
                        srcArray = JSON.parseArray(String.valueOf(srcObject));
                    } else {
                        srcArray = Arrays.asList(StringUtils.split(String.valueOf(srcObject)));
                    }
                }
            } else {
                srcArray.add(srcObject);
            }

            for (Object srcObj : srcArray) {

                if (srcObj == null) {
                    continue;
                }

                Object valueObj = ClassUtils.castValue(srcObj, dataType);
                if (valueObj == null) {
                    //类型转换错误
                    throw new UnknownFormatConversionException("参数类型'" + dataType + "'转换错误");
                } else {
                    valueArray.add(valueObj);
                }

            }

            if (srcArray.size() == 0 && parameter.isRequired() ) {
                throw new NullPointerException("参数'" + name + "'必填");
            }

            if (parameter.isArray()) {
                result.put(name, valueArray);
            } else {
                result.put(name, valueArray.size() > 0 ? valueArray.get(0) : null );
            }

        });

        return result;
    }


    private Object run(Map<String,Object> variables) throws Exception {

        Binding binding = new Binding();

        if (properties != null){
            properties.forEach(binding::setProperty);
        }

        if (variables != null){
            variables.forEach(binding::setVariable);
        }

        runner.setBinding(binding);

        return runner.run();

    }

    public GroovyRunner withProperty(String name,Object object){
        this.properties.put(name,object);
        return this;
    }

    public GroovyRunner withProperties(Map<String,Object> properties){
        this.properties.putAll(properties);
        return this;
    }

    public Object execute(JSONObject values) throws Exception {

        JSONObject config = groovyScript.getConfig();
        config = config == null ? new JSONObject() : (JSONObject) config.clone();
        properties.put("$config", config);

        Map<String,Object> variables = parseVariables(values);
        Object result = run(variables);

        return TypeUtils.castToJavaBean(result, returnClass);

    }

}
