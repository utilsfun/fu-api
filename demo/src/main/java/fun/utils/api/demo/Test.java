package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;


public class Test {

    public static void main(String[] args) throws IOException {

        //String s = IOUtils.resourceToString("test.json5", Charset.defaultCharset());
        //JSONObject a = JSON.parseObject(s);


        JSONObject a = JSON.parseObject(Test.class.getResourceAsStream("/test.json5"), JSONObject.class);


        System.out.println(a);

    }
}
