package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import fun.utils.common.DataUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.List;


public class ConvertTest {

    @Test
    public void test1() throws IOException {

        String regex = "^(\\(@(\\w+)(:'(.+)')?\\))?(.+)$";

        String s1 = "(@string:'名字:{}')wife.nickname||wife.nickname||'无名氏'";
        List<String> groupValues1 = DataUtils.extractGroups(s1,regex);
        System.out.println(JSON.toJSONString(groupValues1));

        String s2 = "wife.nickname||wife.nickname||'无名氏'";
        List<String> groupValues2 = DataUtils.extractGroups(s2,regex);
        System.out.println(JSON.toJSONString(groupValues2));


        String s3 = "(@string)wife.nickname||wife.nickname||'无名氏'";
        List<String> groupValues3 = DataUtils.extractGroups(s3,regex);
        System.out.println(JSON.toJSONString(groupValues3));


    }



}
