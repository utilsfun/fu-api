package fun.utils.api.demo;

import apijson.JSON;
import com.sun.activation.registries.MimeTypeFile;
import fun.utils.api.core.common.DataUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.MimeTypeUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.Arrays;

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
}

