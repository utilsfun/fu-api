package fun.utils.api.core.script;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UnknownFormatConversionException;

public class GroovyRunner {

    private GroovyService groovyService;
    private GroovyScript groovyScript;
    private Script runner;
    private Class<?> returnClass;

    @Getter
    private String version;

    @Getter
    private String id;


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

        System.out.println(sb.toString());
        runner = groovyService.getShell().parse(sb.toString());

        String returnType = StringUtils.defaultIfBlank(groovyScript.getReturnType(), "String");
        returnClass = GroovyUtils.loadClass(returnType);
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

    public Object execute(Object context, JSONObject variables) throws Exception {

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
