package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.fastjson.JSONPath;
import fun.utils.common.ClassUtils;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import javax.script.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GroovyTest {

    public static void test1() throws ScriptException {

        ScriptEngineManager manager = new ScriptEngineManager();
// 得到所有的脚本引擎工厂

        List<ScriptEngineFactory> factories = manager.getEngineFactories();
// 这是Java SE 5 和Java SE 6的新For语句语法

        for (ScriptEngineFactory factory : factories) {
// 打印脚本信息

            System.out.printf("Name: %s%n" +
                            "Version: %s%n" +
                            "Language name: %s%n" +
                            "Language version: %s%n" +
                            "Extensions: %s%n" +
                            "Mime types: %s%n" +
                            "Names: %s%n",
                    factory.getEngineName(),
                    factory.getEngineVersion(),
                    factory.getLanguageName(),
                    factory.getLanguageVersion(),
                    factory.getExtensions(),
                    factory.getMimeTypes(),
                    factory.getNames());
// 得到当前的脚本引擎

            ScriptEngine engine = factory.getScriptEngine();
        }

        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");


        groovyEngine.put("aObj", new A());


        String script = "return \"hello world!\" + ' ' + aObj.name; ";
        // 直接执行
        Object result = groovyEngine.eval(script);

        System.out.println(JSON.toJSONString(result, true));

    }

    public static void test2() throws ScriptException, IOException, NoSuchMethodException {

        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");


        String script = "  \"hello world!\" + a + aObj.name; ";
        CompiledScript compiledScript = ((Compilable) groovyEngine).compile(script);

        Bindings bindings = groovyEngine.createBindings();
        bindings.put("a", 23);
        bindings.put("aObj", new A());


        // 直接执行
        Object result = compiledScript.eval(bindings);
        System.out.println(JSON.toJSONString(result, true));

        bindings = groovyEngine.createBindings();
        bindings.put("a", 33);
        bindings.put("aObj", new A());

        result = compiledScript.eval(bindings);
        System.out.println(JSON.toJSONString(result, true));


    }

    public static void test3() throws ScriptException, IOException, NoSuchMethodException {

        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");


        String script = " def test(o){ return 'r:' + o.name + a + aObj.name; } ";


        CompiledScript compiledScript = ((Compilable) groovyEngine).compile(script);

        Bindings bindings = groovyEngine.createBindings();
        bindings.put("a", 1);
        bindings.put("aObj", new A());

        compiledScript.eval(bindings);

        Invocable invocable = (Invocable) groovyEngine;
        Object result = invocable.invokeFunction("test", new A());
        System.out.println(JSON.toJSONString(result, true));

    }

    public static void test4() throws ScriptException, IOException, NoSuchMethodException {

        GroovyScriptEngine groovyScriptEngine = new GroovyScriptEngine("");
        String script = " def test(o){ return 'r:' + o.name + a + aObj.name; } ";
        Class cls = groovyScriptEngine.getGroovyClassLoader().parseClass(script);
        Binding binding = new Binding();
        binding.setVariable("a", 1);
        binding.setVariable("aObj", new A());
        Script groovyScript = InvokerHelper.createScript(null, binding);

        // Object result = groovyScript.invokeMethod("test",new A());
        // System.out.println(JSON.toJSONString(result,true));

        //groovyScript.evaluate("def hello(o) { 'hello:' + o }");

        System.out.println(JSON.toJSONString(groovyScript.evaluate(" import com.alibaba.fastjson.JSON; def hello(o) { a + ':hello,' + JSON.toJSONString(o); } ; hello(aObj)"), true));


    }

    public static void main(String[] args) throws ScriptException, IOException, NoSuchMethodException {


        System.out.println("------------------------");
        test1();
        System.out.println("------------------------");
        test2();
        System.out.println("------------------------");
        test3();
        System.out.println("------------------------");
        test4();


    }

    static class A {
        public String name = " ";


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

        final JSONObject rootObject;

        public Data(JSONObject rootObject) {
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

    @Test
    public void test14() throws ScriptException, IOException, NoSuchMethodException {


        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");
        ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("javascript");

        String pub = "import com.alibaba.fastjson.*;";

        long bTime = System.currentTimeMillis();

//        String ifBlank = " def ifBlank(String str,String defaultStr){ return org.apache.commons.lang3.StringUtils.defaultIfBlank(str,defaultStr)}; ";
        //String ifBlank = " def ifBlank = org.apache.commons.lang3.StringUtils.defaultIfBlank ; ";

        String ifBlank = "";


        Object result = null;


            Bindings bindings = groovyEngine.createBindings();

            bindings.put("ifBlank",new IfBlank());
            bindings.put("cast",new Cast());
            bindings.put("iif",new Iif());
            bindings.put("data",new Data(JSONObject.parseObject("{id:33,name:'abc',wife:{age:18}}")));
            bindings.put("loadUrl",new LoadUrl(new RestTemplate()));
            bindings.put("loadResource",new LoadResource());

            bindings.put("a", 23);
            bindings.put("aObj", new A());
            bindings.put("k", JSONObject.parseObject("{s:33}"));

            // CompiledScript compiledScript = ((Compilable) groovyEngine).compile(scriptCode + "; return test();");
            // CompiledScript compiledScript = ((Compilable) groovyEngine).compile("a < 22 ? true : false");
            // result = groovyEngine.eval(ifBlank + "ifBlank(aObj.name,null,'default name')",bindings);

           //  result = groovyEngine.eval(pub + "cast('[1,2,3]','JSONArray','this is {0};')",bindings);
          //  result = groovyEngine.eval(pub + "iif([],'true value','false value ')",bindings);


        result = groovyEngine.eval(pub + "data.'wife.age.sd'",bindings);
        System.out.println(JSON.toJSONString(result));

         result = groovyEngine.eval(pub + "data('wife.age')",bindings);
         System.out.println(JSON.toJSONString(result));

        result = groovyEngine.eval(pub + "data.wife.age",bindings);
        System.out.println(JSON.toJSONString(result));

        result = groovyEngine.eval(pub + "data.'wife.age'",bindings);
        System.out.println(JSON.toJSONString(result));

        result = groovyEngine.eval(pub + "data('wife.age.ff',[]) ",bindings);
        System.out.println(JSON.toJSONString(result));


        result = groovyEngine.eval(pub + "def ret = loadUrl('https://www.sina.com.cn/api/hotword.json') ; return iif(ret.result.status.code == 0,ret.result.data,ret.error) ",bindings);
        System.out.println(JSON.toJSONString(result));

        result = groovyEngine.eval(pub + "loadResource('demo/test1.json')",bindings);
        System.out.println(JSON.toJSONString(result));



        System.out.println(System.currentTimeMillis() - bTime);

    }
}
