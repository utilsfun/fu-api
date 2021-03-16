package fun.utils.jtempate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.ClassUtils;
import fun.utils.common.DataUtils;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;


public class GroovyConverter {

    private static String GROOVY_PUB_EXPR = "";

    protected static final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

    static {
        GROOVY_PUB_EXPR += "import com.alibaba.fastjson.*; \r\n";
        GROOVY_PUB_EXPR += "import org.apache.commons.lang3.*; \r\n";
    }


    private final RestTemplate restTemplate;

    private final WebApplicationContext webApplicationContext;

    private final HttpServletRequest request;


    public GroovyConverter(RestTemplate restTemplate, WebApplicationContext webApplicationContext, HttpServletRequest request) {
        this.restTemplate = restTemplate;
        this.webApplicationContext = webApplicationContext;
        this.request = request;
    }


    static class IfBlank {
        public String call(String... str) {
            return org.apache.commons.lang3.StringUtils.firstNonBlank(str);
        }
    }

    static class Cast {

        public Object call(Object value, String type) {
            return ClassUtils.castValue(value, type);
        }

        public Object call(Object value, String type, String format) {
            return MessageFormat.format(format, call(value, type));
        }
    }

    static class Data {

        final JSON rootObject;

        public Data(JSON rootObject) {
            this.rootObject = rootObject;
        }

        public boolean contains(String path) {
            return JSONPath.contains(rootObject, path);
        }

        private Object getOrDefault(String path) {
            return getOrDefault(path, null);
        }

        private Object getOrDefault(String path, Object defaultValue) {
            return JSONPath.contains(rootObject, path) ? JSONPath.eval(rootObject, path) : defaultValue;
        }

        public Object call(String path) {
            return getOrDefault(path);
        }

        public Object call(String path, Object defaultValue) {
            return getOrDefault(path, defaultValue);
        }

        public Object get(String path) {
            return getOrDefault(path);
        }

    }


    static class LoadUrl {

        public final RestTemplate restTemplate;

        LoadUrl(RestTemplate restTemplate) {
            this.restTemplate = restTemplate;
        }

        public Object call(String url) {
            return call(url, "JSON");
        }

        public Object call(String url, String className) {
            String result = restTemplate.getForObject(url, String.class);
            return ClassUtils.castValue(result, className);
        }

    }


    static class LoadResource {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        public Object call(String url) throws IOException {
            return call(url, "JSON");
        }

        public Object call(String url, String className) throws IOException {

            InputStream inputStream = classLoader.getResourceAsStream(url);

            if (inputStream == null) {
                return null;
            }

            String cn = StringUtils.isBlank(className) ? "JSON" : className;

            if (cn.equalsIgnoreCase("JSON")) {
                return JSON.parseObject(inputStream, JSON.class);
            }
            else if (cn.equalsIgnoreCase("JSONObject")) {
                return JSON.parseObject(inputStream, JSONObject.class);
            }
            else if (cn.equalsIgnoreCase("JSONArray")) {
                return JSON.parseObject(inputStream, JSONArray.class);
            }
            else if (cn.equalsIgnoreCase("String")) {
                return IOUtils.toString(inputStream, "utf-8");
            }
            else if (cn.equalsIgnoreCase("Base64")) {
                return new String(Base64.getEncoder().encode(IOUtils.toByteArray(inputStream)));
            }

            JSON result = JSON.parseObject(inputStream, JSON.class);
            return result;
        }

    }

    static class Iif {
        public Object call(Object testValue, Object trueValue, Object falseValue) {

            boolean test = true;

            if (testValue == null) {
                test = false;
            }
            else if (testValue instanceof Boolean) {
                test = (Boolean) testValue;
            }
            else if (testValue instanceof BigDecimal) {
                test = ((BigDecimal) testValue).intValue() == 1;
            }

            else if (testValue instanceof Number) {
                test = ((Number) testValue).intValue() == 1;
            }
            else if (testValue instanceof String) {

                String strTest = String.valueOf(testValue);
                if (StringUtils.isBlank(strTest) || strTest.toLowerCase().matches("false|no|0|0.0|否|假")) {
                    test = false;
                }
            }
            else if (testValue instanceof Collection) {

                test = !((Collection) testValue).isEmpty();
            }
            else if (testValue instanceof Map) {

                test = !((Map) testValue).isEmpty();
            }

            return test ? trueValue : falseValue;
        }
    }


    static class Self {

        public final JSONObject data;
        public final GroovyConverter converter;
        public final Self parent;
        public final JSONObject selfNode;

        @Getter
        public final JSONObject varNode = new JSONObject();


        Self(GroovyConverter converter, JSONObject data, JSONObject node) {
            this.converter = converter;
            this.data = data;
            this.selfNode = node;
            this.parent = null;
        }

        Self(JSONObject node, Self parent) {
            this.converter = parent.converter;
            this.data = parent.data;
            this.selfNode = node;
            this.parent = parent;
        }

        public Self getRoot() {

            if (null == parent) {
                return this;
            }
            else {
                return parent.getRoot();
            }

        }

        public JSONObject getRootNode() {

            if (null == parent) {
                return selfNode;
            }
            else {
                return parent.getRootNode();
            }

        }

        public Object func(String method, Object... args) throws ScriptException {

            String methodKey = "$" + method + "()";

            if (!varNode.containsKey(methodKey) && selfNode.containsKey(methodKey)) {
                varNode.put(methodKey,selfNode.get(methodKey));
                selfNode.remove(methodKey);
            }
            if (varNode.containsKey(methodKey)) {

                return converter.expressionInvoke(varNode.getString(methodKey), this, args);
            }
            else if (null != parent) {

                return parent.func(method, args);
            }
            else {
                return null;
            }

        }



