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


    static class IfBlankObject {
        public String call(String... str) {
            return StringUtils.firstNonBlank(str);
        }
    }


    static class CastObject {

        public Object call(Object value, String type) {
            return ClassUtils.castValue(value, type);
        }

        public Object call(Object value, String type, String format) {
            return MessageFormat.format(format, call(value, type));
        }
    }

    static class DataObject {

        final JSON rootObject;

        public DataObject(JSON rootObject) {
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


    static class LoadUrlObject {

        public final RestTemplate restTemplate;

        LoadUrlObject(RestTemplate restTemplate) {
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


    static class LoadResourceObject {

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

    static class IifObject {

        public Object call(Object testValue, Object trueValue, Object falseValue) {

            boolean test = DataUtils.testBoolean(testValue);

            return test ? trueValue : falseValue;
        }

    }


    static class SelfObject {

        public final JSONObject data;
        public final GroovyConverter converter;
        public final SelfObject parent;
        public final JSONObject selfNode;
        public final JSONObject targetNode;

        @Getter
        public final JSONObject varNode = new JSONObject();


        SelfObject(GroovyConverter converter, JSONObject data, JSONObject node, JSONObject targetNode) {
            this.converter = converter;
            this.data = data;
            this.selfNode = node;
            this.targetNode = targetNode;
            this.parent = null;
        }

        SelfObject(JSONObject node, SelfObject parent, JSONObject targetNode) {
            this.converter = parent.converter;
            this.data = parent.data;
            this.selfNode = node;
            this.parent = parent;
            this.targetNode = targetNode;
        }

        public SelfObject getRoot() {

            if (null == parent) {
                return this;
            }
            else {
                return parent.getRoot();
            }

        }

        public JSONObject getRootNode() {

            if (null == parent) {
                return targetNode;
            }
            else {
                return parent.getRootNode();
            }

        }

        public Object func(String method, Object... args) throws ScriptException {

            String methodKey = "$" + method + "()";

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

                result = targetNode.get(name);

            }

            return result;

        }

        private Object getVar(String name) throws ScriptException {

            String constKey = "#" + name;
            String varKey = "$" + name;

            Object result;

            if (varNode.containsKey(name)) {

                result = varNode.get(name);

            }
            else if (varNode.containsKey(constKey)) {

                varNode.put(name, varNode.get(constKey));
                varNode.remove(constKey);
                result =  varNode.get(name);

            }
            else if (varNode.containsKey(varKey)) {

                varNode.put(name, converter.convert(varNode.get(varKey), this));
                varNode.remove(varKey);
                result =  varNode.get(name);

            }
            else if (null != parent) {

                result =  parent.getVar(name);
            }
            else {
                return  null;
            }

            if (result instanceof String){

                return String.valueOf(result);

            }else if (result instanceof JSON){

                return DataUtils.copyJSON(result);

            }else{

                return result;

            }

        }


    }

    static class FuncObject {

        public final SelfObject self;

        public FuncObject(SelfObject self) {
            this.self = self;
        }

        public Object call(String method, Object... args) throws ScriptException {
            return self.func(method, args);
        }

    }

    static class AttrObject {

        public final SelfObject self;

        public AttrObject(SelfObject self) {
            this.self = self;
        }

        public Object call(String name) throws ScriptException {
            return self.attr(name);
        }

    }

    public JSONObject convert(JSONObject template, JSONObject data) throws Exception {
        return (JSONObject) convert(template, new SelfObject(this, data, template, new JSONObject()));
    }


    private Bindings initBindings(SelfObject self) throws ScriptException {

        Bindings bindings = groovyEngine.createBindings();

        bindings.put("ifBlank", new IfBlankObject());
        bindings.put("cast", new CastObject());
        bindings.put("iif", new IifObject());
        bindings.put("data", new DataObject(self.data));
        bindings.put("loadUrl", new LoadUrlObject(restTemplate));
        bindings.put("loadResource", new LoadResourceObject());
        bindings.put("self", self);
        bindings.put("parent", self.parent);
        bindings.put("root", self.getRoot());
        bindings.put("func", new FuncObject(self));
        bindings.put("attr", new AttrObject(self));

        return bindings;

    }

    private Object expressionEval(String expression, SelfObject self) throws ScriptException {

        Bindings bindings = initBindings(self);
        return groovyEngine.eval(GROOVY_PUB_EXPR + expression, bindings);

    }


    protected Object expressionInvoke(String expression, SelfObject self, Object... args) throws ScriptException {

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

    public Object convert(Object value, SelfObject self) throws ScriptException {


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
            SelfObject subSelf = new SelfObject(objectValue, self, target);


            List<String> keys = new ArrayList<>(objectValue.keySet());
            for (String k : keys) {
                if (k.matches("^#\\w+$") || k.matches("^\\$\\w+$") || k.matches("^\\$\\w+\\(\\)$")) {
                    subSelf.getVarNode().put(k, objectValue.get(k));
                    objectValue.remove(k);
                }
            }

            for (String k : objectValue.keySet()) {

                Object v = objectValue.get(k);

                if (v instanceof JSONObject && ((JSONObject)v).containsKey("@func")){

                    JSONObject vObject = (JSONObject)v;
                    String type = vObject.getString("@func");

                    if ("@if".equalsIgnoreCase(type)){
                        String test = vObject.getString("test");
                        Object v1 ;
                        if (DataUtils.testBoolean(expressionEval(test,subSelf))){
                            v1 = vObject.get("true");
                        }else{
                            v1 = vObject.get("false");
                        }

                        if ("*".equals(k) && v1 instanceof Map){
                          target.putAll((Map)v1);
                        } else{
                          target.put(k,v1);
                        }

                    }



                }else{
                    Object v1 = convert(v, subSelf);
                    String k1 = ClassUtils.castValue(convert(k, subSelf), String.class);
                    target.put(k1, v1);
                }

            }

            return target;
        }
        else if (value instanceof JSONArray) {
            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray) value;
            for (Object v : arrayValue) {
                target.add(convert(v, self));
            }
            return target;
        }
        else {
            return value;
        }
    }

}

