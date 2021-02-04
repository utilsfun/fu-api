package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.DataUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.MediaTypeFactory;


import java.io.IOException;

public class FreeTest {
    @Test
    public void test1(){
        System.out.println("[s]".matches("\\[s\\]"));
        System.out.println("[s]".matches("\\[s]"));
        System.out.println(StringUtils.substringBetween("[s[c]]","[","]"));


        System.out.println(DataUtils.extractStr("[s[c]]","\\[(.+)]",1));
        System.out.println(DataUtils.extractStr("[s[c]]","\\[(.+)]"));
        System.out.println(DataUtils.extractStr("[s[c]]","\\[\\w+]"));

        System.out.println(DataUtils.extractList("[s]er[c],,[cc]ss","\\[([^\\[\\]]+)]",1));
    }

    @Test
    public void test2() throws IOException {
        System.out.println(MediaTypeFactory.getMediaTypes("dd.js"));

    }

    @Test
    public void test3() throws IOException {

        JSONObject o = (JSONObject) JSON.parse("{a:'朱',b:['1','2','s'],c:{d:'d'}}");

        System.out.println(o.get("a").getClass().getName());
        System.out.println(o.get("b").getClass().getName());
        System.out.println(o.get("c").getClass().getName());
    }

    @Test
    public void test4() throws IOException {

        System.out.println(DataUtils.extractBesieged("@(/si)e/d(i{k)s)","@(",")"));
    }

}

