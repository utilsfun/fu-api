package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import fun.utils.api.core.common.groovy.ScriptMethod;
import fun.utils.api.core.common.groovy.ScriptParameter;
import fun.utils.api.core.common.groovy.ScriptRunner;
import org.junit.Test;

import java.util.Map;

public class MyTest {

    @Test
    public void test1 () throws Exception {
        ScriptMethod scriptMethod = new ScriptMethod();


        scriptMethod.setTitle("title");
        scriptMethod.setScript("return 1;");
        scriptMethod.setReturnType("Integer");

        Map<String,ScriptParameter> parameters = scriptMethod.getParameters();

        parameters.put("a",new ScriptParameter());
        parameters.put("b",new ScriptParameter());

        System.out.println(JSON.toJSONString(scriptMethod,true));

        System.out.println(ScriptRunner.execute(scriptMethod,null,null));

    }

}
