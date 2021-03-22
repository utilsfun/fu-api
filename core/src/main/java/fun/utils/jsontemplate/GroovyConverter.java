package fun.utils.jsontemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.ClassUtils;
import fun.utils.common.DataUtils;
import javafx.util.Callback;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;


public class GroovyConverter {

    private static String GROOVY_PUB_EXPR = "";

    protected static final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

    static {
        GROOVY_PUB_EXPR += "import com.alibaba.fastjson.*; \r\n";
        GROOVY_PUB_EXPR += "import org.apache.commons.lang3.*; \r\n";
    }


    @Getter
    @Setter
    private RestTemplate restTemplate = new RestTemplate();

    @Getter
    private final Map<String, Object> context = new HashMap<>();


    private final Map<String, Object> beans = new HashMap<>();


    public GroovyConverter() {
    }

    public GroovyConverter(Map<String, Object> context) {
        if (context != null) {
            this.context.putAll(context);
        }
    }

    public GroovyConverter(Map<String, Object> context, Map<String, Object> beans) {
        if (context != null) {
            this.context.putAll(context);
        }
        if (beans != null) {
            this.beans.putAll(beans);
        }

    }

    public GroovyConverter withBean(@NonNull String name, Object bean) throws Exception {

        if (name.matches("(ifBlank|cast|iif|data|loadUrl|loadResource|self|parent|root|func|vars|\\$)")) {
            throw new Exception("bean name can not be a keyword : " + name);
        }
        beans.put(name, bean);
        return this;
    }

