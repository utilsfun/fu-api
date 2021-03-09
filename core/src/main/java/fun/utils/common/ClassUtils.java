package fun.utils.common;

import com.alibaba.fastjson.util.TypeUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fun.utils.api.core.script.GroovyRunner;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;


public class ClassUtils {

    private final static ConcurrentMap<String, Class<?>> classMappings = new ConcurrentHashMap<>();
    private final static Cache<String, GroovyRunner> cacheRunner = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(2, TimeUnit.MINUTES).build();

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

    public static <T> T castValue(Object value,  Class<T> cls) {
        return TypeUtils.castToJavaBean(value,cls);
    }

    public static Object castValue(Object value, String className) {
        return TypeUtils.castToJavaBean(value,loadClass(className));
    }



}
