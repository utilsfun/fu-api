package fun.utils.api.demo;

import apijson.RequestMethod;
import apijson.framework.*;
import apijson.orm.SQLConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.apijson.ApiJsonParser;
import fun.utils.common.DataUtils;
import fun.utils.api.core.script.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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

        List<String> ss= new ArrayList<>();
        ss.add("System.out.println(toJSONString($context));");
        ss.add("$config.age = 99");
        ss.add("System.out.println($config.age);");
        ss.add("System.out.println(c[0]);");
        ss.add("return $context.s + a + b;");

        Map<String,Object> context = new HashMap<>();
        context.put("s",4);

        GroovyScript groovyScript = GroovyUtils.scriptOf(StringUtils.join(ss,"\r\n"));

        groovyScript.setTitle("title");
        groovyScript.getImports().add("import com.alibaba.fastjson.* ; ");
        groovyScript.getImports().add("org.apache.commons.lang3.StringUtils");

        groovyScript.getSourceIds().add("demo1.groovy");


        groovyScript.setConfig(JSON.parseObject("{age:100}"));
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
            result = runner.withProperty("$context",context).execute(variables);
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
        method.setSource("return $context.s + a + b;");
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
            result = runner.withProperty("$context",new A()).execute(parameters);
        }
        System.out.println(System.currentTimeMillis() - bTime);
        System.out.println(JSON.toJSONString(result));


    }



    @Test
    public void testStringFormat() throws Exception {

        // Log.DEBUG = false;
        Map<String,Object> objectMap = new HashMap<>();
        objectMap.put("cc","dd");
        objectMap.put("aa","{a:2}");

        objectMap.put("bb",JSON.parseObject("{dkd:\"3asfs\",kso:123}"));


        System.out.println(DataUtils.stringFormat("这个测测${cc}!",objectMap));
        System.out.println(DataUtils.stringFormat("这个测测${dd|default:99}!",objectMap));
        System.out.println(DataUtils.stringFormat("这个测测${dd|default:'99'|xx| d ||}!",objectMap));
        System.out.println(DataUtils.stringFormat("${aa|toJson}!",objectMap));
        System.out.println(DataUtils.stringFormat("${bb|json:}!",objectMap));
    }

}
