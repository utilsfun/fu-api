package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;

import fun.utils.jsontemplate.common.DataUtils;

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


    @Test
    public void test2() throws IOException {

        String regex = "@<%(((?!@<%).)+)%>";


        String s1 = "aa@<%=data.1 '@'<%=data.3%> %>;@<%=data.2%>;";
        List<String> groupValues1 = DataUtils.extractList(s1,regex,1);
        System.out.println(JSON.toJSONString(groupValues1));


    }

    @Test
    public void test3() throws IOException {

        String regex = "@(([_\\.\\w]+)?\\([^;]*\\));";


        String s1 = "aa@<%=data.1 '@'<%=data.3%> %>;@data(2);";
        List<String> groupValues1 = DataUtils.extractList(s1,regex,1);
        System.out.println(JSON.toJSONString(groupValues1));

        String s2 = "@data(2); @(data.2); '@'(ddd) @cast(load('url',data.s),'json','cc');";
        List<String> groupValues2 = DataUtils.extractList(s2,regex,1);
        System.out.println(JSON.toJSONString(groupValues2));

    }


    @Test
    public void test4() throws IOException {

        String r1 =  "@<%((?!@<%).)+%>";
        String r2 =  "@([_\\.\\w]+)?\\([^;]*\\);";

        String regex = r1 + "|" + r2;


        String s1 = "aa@<%=data.1 '@'<%=data.3%> %>;@data(2); @<%=data.2%>;";
        List<String> groupValues1 = DataUtils.extractList(s1,regex,0);
        System.out.println(JSON.toJSONString(groupValues1));

        String s2 = "@data(2); @(data.2); '@'(ddd) @cast(load('url',data.s),'json','cc');";
        List<String> groupValues2 = DataUtils.extractList(s2,regex,0);
        System.out.println(JSON.toJSONString(groupValues2));

    }

}
