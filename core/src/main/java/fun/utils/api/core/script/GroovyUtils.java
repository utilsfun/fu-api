package fun.utils.api.core.script;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.util.DigestUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class GroovyUtils {


    private final static Cache<String, GroovyRunner> cacheRunner = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(2, TimeUnit.MINUTES).build();

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

    public static String hash(Object object) {
        String hash = DigestUtils.md5DigestAsHex(JSON.toJSONString(object).getBytes());
        return hash;
    }


}
