package fun.utils.api.core.common.groovy;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class GroovyRunner {

    private GroovyScript groovyScript;
    private Script runner;
    private Class<?> returnClass;

    @Getter
    private String version;

    @Getter
    private String id;

    public GroovyRunner(GroovyScript groovyScript) {

        this.groovyScript = groovyScript;
        this.id = groovyScript.getId();
        this.version = groovyScript.getVersion();

        GroovyShell shell = new GroovyShell();

        Set<String> imports = new HashSet<>();
        for (String s: groovyScript.getImports()){
            imports.add(s.replaceFirst("^[\\s]*import[\\s]*","").replaceAll("[\\s]*;?[\\s]*$",""));
        }

        StringBuffer sb = new StringBuffer();

        for (String s:imports){
            sb.append("import " + s + ";\r\n");
        }

        sb.append("\r\n");
        sb.append(groovyScript.getScript());

        System.out.println(sb.toString());
        runner = shell.parse(sb.toString());

        String returnType = StringUtils.defaultIfBlank(groovyScript.getReturnType(), "String");
        returnClass = GroovyUtils.loadClass(returnType);
        if (returnClass == null) {
            throw new UnknownFormatConversionException("返回类型'" + returnType + "'不可识别");
        }

    }

    public Object execute(Object context,JSONObject variables) throws Exception {

        Binding binding = new Binding();
        binding.setVariable("context", context);
        binding.setVariable("config", groovyScript.getConfig());

        groovyScript.getDeclaredVariables().forEach((name, parameter) -> {

            Object parameterValue;
            Object sourceValue = null;

            String dataType = StringUtils.defaultIfBlank(parameter.getDataType(), "String");
            Class dataClass = GroovyUtils.loadClass(dataType);

            if (dataClass == null) {
                throw new UnknownFormatConversionException("参数类型'" + dataType + "'不可识别");
            }

            if (variables != null && variables.containsKey(name)) {
                sourceValue = variables.get(name);
            } else {
                if (StringUtils.isNotBlank(parameter.getDefaultValue())) {
                    sourceValue = parameter.getDefaultValue();
                }
            }

            parameterValue = TypeUtils.castToJavaBean(sourceValue, dataClass);
            if (sourceValue == null && parameter.isRequired()) {
                throw new NullPointerException("参数'" + name + "'必填");
            } else {
                binding.setVariable(name, parameterValue);
            }

        });


        runner.setBinding(binding);
        Object result = runner.run();

        return TypeUtils.castToJavaBean(result, returnClass);

    }
}
