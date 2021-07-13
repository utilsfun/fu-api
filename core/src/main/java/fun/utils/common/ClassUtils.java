package fun.utils.common;

import apijson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.script.GroovyRunner;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class ClassUtils {

    private final static ConcurrentMap<String, Class<?>> classMappings = new ConcurrentHashMap<>();

    static {
        Class<?>[] classes = new Class[]{
                Object.class,
                Cloneable.class,
                Exception.class,
                RuntimeException.class,
                IllegalAccessError.class,
                IllegalAccessException.class,
                IllegalArgumentException.class,
                IllegalMonitorStateException.class,
                IllegalStateException.class,
                IllegalThreadStateException.class,
                IndexOutOfBoundsException.class,
                InstantiationError.class,
                InstantiationException.class,
                InternalError.class,
                InterruptedException.class,
                LinkageError.class,
                NegativeArraySizeException.class,
                NoClassDefFoundError.class,
                NoSuchFieldError.class,
                NoSuchFieldException.class,
                NoSuchMethodError.class,
                NoSuchMethodException.class,
                NullPointerException.class,
                NumberFormatException.class,
                OutOfMemoryError.class,
                SecurityException.class,
                StackOverflowError.class,
                StringIndexOutOfBoundsException.class,
                TypeNotPresentException.class,
                VerifyError.class,
                StackTraceElement.class,
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
                TimeUnit.class,
                ConcurrentHashMap.class,
                java.util.concurrent.atomic.AtomicInteger.class,
                java.util.concurrent.atomic.AtomicLong.class,
                java.util.Collections.EMPTY_MAP.getClass(),
                Boolean.class,
                Character.class,
                Byte.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Number.class,
                String.class,
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

        for (Class clazz : classes) {
            if (clazz != null && TypeUtils.getClassFromMapping(clazz.getSimpleName()) == null) {
                TypeUtils.addMapping(clazz.getSimpleName(), clazz);
                classMappings.put(clazz.getSimpleName(), clazz);
            }
        }

        for (Class clazz : classes) {
            if (clazz != null && TypeUtils.getClassFromMapping(clazz.getSimpleName().toLowerCase()) == null) {
                TypeUtils.addMapping(clazz.getSimpleName().toLowerCase(), clazz);
                classMappings.put(clazz.getSimpleName(), clazz);
            }
        }

    }

    public static Class<?> loadClass(String className) {
        Class<?> clazz = classMappings.get(className);
        if (clazz != null) {
            return clazz;
        }
        return TypeUtils.loadClass(className);
    }

    public static <T> T castValue(Object value, Class<T> cls) {

        if (value == null) {
            return null;
        }

        if (JSON.class == cls){
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return (T) JSON.parse(value);
        }

        if (JSONObject.class == cls){
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return (T) JSON.parseObject(value);
        }

        if (JSONArray.class == cls){
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return (T) JSON.parseArray(value);
        }

        return TypeUtils.castToJavaBean(value, cls);

    }

    public static Object castValue(Object value, String className) {


        if (value == null) {
            return null;
        }

        if ("JSONString".equalsIgnoreCase(className)) {
            return JSON.toJSONString(value);
        }


        if ("JSON".equalsIgnoreCase(className)) {
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return JSON.parse(value);
        }


        if ("JSONObject".equalsIgnoreCase(className)) {
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return JSON.parseObject(value);
        }


        if ("JSONArray".equalsIgnoreCase(className)) {
            if (value instanceof String && StringUtils.isBlank((String) value)){
                return null;
            }
            return JSON.parseArray(value);
        }


        return TypeUtils.castToJavaBean(value, loadClass(className));


    }


}
