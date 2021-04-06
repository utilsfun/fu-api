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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

@Slf4j
public class GroovyConverter {

    private static String GROOVY_PUB_EXPR = "";

    private static RestTemplate defaultRestTemplate = new RestTemplate();

    protected static final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

    static {

        GROOVY_PUB_EXPR += "import com.alibaba.fastjson.*; \r\n";
        GROOVY_PUB_EXPR += "import org.apache.commons.lang3.*; \r\n";
    }


    @Getter
    @Setter
    private RestTemplate restTemplate = defaultRestTemplate;



    private final Map<String, Object> beans = new HashMap<>();


    public GroovyConverter() {
    }


    public GroovyConverter(Map<String, Object> beans) {
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

        final SelfObject selfObject;

        public DataObject(SelfObject selfObject) {
            this.selfObject = selfObject;
        }

        public boolean contains(String path) {
            return JSONPath.contains(selfObject.getData(), path);
        }

        private Object getOrDefault(String path) {
            return getOrDefault(path, null);
        }

        private Object getOrDefault(String path, Object defaultValue) {
            return JSONPath.contains(selfObject.getData(), path) ? JSONPath.eval(selfObject.getData(), path) : defaultValue;
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
            } else if (cn.equalsIgnoreCase("JSONObject")) {
                return JSON.parseObject(inputStream, JSONObject.class);
            } else if (cn.equalsIgnoreCase("JSONArray")) {
                return JSON.parseObject(inputStream, JSONArray.class);
            } else if (cn.equalsIgnoreCase("String")) {
                return IOUtils.toString(inputStream, "utf-8");
            } else if (cn.equalsIgnoreCase("Base64")) {
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


        public final GroovyConverter converter;
        public final SelfObject parent;

        @Getter
        public final JSONObject varNode = new JSONObject();

        @Getter
        @Setter
        protected JSON data;


        SelfObject(GroovyConverter converter, JSON data) {
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
            } else {
                return parent.getRoot();
            }

        }

        public Object func(String method, Object... args) throws Exception {

            String methodKey = "$" + method + "()";

            if (varNode.containsKey(methodKey)) {

                return converter.expressionInvoke(varNode.getString(methodKey), this, args);

            } else if (null != parent) {

                return parent.func(method, args);
            } else {

                throw new Exception("local func is not exist :" + methodKey);
            }

        }


        //取变量值,递归到上级
        public Object vars(String name) throws Exception {

            Object result = getVar(name);

            if (result == null && null != parent) {
                result = parent.vars(name);
            }

            if (result == null){
                log.debug("vars(all) is null :" + name);
            }
            return result;

        }

        //取变量值,不递归到上级
        public Object getVar(String name) throws Exception {

            if (varNode.containsKey("$")) {
                Object v = varNode.get("$");
                varNode.remove("$");
                if (v instanceof JSONObject) {
                    JSONObject vMap = (JSONObject) v;
                    for (String k : vMap.keySet()) {
                        varNode.put(k.startsWith("$") ? k : "$" + k, vMap.get(k));
                    }
                }
                return getVar(name);
            }

            if (varNode.containsKey("$#")) {
                Object v = varNode.get("$#");
                varNode.remove("$#");
                if (v instanceof JSONObject) {
                    JSONObject vMap = (JSONObject) v;
                    for (String k : vMap.keySet()) {
                        String key = k.startsWith("$") ? k.substring(1) : k;
                        key = key.endsWith("#") ? key.substring(0, key.length() - 1) : key;
                        varNode.put(key, vMap.get(k));
                    }
                }
                return getVar(name);
            }

            String varKey = "$" + name;
            String constKey = "$" + name + "#";

            Object result = null;

            if (varNode.containsKey(name)) {

                result = varNode.get(name);

            } else if (varNode.containsKey(constKey)) {
                log.debug("vars $ const :" + name);
                varNode.put(name, varNode.get(constKey));
                varNode.remove(constKey);
                result = varNode.get(name);

            } else if (varNode.containsKey(varKey)) {

                log.debug("vars $ convert :" + name);
                varNode.put(name, converter.convert(varNode.get(varKey), this));
                varNode.remove(varKey);
                result = varNode.get(name);

            }else{
               log.debug("vars(local) is not exist :" + name);
            }

            if (result instanceof String) {

                return String.valueOf(result);

            } else if (result instanceof JSON) {

                return DataUtils.copyJSON(result);

            } else {
                return result;
            }

        }


    }

