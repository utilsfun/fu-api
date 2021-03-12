package fun.utils.java;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSON;

/**
 * @author : jialin
 * @group : THS_JAVA_PLATFORM
 * @Date : 2014-10-25 上午10:43:42
 * @Comments : 原理： 在Java SE6中最好的方法是使用StandardJavaFileManager类。
 *           这个类可以很好地控制输入、输出，并且可以通过DiagnosticListener得到诊断信息，
 *           而DiagnosticCollector类就是listener的实现。 使用StandardJavaFileManager需要两步。
 *           首先建立一个DiagnosticCollector实例以及通过JavaCompiler的getStandardFileManager()方法得到一个StandardFileManager对象。
 *           最后通过CompilationTask中的call方法编译源程序。
 * @Version : 1.0.0
 */
public class DynamicEngine {

	private static Log logger = LogFactory.getLog(DynamicEngine.class);
	
	// 单例
	private static DynamicEngine ourInstance = new DynamicEngine();

	public static DynamicEngine getInstance() {

		return ourInstance;
	}

	private static Map<String, CompiledClass> compiledClasses = Collections.synchronizedMap(new HashMap<String, CompiledClass>());

	public  String classpath;

	private DynamicEngine() {

		// 创建classpath

		this.classpath = null;

		try {

			List<URLClassLoader> ucls = new ArrayList<URLClassLoader>();

			try {
				ucls.add((URLClassLoader) this.getClass().getClassLoader());
			} catch (Exception e) {
			}
			
			try {
				ucls.add((URLClassLoader) ClassLoader.getSystemClassLoader());
			} catch (Exception e) {
			}
			

			try {
				Class<?> cls = Class.forName("javax.servlet.http.HttpServletRequest");
				ucls.add((URLClassLoader) cls.getClassLoader());

			} catch (Exception e) {
			}
			
			try {
				Class<?> cls = Class.forName("org.springframework.boot.SpringApplication");
				ucls.add((URLClassLoader) cls.getClassLoader());

			} catch (Exception e) {
			}

			try {
				Class<?> cls = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
				ucls.add((URLClassLoader) cls.getClassLoader());
			} catch (Exception e) {
			}

			List<String> urls = new ArrayList<String>();
			urls.addAll(Arrays.asList(System.getProperty("java.class.path").split(";")));
			for (URLClassLoader ucl : ucls) {
				for (URL url : ucl.getURLs()) {
					if (!urls.contains(url.getFile())) {
						urls.add(url.getFile());
					}
				}
			}

			StringBuilder sb = new StringBuilder();
			for (String url : urls) {
				if (!url.endsWith("-sources.jar")) {
					sb.append(url).append(File.pathSeparator);
				}
			}

			this.classpath = sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @MethodName : 编译java代码到Object
	 * @Description : TODO
	 * @param fullClassName
	 *          类名
	 * @param javaCode
	 *          类代码
	 * @return Object
	 * @throws Exception
	 */

	public synchronized CompiledClass javaCodeToClass(String fullClassName, String javaCode) throws Exception {

		CompiledClass cclass = compiledClasses.get(fullClassName);
		if (cclass == null) {
			cclass = new CompiledClass();
		}

		if (cclass.clazz == null || !javaCode.equals(cclass.code)) {

			long start = System.currentTimeMillis(); // 记录开始编译时间

			// 获取系统编译器
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			// 建立DiagnosticCollector对象
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

			// 建立用于保存被编译文件名的对象
			// 每个文件被保存在一个从JavaFileObject继承的类中
			ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(diagnostics, null, null));

			List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
			jfiles.add(new CharSequenceJavaFileObject(fullClassName, javaCode));

			// 使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
			List<String> options = new ArrayList<String>();
			options.add("-encoding");
			options.add("UTF-8");
			options.add("-classpath");
			options.add(this.classpath);

			logger.debug(JSON.toJSON(options));
			JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);

			logger.debug(javaCode);

			// 编译源程序
			boolean success = task.call();
			long end = System.currentTimeMillis();

			if (success) {
				// 如果编译成功，用类加载器加载该类
				JavaClassObject jco = fileManager.getJavaClassObject();
				// DynamicClassLoader dcLoader = new
				// DynamicClassLoader(this.parentClassLoader);
				DynamicClassLoader dcLoader = new DynamicClassLoader(this.getClass().getClassLoader());
				cclass.fullname = fullClassName;
				cclass.compiletime = end - start;
				cclass.loadtime = System.currentTimeMillis();
				cclass.clazz = dcLoader.loadClass(fullClassName, jco);
				cclass.code = javaCode;
				dcLoader.close();

				compiledClasses.put(fullClassName, cclass);
				logger.debug("javaCodeToObject use:" + (end - start) + "ms");

			} else {
				// 如果想得到具体的编译错误，可以对Diagnostics进行扫描
				String error = "";
				for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
					error = error + compilePrint(diagnostic);
				}
				logger.warn(javaCode);
				throw new Exception(error);
			}
		}

		return cclass;
	}

	/**
	 * @MethodName : compilePrint
	 * @Description : 输出编译错误信息
	 * @param diagnostic
	 * @return
	 */
	private String compilePrint(Diagnostic<?> diagnostic) {

		StringBuffer res = new StringBuffer();
		res.append("Code:[" + diagnostic.getCode() + "]\n");
		res.append("Kind:[" + diagnostic.getKind() + "]\n");
		res.append("Position:[" + diagnostic.getPosition() + "]\n");
		res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
		res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
		res.append("Source:[" + diagnostic.getSource() + "]\n");
		res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
		res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
		res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");

		String result = res.toString();
		logger.debug(result);
		return result;
	}
}