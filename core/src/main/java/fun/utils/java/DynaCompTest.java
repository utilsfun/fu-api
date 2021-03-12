package fun.utils.java;

public class DynaCompTest
{
    public static void main(String[] args) throws Exception {
        String fullName = "DynaClass";
        StringBuilder src = new StringBuilder();
        src.append("import  com.alibaba.fastjson.*;\n");
        src.append("import java.util.Date;\n");
        src.append("public class DynaClass {\n");
        src.append("    public String toString() {\n");
        src.append("     JSONObject ret = new JSONObject();\n");
        src.append("    ret.put(\"name\",123);\n");
        src.append("        return  \"Hello, I am \" + ");
        src.append("this.getClass().getSimpleName() + ret;\n");
        src.append("    }\n");
        src.append("}\n");
 
        System.out.println(src);
        DynamicEngine de = DynamicEngine.getInstance();

        String javasrc = src.toString();
       
        Class<?> cls =  de.javaCodeToClass(fullName,javasrc).clazz;
        System.out.println(cls.newInstance());
        
        cls =  de.javaCodeToClass(fullName,javasrc).clazz;
        
        System.out.println(cls.newInstance());
        javasrc = javasrc.replace("I am", "I was"); 

        cls =  de.javaCodeToClass(fullName,javasrc).clazz;
        System.out.println(cls.newInstance());
        
        System.out.println(de.classpath.replaceAll(";", "\r\n"));

        
    }
}
