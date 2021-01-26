package fun.utils.api.demo;

import apijson.Log;
import apijson.RequestMethod;
import apijson.framework.*;
import apijson.orm.SQLConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.apijson.MyParser;
import fun.utils.api.core.script.*;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class MyTest {


    GroovyService groovyService;


    @Before
    public void init() throws Exception {
        groovyService = new GroovyService();
    }


    @Test
    public void test() throws Exception {

        @Data
        class A {
            private int s = 3;
        }

        GroovyScript groovyScript = GroovyUtils.scriptOf("System.out.println(toJSONString($context)); System.out.println(toJSONString($config)); System.out.println(toJSONString(c)); return $context.s + a + b;");

        groovyScript.setTitle("title");
        groovyScript.getImports().add("import com.alibaba.fastjson.* ; ");
        groovyScript.getImports().add("org.apache.commons.lang3.StringUtils");

        groovyScript.getSourceIds().add("demo1.groovy");


        Map<String, GroovyVariable> declaredParameters = groovyScript.getDeclaredVariables();
        declaredParameters.put("a", GroovyUtils.parameterOf("int"));
        declaredParameters.put("b", GroovyUtils.parameterOf("integer"));

        GroovyVariable  variableC = GroovyUtils.parameterOf("integer");
        variableC.setArray(true);
        declaredParameters.put("c", variableC);

        groovyScript.setReturnType("Integer");


        groovyScript.setId(GroovyUtils.hash(groovyScript));

        System.out.println(JSON.toJSONString(groovyScript, true));

        Object result = null;
        JSONObject variables = new JSONObject();
        variables.put("a", 1);
        variables.put("b", 2);
        variables.put("c", "[1,2,3]");
        GroovyRunner runner = groovyService.getRunner(groovyScript);

        long bTime = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            result = runner.execute(new A(), variables);
        }
        System.out.println(System.currentTimeMillis() - bTime);
        System.out.println(JSON.toJSONString(result));


    }


    @Test
    public void test1() throws Exception {

        @Data
        class A {
            private int s = 3;
        }

        GroovyScript method = new GroovyScript();

        method.setTitle("title");
        method.getImports().add("import java.lang.*");

        Map<String, GroovyVariable> declaredParameters = method.getDeclaredVariables();
        declaredParameters.put("a", GroovyUtils.parameterOf("int"));
        declaredParameters.put("b", GroovyUtils.parameterOf("integer"));
        method.setSource("return context.s + a + b;");
        method.setReturnType("Integer");


        method.setId(GroovyUtils.hash(method));

        System.out.println(JSON.toJSONString(method, true));

        Object result = null;
        JSONObject parameters = new JSONObject();
        parameters.put("a", 1);
        parameters.put("b", 2);
        GroovyRunner runner = groovyService.getRunner(method);

        long bTime = System.currentTimeMillis();

        for (int i = 0; i < 1000000; i++) {
            result = runner.execute(new A(), parameters);
        }
        System.out.println(System.currentTimeMillis() - bTime);
        System.out.println(JSON.toJSONString(result));


    }


    @Test
    public void testApiJson() throws Exception {

        // Log.DEBUG = false;

       APIJSONCreator creator = new APIJSONCreator() {
            @Override
            public SQLConfig createSQLConfig() {
                return new DemoSQLConfig();
            }

        };

        MyParser myParser =  new MyParser(RequestMethod.GET,false, creator);


        JSONObject request = new JSONObject();
        request.put("API_APPLICATION",JSON.parseObject("{id:1}"));



        System.out.println( myParser.parseResponse(request));
    }

}