    static class FuncObject {

        public final SelfObject self;

        public FuncObject(SelfObject self) {
            this.self = self;
        }

        public Object call(String method, Object... args) throws Exception {
            return self.func(method, args);
        }

    }

    static class VarsObject implements Callback<String,Object>{

        public final SelfObject self;

        public VarsObject(SelfObject self) {
            this.self = self;
        }

        @SneakyThrows
        public Object call(String name)  {
            return self.vars(name);
        }

        public Object get(String name) throws Exception {
            return self.vars(name);
        }
    }

    public Object convert(Object template, JSON data) throws Exception {
        return convert(template, new SelfObject(this, data));
    }


    private Bindings initBindings(SelfObject self) throws ScriptException {

        Bindings bindings = new SimpleBindings(){
            @SneakyThrows
            @Override
            public Object get(Object key) {

                Object result = super.get(key);

                if ( result == null && String.valueOf(key).startsWith("$") && super.containsKey("vars")){
                    VarsObject vars = (VarsObject)super.get("vars");
                    result = vars.get(String.valueOf(key).substring(1));
                }

                return result;
            }

            @SneakyThrows
            @Override
            public boolean containsKey(Object key) {
                boolean result = super.containsKey(key);

                if ( result == false && String.valueOf(key).startsWith("$") && super.containsKey("vars")){
//                  VarsObject vars = (VarsObject)super.get("vars");
//                  result = null != vars.get(String.valueOf(key).substring(1));
                    result = true;
                }

                return result;
            }
        };

        bindings.putAll(beans);

        bindings.put("ifBlank", new IfBlankObject());
        bindings.put("cast", new CastObject());
        bindings.put("iif", new IifObject());
        bindings.put("data", new DataObject(self));

        bindings.put("loadUrl", new LoadUrlObject(restTemplate));
        bindings.put("loadResource", new LoadResourceObject());


//        bindings.put("self", self);
//        bindings.put("parent", self.parent);
//        bindings.put("root", self.getRoot());

        bindings.put("func", new FuncObject(self));
        bindings.put("vars", new VarsObject(self));


        return bindings;

    }

