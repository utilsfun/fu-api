package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.common.detector.WhiteRectangleDetector;
import com.google.zxing.qrcode.QRCodeReader;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.script.*;
import java.io.IOException;
import java.util.List;

public class GroovyTest {

    static class A {
        public String name = "A";
    }

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
}
