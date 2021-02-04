package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.DataUtils;
import org.junit.Test;

import java.io.IOException;

public class DataTest {


    @Test
    public void test1() throws IOException {

        JSONObject s = (JSONObject) JSON.parse("{s:'朱',r:'@{/Sdd3\\\\[\\\\]}',ddi:'@{/D[1]}','@{name}':['@{/D[0]}']}");
        JSONObject p = (JSONObject) JSON.parse("{'Sdd3[]':'什么什么',D:[1,2],name:'中国'}");
        JSONObject target = DataUtils.fullRefObject(s,p);
        System.out.println(target);
    }
}
