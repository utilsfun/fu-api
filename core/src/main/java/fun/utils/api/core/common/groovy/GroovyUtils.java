package fun.utils.api.core.common.groovy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class GroovyUtils {

    private final static ConcurrentMap<String,Class<?>> classMappings = new ConcurrentHashMap<>();
    private final static Cache<String,GroovyRunner> cacheRunner = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(2,TimeUnit.MINUTES).build();

    static {
        Class<?>[] classes = new Class[]{
                Object.class,
                java.lang.Cloneable.class,
                java.lang.Exception.class,
                java.lang.RuntimeException.class,
                java.lang.IllegalAccessError.class,
                java.lang.IllegalAccessException.class,
                java.lang.IllegalArgumentException.class,
                java.lang.IllegalMonitorStateException.class,
                java.lang.IllegalStateException.class,
                java.lang.IllegalThreadStateException.class,
                java.lang.IndexOutOfBoundsException.class,
                java.lang.InstantiationError.class,
                java.lang.InstantiationException.class,
                java.lang.InternalError.class,
                java.lang.InterruptedException.class,
                java.lang.LinkageError.class,
                java.lang.NegativeArraySizeException.class,
                java.lang.NoClassDefFoundError.class,
                java.lang.NoSuchFieldError.class,
                java.lang.NoSuchFieldException.class,
                java.lang.NoSuchMethodError.class,
                java.lang.NoSuchMethodException.class,
                java.lang.NullPointerException.class,
                java.lang.NumberFormatException.class,
                java.lang.OutOfMemoryError.class,
                java.lang.SecurityException.class,
                java.lang.StackOverflowError.class,
                java.lang.StringIndexOutOfBoundsException.class,
                java.lang.TypeNotPresentException.class,
                java.lang.VerifyError.class,
                java.lang.StackTraceElement.class,
                java.util.HashMap.class,
                java.util.Hashtable.class,
                java.util.TreeMap.class,
                java.util.IdentityHashMap.class,
                java.util.WeakHashMap.class,
                java.util.LinkedHashMap.class,
                java.util.HashSet.class,
                java.util.LinkedHashSet.class,
                java.util.TreeSet.class,
                java.util.ArrayList.class,
                java.util.concurrent.TimeUnit.class,
                java.util.concurrent.ConcurrentHashMap.class,
                java.util.concurrent.atomic.AtomicInteger.class,
                java.util.concurrent.atomic.AtomicLong.class,
                java.util.Collections.EMPTY_MAP.getClass(),
                java.lang.Boolean.class,
                java.lang.Character.class,
                java.lang.Byte.class,
                java.lang.Short.class,
                java.lang.Integer.class,
                java.lang.Long.class,
                java.lang.Float.class,
                java.lang.Double.class,
                java.lang.Number.class,
                java.lang.String.class,
                java.math.BigDecimal.class,
                java.math.BigInteger.class,
                java.util.BitSet.class,
                java.util.Calendar.class,
                java.util.Date.class,
                java.util.Locale.class,
                java.util.UUID.class,
                java.sql.Time.class,
                java.sql.Date.class,
                java.sql.Timestamp.class,
                java.text.SimpleDateFormat.class,
                com.alibaba.fastjson.JSONObject.class,
                com.alibaba.fastjson.JSONPObject.class,
                com.alibaba.fastjson.JSONArray.class,
        };

        for(Class clazz : classes){
            if (clazz !=null && TypeUtils.getClassFromMapping(clazz.getSimpleName()) == null) {
                TypeUtils.addMapping(clazz.getSimpleName(), clazz);
                classMappings.put(clazz.getSimpleName(), clazz);
            }
        }

        for(Class clazz : classes){
            if (clazz !=null && TypeUtils.getClassFromMapping(clazz.getSimpleName().toLowerCase()) == null) {
                TypeUtils.addMapping(clazz.getSimpleName().toLowerCase(), clazz);
                classMappings.put(clazz.getSimpleName(), clazz);
            }
        }

    }

    public static Class<?> loadClass(String className){
        Class<?> clazz = classMappings.get(className);
        if(clazz != null){
            return clazz;
        }
        return TypeUtils.loadClass(className);
    }


    public static GroovyScript scriptOf(String script) {
        GroovyScript result = new GroovyScript();
        result.setId(hash(script));
        result.setScript(script);
        result.setVersion("v:"+System.currentTimeMillis());
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

    public static GroovyRunner getRunner(GroovyScript method) throws Exception{
        GroovyRunner runner = cacheRunner.get(method.getId(),()-> new GroovyRunner(method));
        if (runner.getVersion().equals(method.getVersion())) {
            return runner;
        }else{
            return reloadRunner(method);
        }
    }

    public static GroovyRunner reloadRunner(GroovyScript method) throws Exception{
        cacheRunner.invalidate(method.getId());
        GroovyRunner runner = cacheRunner.get(method.getId(),()-> new GroovyRunner(method));
        return runner;
    }

    public static void expireRunner(String id) throws Exception{
        cacheRunner.invalidate(id);
    }

    public static void expireRunner(GroovyScript method) throws Exception{
        cacheRunner.invalidate(method.getId());
    }


}
