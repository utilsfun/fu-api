package fun.utils.api.core.common.groovy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.lang3.StringUtils;

import java.util.UnknownFormatConversionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ScriptRunner {

    private static ConcurrentMap<String,Class<?>> classMappings = new ConcurrentHashMap<String,Class<?>>();
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
            if(clazz == null){
                continue;
            }
            classMappings.put(clazz.getSimpleName(), clazz);
        }

    }

    public static Class<?> loadClass(String className){
        Class<?> clazz = classMappings.get(className);
        if(clazz != null){
            return clazz;
        }
        return TypeUtils.loadClass(className);
    }

    public static Object execute(ScriptMethod scriptMethod, Object myContext, JSONObject parameters) throws Exception{

        GroovyShell shell = new GroovyShell();


        Script runner = shell.parse(scriptMethod.script);
        Binding binding = new Binding();

        binding.setVariable("my", myContext);

        String returnType = StringUtils.defaultIfBlank(scriptMethod.getReturnType(),"String");
        Class<?> returnClass = loadClass(returnType);
        if (returnClass == null){
            throw new UnknownFormatConversionException("返回类型'"+ returnType +"'不可识别");
        }

        scriptMethod.parameters.forEach((name,parameter)->{

            Object parameterValue;
            Object sourceValue = null;

            String dataType = StringUtils.defaultIfBlank(parameter.getDataType(),"String");;

            Class dataClass  = loadClass(dataType);

            if (dataClass == null){
                throw new UnknownFormatConversionException("参数类型'"+ dataType +"'不可识别");
            }

            if (parameters != null && parameters.containsKey(name)){

                sourceValue = parameters.get(name);

            }else{

                if (StringUtils.isNotBlank(parameter.defaultValue)){

                    sourceValue = parameter.defaultValue;
                }
            }

            parameterValue = TypeUtils.castToJavaBean(sourceValue,dataClass);

            if (sourceValue == null && parameter.isRequired()) {

                throw new NullPointerException("参数'" + name + "'必填");

            }else{
                binding.setVariable(name, parameterValue);
            }

        });


        long bTime = System.nanoTime();

        Object result = runner.run();

        Object resultValue = TypeUtils.castToJavaBean(result,returnClass);

        return resultValue;

    }

}
