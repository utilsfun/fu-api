package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import javax.script.*;
import java.io.IOException;

public class GroovySpeedTest {

    static final String scriptCode = " def test(){ return 'r: ' + ( a + aObj.name ) + Math.pow(1.145224534312,300.12379) ; } ";

    public static void test4() throws ScriptException, IOException, NoSuchMethodException {


        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");
        ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("javascript");


        long bTime = System.currentTimeMillis();
        Object result = null;
        for (int i = 0; i < 10000; i++) {

            Bindings bindings = groovyEngine.createBindings();
            bindings.put("a", 23);
            bindings.put("aObj", new A());
            bindings.put("k", JSONObject.parseObject("{s:33}"));
          //  CompiledScript compiledScript = ((Compilable) groovyEngine).compile(scriptCode + "; return test();");

            // CompiledScript compiledScript = ((Compilable) groovyEngine).compile("a < 22 ? true : false");

            result = groovyEngine.eval("int j = 100 ; return a < " + 22 + " ? j : k.s ;",bindings);

           // result = compiledScript.eval(bindings);
        }
        System.out.println(JSON.toJSONString(result));
        System.out.println(System.currentTimeMillis() - bTime);

    }

    public static void test5() throws ScriptException, IOException, NoSuchMethodException {


        GroovyShell shell = new GroovyShell();



        Script runner = shell.parse(scriptCode);

        Binding binding = new Binding();
        binding.setVariable("a", 23);
        binding.setVariable("aObj", new A());

        long bTime = System.currentTimeMillis();
        Object result = null;
        for (int i = 0; i < 10000; i++) {

            runner.setBinding(binding);
            result = runner.invokeMethod("test", null);
        }
        System.out.println(System.currentTimeMillis() - bTime);
        System.out.println(JSON.toJSONString(result));


    }

    public static void test6() throws ScriptException, IOException, NoSuchMethodException {
        GroovyShell shell = new GroovyShell();
        Script runner = shell.parse(scriptCode + "; def main(){ return test();}");
        System.out.println( runner.getClass().getName());
        Binding binding = new Binding();
        binding.setVariable("a", 23);
        binding.setVariable("aObj", new A());
        runner.setBinding(binding);
        long bTime = System.currentTimeMillis();
        Object result = null;
        for (int i = 0; i < 1000000; i++) {
            result = InvokerHelper.createScript(runner.getClass(),binding).invokeMethod("main",null);
          //  result = runner.invokeMethod("main",null);
        }
        System.out.println(System.currentTimeMillis() - bTime);
        System.out.println(JSON.toJSONString(result));
    }

    public static void test7() throws ScriptException, IOException, NoSuchMethodException {


        Object result = null;

        B.a = "23";
        B.aObj = new A();
        B b = new B();
        long bTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {

            result = b.test();
        }
        System.out.println(System.currentTimeMillis() - bTime);

        System.out.println(JSON.toJSONString(result));


    }

    public static void main(String[] args) throws ScriptException, IOException, NoSuchMethodException {


        System.out.println("4------------------------");

        test4();

        System.out.println("5------------------------");

        test5();

        System.out.println("6------------------------");

        test6();

        System.out.println("7------------------------");

        test7();

    }

    static class A {
        public String name = "A";
    }

    static class B {
        static public String a;
        static public A aObj;

        public String test() {
            return "r:" + a + aObj.name + Math.pow(1.145224534312, 300.12379);
        }
    }
}