        public Object attr(String name) throws ScriptException {

            Object result = getVar(name);

            if (result == null) {
                result = selfNode.get(name);
            }

            return result;

        }

        private Object getVar(String name) throws ScriptException {

            String constKey = "#" + name;
            String varKey = "$" + name;

            if (varNode.containsKey(constKey)) {
                return varNode.get(constKey);
            }
            else if (varNode.containsKey(varKey)) {
                return varNode.get(varKey);
            }
            else if (selfNode.containsKey(constKey)) {
                varNode.put(constKey,selfNode.get(constKey));
                selfNode.remove(constKey);
                return varNode.get(constKey);
            }
            else if (selfNode.containsKey(varKey)) {

                varNode.put(varKey, converter.convert(selfNode.get(varKey), data, this));
                selfNode.remove(varKey);
                return varNode.get(varKey);
            }
            else if (null != parent) {

                return parent.getVar(name);
            }
            else {

                return null;
            }

        }


    }

    static class Func {

        public final Self self;

        public Func(Self self) {
            this.self = self;
        }

        public Object call(String method,Object... args) throws ScriptException {
           return self.func(method,args);
        }

    }

    static class Attr {

        public final Self self;

        public Attr(Self self) {
            this.self = self;
        }

        public Object call(String name) throws ScriptException {
            return self.attr(name);
        }

    }

    public JSONObject convert(JSONObject template, JSONObject data) throws Exception {
        return (JSONObject) convert(template, data, new Self(this, data, template));
    }


    private Bindings initBindings(Self self) throws ScriptException {

        Bindings bindings = groovyEngine.createBindings();

        bindings.put("ifBlank", new IfBlank());
        bindings.put("cast", new Cast());
        bindings.put("iif", new Iif());
        bindings.put("data", new Data(self.data));
        bindings.put("loadUrl", new LoadUrl(restTemplate));
        bindings.put("loadResource", new LoadResource());
        bindings.put("self", self);
        bindings.put("parent", self.parent);
        bindings.put("root", self.getRoot());
        bindings.put("func", new Func(self));
        bindings.put("attr", new Attr(self));

        return bindings;

    }

    private Object expressionEval(String expression, Self self) throws ScriptException {

        Bindings bindings = initBindings(self);
        return groovyEngine.eval(GROOVY_PUB_EXPR + expression, bindings);

    }


    protected Object expressionInvoke(String expression, Self self, Object... args) throws ScriptException {

        Bindings bindings = initBindings(self);

        String scriptBegin = "def main";
        List<String> argNames = new ArrayList<>();
        int i = 0;
        for (Object arg : args) {
            String argName = "__arg__" + i++;
            argNames.add(argName);
            bindings.put(argName, arg);
        }

        String scriptEnd = "; return main(" + StringUtils.join(argNames, ",") + ");";

        return groovyEngine.eval(GROOVY_PUB_EXPR + scriptBegin + expression + scriptEnd, bindings);

    }

    public Object convert(Object value, JSONObject data, Self self) throws ScriptException {


        if (value == null) {
            return null;
        }

        else if (value instanceof String) {

            String strValue = (String) value;

            if (strValue.matches("^@\\{.+\\}$")) {

                String expression = DataUtils.extractBesieged(strValue, "@{", "}");
                return expressionEval(expression, self);

            }
            else if (strValue.matches("^@([_\\.\\w]+)?\\([^;]*\\);$")) {

                String expression = DataUtils.extractBesieged(strValue, "@", ";");
                return expressionEval(expression, self);

            }
            else {

                Map<String, String> expressionMap = new HashMap<>();

                String strResult = DataUtils.replaceBy(strValue, "@(([_\\.\\w]+)?\\([^;]*\\));", 1, (expr) -> {
                    String key = "%expr:" + expr.hashCode() + "%";
                    expressionMap.put(key, expr);
                    return key;
                });

                for (String key : expressionMap.keySet()) {

                    String expr = expressionMap.get(key);
                    Object ret = expressionEval(expr, self);

                    if (ret instanceof JSON) {
                        strResult = strResult.replaceFirst(key, JSON.toJSONString(ret));
                    }
                    else {
                        strResult = strResult.replaceFirst(key, String.valueOf(ret));
                    }
                }

                return strResult.replaceAll("'@'", "@");
            }
        }
        else if (value instanceof JSONObject) {

            JSONObject target = new JSONObject();
            JSONObject objectValue = (JSONObject) value;
            Self subSelf = new Self(objectValue, self);

            for (String k : objectValue.keySet()) {
                Object v = objectValue.get(k);
                Object v1 = convert(v, data, subSelf);
                if (k.matches("^@:\\w+\\(\\)$")) {
                    target.put(k, v);
                }
                else if (k.matches("^@:\\w+$")) {
                    subSelf.getVarNode().put(k.replaceFirst("^@:", ""), v1);
                    target.put(k, v);
                }
                else {
                    String k1 = ClassUtils.castValue(convert(k, data, subSelf), String.class);
                    target.put(k1, v1);
                }
            }

            return target;
        }
        else if (value instanceof JSONArray) {
            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray) value;
            for (Object v : arrayValue) {
                target.add(convert(v, data, self));
            }
            return target;
        }
        else {
            return value;
        }
    }

}

