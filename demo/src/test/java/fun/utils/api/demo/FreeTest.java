package fun.utils.api.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import fun.utils.common.DataUtils;
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

    @Test
    public void test5() throws IOException {
        System.out.println(System.currentTimeMillis() );
        System.out.println((System.currentTimeMillis() / 1000) % 1000000000);
    }


    private Object p(Object p){
        return p;
    }

    @Test
    public void test6() throws IOException {

        System.out.println( p(1==2 ? (System.currentTimeMillis() / 1000) % 1000000000 : 0 ));
    }

    @Test
    public void test7() throws IOException {

        String s = "t_ldx_fx.fxa_order_all";
        String s2 = "t_ldx_fx.fxa_order_20210317";

        String regex = "t_ldx_fx\\.((?!\\d{6}).)*";

        System.out.println(s.matches("^"+ regex +"$"));
        System.out.println(s2.matches("^"+ regex +"$"));
    }



}

