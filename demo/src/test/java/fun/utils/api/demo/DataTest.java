package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import fun.utils.common.DataUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class DataTest {


    @Test
    public void test2() throws IOException {

        JSONObject s = (JSONObject) JSON.parse("{m:[1,2],s:'朱',r:'@{Sdd3\\\\[\\\\]}',name:'@{name.D[1]}','@{name.sub}':['@{/D[0]}']}");
        JSONObject p = (JSONObject) JSON.parse("{m:[2,3,5],'Sdd3[]':'什么什么',name:{sub:'中国',D:[1,2]}}");
        JSONObject target = DataUtils.mergeJson(s,p);
        System.out.println(JSON.toJSONString(target,true));

        System.out.println(JSON.toJSONString(s,true));
        System.out.println(JSON.toJSONString(p,true));
    }

    @Test
    public void test3() throws IOException {

        JSONObject s = (JSONObject) JSON.parse("{m:[1,2],s:'朱',r:'@{Sdd3\\\\[\\\\]}',name:'@{name.D[1]}','@{name.sub}':['@{/D[0]}']}");
        JSONObject p = (JSONObject) JSON.parse("{m:[2,3,5],'Sdd3[]':'什么什么',name:{sub:'中国',D:[1,2]}}");

        JSONObject n1 = DataUtils.copyJSONObject(p.getJSONObject("name"));
        JSONObject n2 = (JSONObject)JSONPath.eval(p,"name");
        System.out.println(n1 == n2);
        System.out.println(n1.equals(n2));

        System.out.println(JSON.toJSONString(s,true));
        System.out.println(JSON.toJSONString(p,true));
    }

    @Test
    public void test4() throws IOException {

        String s = "@{a||'{}'},LKJ@{b|| '}@{DD}'}";

        List<String>  sList = DataUtils.extractList(s,"@\\{(((?!@\\{).)*)\\}",1);

        System.out.println(JSON.toJSONString(sList));

    }



}
