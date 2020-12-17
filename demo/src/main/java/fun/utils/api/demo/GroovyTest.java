package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import groovy.util.GroovyScriptEngine;

import javax.naming.Binding;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class GroovyTest {

    static  class A{
        public String name = "1";
    }

    public static void test1() throws ScriptException {

        ScriptEngine groovyEngine = new ScriptEngineManager().getEngineByName("groovy");


        groovyEngine.put("aObj",new A());
        String script = "return \"hello world!\" + aObj.name ";
        // 直接执行
        Object result = groovyEngine.eval(script);

        System.out.println(JSON.toJSONString(result,true));

    }


    public static void test2() throws ScriptException {



    }

    public static void main(String[] args) throws ScriptException {

        test1();


    }
}