    private Object expressionEval(String expression, SelfObject self) throws Exception {


        if (DataUtils.isBesieged(expression, "(", ")")) {
            expression = DataUtils.extractBesieged(expression, "(", ")");
        }

        if (expression.matches(":[\\._\\w]+")) {

            String expr = expression.substring(1);
            Object result = JSONPath.eval(self.getData(), expr);
            log.debug( "JSONPath.eval(\"" + expr + "\"):" + JSON.toJSONString(result));
            return result;

        } else if (expression.matches("\\$[\\._\\w]+")) {

            String expr = expression.substring(1);
            String varName = StringUtils.substringBefore(expr,".");
            String varPath = StringUtils.substringAfter(expr,varName);
            Object result = self.vars(varName);
            if (result != null && StringUtils.isNotBlank(varPath)){
                result = JSONPath.eval(result, varPath);
            }
            log.debug( "self.vars(\"" + expr + "\"):" + JSON.toJSONString(result));
            return result;

        } else {

            try {
                Bindings bindings = initBindings(self);
                log.debug( "groovyEngine.eval(\"" + expression + "\")");
                Object result = groovyEngine.eval(GROOVY_PUB_EXPR + expression, bindings);
                log.debug( "return " + JSON.toJSONString(result));
                return result;

            } catch (ScriptException e) {

                throw new Exception(e.toString() + " with: " + expression, e);

            }

        }
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

    public Object convert(Object value, SelfObject self) throws Exception {


        if (value == null) {
            return null;
        }

        if (value instanceof String) {

            String strValue = (String) value;

            if (strValue.matches("^@\\{.+\\}$")) {

                String expression = DataUtils.extractBesieged(strValue, "@{", "}");
                return expressionEval(expression, self);

            } else if (strValue.matches("^@([_\\.\\w]+)?\\([^;]*\\);?$")) {

                String expression = DataUtils.extractBesieged(strValue, "@", ";");
                return expressionEval(expression, self);

            } else {

                Map<String, String> expressionMap = new HashMap<>();

                String strResult = DataUtils.replaceBy(strValue, "@(([_\\.\\w]+)?\\([^;]*\\));", 1, (expr) -> {
                    String key = "%expr:" + expr.hashCode() + "%";
                    expressionMap.put(key, expr);
                    return key;
                });

                strResult = DataUtils.replaceBy(strResult, "@\\{((\\$|:)[\\.\\w_]+)\\}", 1, (expr) -> {
                    String key = "%expr:" + expr.hashCode() + "%";
                    expressionMap.put(key, expr);
                    return key;
                });

                for (String key : expressionMap.keySet()) {

                    String expr = expressionMap.get(key);
                    Object ret = expressionEval(expr, self);

                    if (ret instanceof JSON) {

                        strResult = strResult.replaceFirst(key, JSON.toJSONString(ret));
                    } else {
                        strResult = strResult.replaceFirst(key, String.valueOf(ret));
                    }
                }

                return strResult.replaceAll("'@'", "@");
            }
        } else if (value instanceof JSONObject && (((JSONObject) value).containsKey("@func#") ||((JSONObject) value).containsKey("@func"))) {

            JSONObject objectValue = (JSONObject) value;
            boolean isConvert = objectValue.containsKey("@func");

            String type = isConvert ? objectValue.getString("@func") : objectValue.getString("@func#");

            if ("@if".equalsIgnoreCase(type)) {

                Object select = convert(objectValue.get("select"), self);
                Object funcValue = DataUtils.testBoolean(select) ? objectValue.get("true") : objectValue.get("false");
                return isConvert ? convert(funcValue, self) : funcValue;

            } else if ("@switch".equalsIgnoreCase(type)) {

                Object select = convert(objectValue.get("select"), self);
                Object funcValue = objectValue.containsKey(select) ? objectValue.get(select) : objectValue.get("default");

                return isConvert ? convert(funcValue, self) : funcValue;

            } else if ("@each".equalsIgnoreCase(type)) {

                Object select = convert(objectValue.get("select"), self);

                Object body = objectValue.get("for");

                if (body == null){
                    JSONObject bodyObject = DataUtils.copyJSONObject(objectValue);
                    bodyObject.remove("@func");
                    bodyObject.remove("select");
                    body = bodyObject;
                }

                JSONArray funcValue = new JSONArray();

                if (select instanceof Object[]){
                    select = Arrays.asList((Object[])select);
                }

                if (select instanceof List) {

                    int index = 0;
                    for (Object obj : (List) select) {

                        log.trace("@each list index:" + index);

                        SelfObject subSelf = new SelfObject(self);
                        subSelf.varNode.put("$key", index++);
                        subSelf.varNode.put("$item", obj);
                        Object subTarget = convert(body, subSelf);
                        if (subTarget instanceof List){
                            funcValue.addAll((List)subTarget);
                        }else{
                            funcValue.add(subTarget);
                        }

                    }

                } else if (select instanceof Map) {

                    Map selectMap = (Map) select;

                    for (Object key : selectMap.keySet()) {

                        log.trace("@each list Map:" + key);

                        SelfObject subSelf = new SelfObject(self);
                        subSelf.varNode.put("$key", key);
                        subSelf.varNode.put("$item", selectMap.get(key));
                        funcValue.add(convert(body, subSelf));
                    }

                }else{
                    log.warn("@each select is not list or map :" + JSON.toJSONString(select));
                }

                return isConvert ? convert(funcValue, self) : funcValue;

            } else {

                Object func = getBean(type);


                Object funcValue = null;

                if (func != null && func instanceof Callback) {

                    JSONObject input = DataUtils.copyJSONObject(objectValue);
                    input.remove("@func");
                    input.remove("@func#");
                    funcValue = ((Callback) func).call(convert(input, self));

                }else{
                    throw new Exception("can not find func :" + type ) ;
                }

                return isConvert ? convert(funcValue, self) : funcValue;

            }

        } else if (value instanceof JSONObject) {

            JSONObject target = new JSONObject();
            JSONObject objectValue = (JSONObject) value;
            SelfObject subSelf = new SelfObject(self);


            if (objectValue.containsKey("@")) {
                subSelf.setData(ClassUtils.castValue(convert(objectValue.get("@"), self), JSONObject.class));
                objectValue.remove("@");
            }

            if (objectValue.containsKey("$")) {
                self.varNode.put("$", objectValue.get("$"));
                objectValue.remove("$");
            }

            if (objectValue.containsKey("$#")) {
                self.varNode.put("$#", objectValue.get("$#"));
                objectValue.remove("$#");
            }

            if (objectValue.containsKey("#")) {
                //只是一个注释
                objectValue.remove("#");
            }

            List<String> keys = new ArrayList<>(objectValue.keySet());
            for (String k : keys) {

                if (k.matches("\\$[\\._\\w]+#")) {
                    // 变量，不转换
                    subSelf.varNode.put(k, objectValue.get(k));
                    objectValue.remove(k);

                } else if (k.matches("\\$[\\._\\w]+")) {

                    //变量,转换
                    subSelf.varNode.put(k, objectValue.get(k));
                    objectValue.remove(k);

                } else if (k.matches("\\$[\\._\\w]+\\(\\)")) {
                    //方法
                    subSelf.varNode.put(k, objectValue.get(k));
                    objectValue.remove(k);
                }

            }


            for (String k : objectValue.keySet()) {

                Object v = objectValue.get(k);

                if (k.matches("[\\._\\w]+#")) {
                    // 值,不转换
                    String k1 = k.substring(0, k.length() - 1);
                    target.put(k1, objectValue.get(k));
                    continue;
                }

                if ("*#".equals(k) && v instanceof Map) {
                    //当键为'*', 同时值转换后为 JSONObject,则不转换并入目标JSONObject
                    target.putAll((Map) v);
                    continue;
                }

                if ("@".equals(v)) {
                    v = "@{:" + k + "}";
                }

                if ("$".equals(v)) {
                    v = "@{$" + k + "}";
                }

                Object v1 = convert(v, subSelf);

                if ("*".equals(k) && v1 instanceof Map) {
                    //当键为'*', 同时值转换后为 JSONObject,则并入目标JSONObject
                    target.putAll((Map) v1);
                } else {

                    //键也转换
                    String k1 = k;
                    k1.replaceAll("^'$'", "$");
                    k1.replaceAll("'#'$", "#");
                    k1 = ClassUtils.castValue(convert(k, subSelf), String.class);

                    //加入目标JSONObject
                    target.put(k1, v1);
                }

            }

            return target;

        } else if (value instanceof JSONArray) {

            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray) value;

            for (Object v : arrayValue) {

                Object v1 = convert(v, self);

                if (!(v instanceof List) && v1 instanceof List) {
                    //当item不为子list,同时转换后为list, 则并入目标list
                    target.addAll((List) v1);
                } else {
                    //加入转换后的值
                    target.add(v1);
                }

            }

            return target;

        } else {

            // String Array Object 三种可转换，其它原样返回
            return value;

        }
    }

}

