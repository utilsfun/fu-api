package fun.utils.api.core.script;

import com.alibaba.fastjson.JSON;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroovyUtils {

    public static GroovySource sourceOf(String id, String source) {
        GroovySource result  = new GroovyScript();
        result.setId(id);
        List<String> imports = result.getImports();
        //Pattern pattern = Pattern.compile("[\\s]*import[\\s]*([a-z0-9A-Z\\.]+)[\\s]*;?[\\s]*(\\/\\/.+)?[\r\n]+");
        Pattern pattern = Pattern.compile("\\s*import\\s*([a-z0-9A-Z.]+)\\s*;?\\s*(//.+)?[\r\n]+");
        Matcher matcher = pattern.matcher(source);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            imports.add(matcher.group(1));
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        result.setSource(sb.toString());
        return result;
    }

    public static GroovyScript scriptOf(String script) {
        GroovyScript result = new GroovyScript();
        result.setId(hash(script));
        result.setSource(script);
        result.setVersion("v:" + System.currentTimeMillis());
        return result;
    }

    public static GroovyVariable parameterOf(String dataType) {
        GroovyVariable result = new GroovyVariable();
        result.setDataType(dataType);
        return result;
    }

    public static GroovyVariable parameterOf(String dataType,boolean isArray) {
        GroovyVariable result = new GroovyVariable();
        result.setDataType(dataType);
        result.setArray(isArray);
        return result;
    }

    public static String hash(Object object) {
        String hash = DigestUtils.md5DigestAsHex(JSON.toJSONString(object).getBytes());
        return hash;
    }


}
