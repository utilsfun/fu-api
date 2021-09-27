package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Test {

    public static void main(String[] args) throws IOException {

        //String s = IOUtils.resourceToString("test.json5", Charset.defaultCharset());
        //JSONObject a = JSON.parseObject(s);


        JSONObject a = JSON.parseObject(Test.class.getResourceAsStream("/test.json5"), JSONObject.class);


        System.out.println(a);

        String id = "1";
        List<JSONObject> list = new ArrayList<>();
        LinkedList<String> ids = new LinkedList<>();
      //  list.forEach(obj -> ids.add(obj.id));
      //  for (obj:) {ids.add(obj.id)};
        int idx = ids.indexOf(id);
        int newIdx = idx > 0 ? idx - 1 : 0;
        ids.remove(id);
        ids.add(newIdx,id);



    }
}
