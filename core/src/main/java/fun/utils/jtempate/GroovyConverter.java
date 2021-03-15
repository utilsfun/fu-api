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
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class GroovyConverter {

    private static String GROOVY_PUB_EXPR = "" ;
    private static final ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");

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

        public Object call(Object value,String type) {
            return ClassUtils.castValue(value,type);
        }

        public Object call(Object value,String type,String format) {
            return MessageFormat.format(format,call(value, type));
        }
    }

    static class Data {

        final JSON rootObject;

        public Data(JSON rootObject) {
            this.rootObject = rootObject;
        }

        public boolean contains(String path){
            return JSONPath.contains(rootObject,path);
        }

        private Object getOrDefault(String path){
            return getOrDefault(path,null);
        }

        private Object getOrDefault(String path,Object defaultValue){
            return JSONPath.contains(rootObject,path) ? JSONPath.eval(rootObject,path) : defaultValue;
        }

        public Object call(String path) {
            return getOrDefault(path);
        }

        public Object call(String path,Object defaultValue) {
            return getOrDefault(path,defaultValue);
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
            return call(url,"JSON");
        }

        public Object call(String url,String className) {
            String result = restTemplate.getForObject(url,String.class);
            return ClassUtils.castValue(result,className);
        }

    }


    static class LoadResource {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        public Object call(String url) throws IOException {
            return call(url,"JSON");
        }

        public Object call(String url,String className) throws IOException {

            InputStream inputStream =  classLoader.getResourceAsStream(url);

            if (inputStream == null){
                return null;
            }

            String cn = StringUtils.isBlank(className) ? "JSON" : className;

            if (cn.equalsIgnoreCase("JSON")){
                return JSON.parseObject(inputStream, JSON.class);
            }
            else if (cn.equalsIgnoreCase("JSONObject")){
                return JSON.parseObject(inputStream,  JSONObject.class);
            }
            else if (cn.equalsIgnoreCase("JSONArray")){
                return JSON.parseObject(inputStream,  JSONArray.class);
            }
            else if (cn.equalsIgnoreCase("String")){
                return IOUtils.toString(inputStream, "utf-8");
            }
            else if (cn.equalsIgnoreCase("Base64")){
                return new String(Base64.getEncoder().encode(IOUtils.toByteArray(inputStream)));
            }

            JSON result = JSON.parseObject(inputStream, JSON.class);
            return result;
        }

    }

    static class Iif {
        public Object call(Object testValue,Object trueValue,Object falseValue) {

            boolean test = true;

            if (testValue == null){
                test =  false;
            }
            else if (testValue instanceof Boolean){
                test = (Boolean)testValue;
            }
            else if(testValue instanceof BigDecimal){
                test = ((BigDecimal) testValue).intValue() == 1;
            }

            else if(testValue instanceof Number){
                test = ((Number) testValue).intValue() == 1;
            }
            else if (testValue instanceof String ){

                String strTest = String.valueOf(testValue);
                if (StringUtils.isBlank(strTest) || strTest.toLowerCase().matches("false|no|0|0.0|否|假")){
                    test = false;
                }
            }
            else if (testValue instanceof Collection) {

                test =  !((Collection) testValue).isEmpty();
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

        Self( GroovyConverter converter,JSONObject data, JSONObject node) {
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

        public Object get(String name) throws ScriptException {
            return call(name);
        }

        public Object call(String name) throws ScriptException {

           if (varNode.containsKey(name)){

               return varNode.get(name);
           }
           else if (selfNode.containsKey(name)){

                return selfNode.get(name);
           }
           else if (selfNode.containsKey("@:" + name)){

               varNode.put(name, converter.convert(selfNode.get("@:" + name),data,this) );
               return varNode.get(name);

           }else if (selfNode.containsKey("@:" + name + "()")){

              return converter.convert(selfNode.get("@:" + name + "()"),data,this);

           }else{

               if ( null != parent) {
                   return  parent.get(name);
               }
               else {
                  return null;
              }

           }
        }
    }


    public JSONObject convert(JSONObject template, JSONObject data) throws Exception {
        return (JSONObject) convert(template, data, new Self(this,data,template));
    }


    private Object expressionRefObject(String expression,JSONObject data, Self self) throws ScriptException {

        Bindings bindings = groovyEngine.createBindings();

        bindings.put("ifBlank",new IfBlank());
        bindings.put("cast",new Cast());
        bindings.put("iif",new Iif());
        bindings.put("data",new Data(data));
        bindings.put("loadUrl",new LoadUrl(restTemplate));
        bindings.put("loadResource",new LoadResource());
        bindings.put("self",self);

        return groovyEngine.eval(GROOVY_PUB_EXPR + expression ,bindings);

    }

    public Object convert(Object value, JSONObject data, Self self) throws ScriptException {


        if (value == null){
            return null;
        }

        else if (value instanceof String) {

            String strValue = (String) value;

            if (strValue.matches("^@\\{.+\\}$")) {

                String expression = DataUtils.extractBesieged(strValue, "@{", "}");
                return expressionRefObject(expression,data,self);

            }
            else if (strValue.matches("^@([_\\.\\w]+)?\\([^;]*\\);$")) {

                String expression = DataUtils.extractBesieged(strValue, "@", ";");
                return expressionRefObject(expression,data,self);

            }
            else {

                Map<String,String> expressionMap = new HashMap<>();
                String strResult = DataUtils.replaceBy(strValue, "@(([_\\.\\w]+)?\\([^;]*\\));", 1, (expr) -> {
                    String key = "%expr:" + expr.hashCode() + "%";
                    expressionMap.put(key ,expr);
                    return key;
                });

                for (String key: expressionMap.keySet()) {
                    String expr = expressionMap.get(key);
                    Object ret = expressionRefObject(expr, data,self);
                    if (ret instanceof JSON) {
                        strResult = strResult.replaceFirst(key,JSON.toJSONString(ret));
                    }
                    else {
                        strResult = strResult.replaceFirst(key,String.valueOf(ret));
                    }
                }

                return  strResult.replaceAll("'@'","@");
            }
        }
        else if (value instanceof JSONObject){

            JSONObject target = new JSONObject();
            JSONObject objectValue = (JSONObject)value;
            Self subSelf = new Self(objectValue,self);

            for (String k: objectValue.keySet()){
                Object v = objectValue.get(k);
                Object v1 = convert(v, data, subSelf);
                if (k.matches("^@:\\w+\\(\\)$")){
                    target.put(k,v);
                }else if (k.matches("^@:\\w+$")){
                    subSelf.getVarNode().put(k.replaceFirst("^@:",""),v1);
                    target.put(k,v);
                }else{
                    String k1 = ClassUtils.castValue(convert(k,data,subSelf),String.class);
                    target.put(k1,v1);
                }
            }

            return target;
        }
        else if (value instanceof JSONArray){
            JSONArray target = new JSONArray();
            JSONArray arrayValue = (JSONArray)value;
            for (Object v: arrayValue){
                target.add(convert(v, data,self));
            }
            return  target;
        }
        else{
            return  value;
        }
    }

}

