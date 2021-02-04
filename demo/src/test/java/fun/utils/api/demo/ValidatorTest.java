package fun.utils.api.demo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.common.ValidUtils;
import fun.utils.api.core.valid.AbstractValidator;
import fun.utils.api.core.valid.ValidatorIn;
import fun.utils.api.core.valid.ValidatorLength;
import fun.utils.api.core.valid.ValidatorUrl;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.MDC;

public class ValidatorTest {

    @Test
    public void testUrl() throws Exception {

        String value = "https://sdfs.com/sb.jsp";
        AbstractValidator validator =  new ValidatorUrl();

        validator.validate(value);
        System.out.println(validator.getFormatMessage());

    }

    @Test
    public void testIn() throws Exception {

        String value = "a";
        AbstractValidator validator =  new ValidatorIn();
        JSONObject p = new JSONObject();
        JSONArray values = new JSONArray();
        values.add("a");
        values.add("b");
        values.add("c");

        p.put("values",values);

        validator.withParameters(p).validate(value);
        System.out.println(validator.getFormatMessage());

    }

    @Test
    public void testLength() throws Exception {

        String value = "a";
        AbstractValidator validator =  new ValidatorLength();
        JSONObject p = new JSONObject();
        p.put("min",2);
        validator.withParameters(p).validate(value);
        System.out.println(validator.getFormatMessage());

    }

    @Test
    public void testLength2() throws Exception {

        String value = "a";
               JSONObject p = new JSONObject();
        p.put("min",2);

        ValidUtils.validateValue("testLength2",value,"@Length",p,null);

    }

    @Test
    public void testMy1() throws Exception {

        String value1 = "朱寿喜";
        String value2 = "张三";
        JSONObject p = new JSONObject();
        p.put("min",2);
        MDC.put("traceId", RandomStringUtils.randomAlphabetic(8));
        ValidUtils.validateValue("value1",value1,"@:fun.utils.api.demo.MyValidator1",p,null);
        ValidUtils.validateValue("value2",value2,"@:fun.utils.api.demo.MyValidator1",p,null);

    }

}