    public Object getBean(@NonNull String name) {

        return beans.get(name);

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

        @Getter
        public final JSONObject varNode = new JSONObject();


        SelfObject(GroovyConverter converter, JSONObject data) {
            this.converter = converter;
            this.data = data;
            this.parent = null;
        }

        SelfObject(SelfObject parent) {
            this.converter = parent.converter;
            this.data = parent.data;
            this.parent = parent;

        }

        public SelfObject getRoot() {

            if (null == parent) {
                return this;
            }
            else {
                return parent.getRoot();
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


        //取变量值,递归到上级
        public Object vars(String name) throws ScriptException {

            Object result = getVar(name);

            if (result == null && null != parent) {
                result = parent.vars(name);
            }

            return result;

        }

        //取变量值,不递归到上级
        public Object getVar(String name) throws ScriptException {

            String varKey = "$" + name;

            Object result = null;

            if (varNode.containsKey(name)) {

                result = varNode.get(name);

            }
            else if (varNode.containsKey(varKey)) {

                varNode.put(name, converter.convert(varNode.get(varKey), this));
                varNode.remove(varKey);
                result = varNode.get(name);

            }

            if (result instanceof String) {

                return String.valueOf(result);

            }
            else if (result instanceof JSON) {

                return DataUtils.copyJSON(result);

            }
            else {

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

    static class VarsObject {

        public final SelfObject self;

        public VarsObject(SelfObject self) {
            this.self = self;
        }

        public Object call(String name) throws ScriptException {
            return self.vars(name);
        }

        public Object get(String name) throws ScriptException {
            return self.vars(name);
        }
    }

    public JSONObject convert(JSONObject template, JSONObject data) throws Exception {
        return (JSONObject) convert(template, new SelfObject(this, data));
    }


    private Bindings initBindings(SelfObject self) throws ScriptException {

        Bindings bindings = groovyEngine.createBindings();

        bindings.putAll(beans);

        bindings.put("ifBlank", new IfBlankObject());
        bindings.put("cast", new CastObject());
        bindings.put("iif", new IifObject());
        bindings.put("data", new DataObject(self.data));

        bindings.put("loadUrl", new LoadUrlObject(restTemplate));
        bindings.put("loadResource", new LoadResourceObject());


//        bindings.put("self", self);
//        bindings.put("parent", self.parent);
//        bindings.put("root", self.getRoot());

        bindings.put("func", new FuncObject(self));
        bindings.put("vars", new VarsObject(self));

        bindings.put("$context", context);

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

        else if (value instanceof JSONObject && ((JSONObject) value).containsKey("@func")) {

            JSONObject vObject = (JSONObject) value;
            String type = vObject.getString("@func");

            if ("@if".equalsIgnoreCase(type)) {

                Object select = convert(vObject.get("select"), self);
                Object funcValue = DataUtils.testBoolean(select) ? vObject.get("true") : vObject.get("false");
                return convert(funcValue, self);

            }
            else if ("@switch".equalsIgnoreCase(type)) {

                Object select = convert(vObject.get("select"), self);
                Object funcValue = vObject.containsKey(select) ? vObject.get(select) : vObject.get("default");

                return convert(funcValue, self);

            }
            else if ("@each".equalsIgnoreCase(type)) {

                Object select = convert(vObject.get("select"), self);
                Object body = vObject.get("for");

                List<Object> funcValue = new ArrayList<>();

                if (select instanceof List) {

                    int index = 0;
                    for (Object obj : (List) select) {

                        SelfObject subSelf = new SelfObject(self);
                        subSelf.varNode.put("$@key", index++);
                        subSelf.varNode.put("$@item", obj);
                        funcValue.add(convert(body, subSelf));
                    }

                }
                else if (select instanceof Map) {

                    Map selectMap = (Map) select;

                    for (Object key : selectMap.keySet()) {

                        SelfObject subSelf = new SelfObject(self);
                        subSelf.varNode.put("$@key", key);
                        subSelf.varNode.put("$@item", selectMap.get(key));
                        funcValue.add(convert(body, subSelf));
                    }
                }

                return funcValue;

            }
            else {

                Object func = getBean(type);


                Object funcValue = null;

                if (func != null && func instanceof Callback) {

                    JSONObject input = DataUtils.copyJSONObject(vObject);
                    input.remove("@func");
                    funcValue = ((Callback) func).call(convert(input, self));

                }

                return convert(funcValue, self);

            }

        }
        else if (value instanceof JSONObject) {

            JSONObject target = new JSONObject();
            JSONObject objectValue = (JSONObject) value;
            SelfObject subSelf = new SelfObject(self);

            List<String> keys = new ArrayList<>(objectValue.keySet());
            for (String k : keys) {


                if (k.matches("^\\w+#$")) {
                    // 值不转换
                    String k1 = k.replaceFirst("#$", "");
                    target.put(k1, objectValue.get(k));
                    objectValue.remove(k);

                }
                else if (k.matches("^\\$\\w+#$")) {

                    // 变量 值 不转换
                    String k1 = DataUtils.extractBesieged(k, "$", "#");
                    subSelf.getVarNode().put(k1, objectValue.get(k));
                    objectValue.remove(k);

                }
                else if (k.matches("^\\$\\w+$")) {
                    // 变量 值 转换
                    String k1 = k;
                    subSelf.getVarNode().put(k1, objectValue.get(k));
                    objectValue.remove(k);

                }
                else if (k.matches("^\\$\\w+\\(\\)$")) {

                    // 自定义方法
                    String k1 = k;
                    subSelf.getVarNode().put(k1, objectValue.get(k));
                    objectValue.remove(k);

                }

            }

            for (String k : objectValue.keySet()) {

                Object v = objectValue.get(k);

                Object v1 = convert(v, subSelf);

                if ("*".equals(k) && v1 instanceof Map) {
                    //当键为'*', 同时值转换后为 JSONObject,则并入目标JSONObject
                    target.putAll((Map) v1);
                }
                else {

                    //键也转换
                    String k1 = k;
                    k1.replaceAll("^'$'","$");
                    k1.replaceAll("'#'$","#");
                    k1 = ClassUtils.castValue(convert(k, subSelf), String.class);

                    //加入目标JSONObject
                    target.put(k1, v1);
                }

            }

            return target;
        }
        else if (value instanceof JSONArray) {

            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray) value;

            for (Object v : arrayValue) {

                Object v1 = convert(v, self);

                if (!(v instanceof List) && v1 instanceof List) {
                    //当item不为子list,同时转换后为list, 则并入目标list
                    target.addAll((List) v1);
                }
                else {
                    //加入转换后的值
                    target.add(v1);
                }

            }
            return target;
        }
        else {
            return value;
        }
    }

}

