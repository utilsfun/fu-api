package fun.utils.api.core.exception;

import java.util.LinkedList;
import java.util.List;

public class ApiException extends Exception {

	private static final long serialVersionUID = -3683450436146794017L;

	public int code = 500;

	public Object detail ;

	public ApiException(int code, String message) {
		super(message);
		this.code = code;
	}

	public ApiException(int code, String message, Object detail){
		super(message);
		this.code = code;
		this.detail = detail;
	}




	public static ApiException resultNullException(){
		return new ApiException(401, String.format("返回值为空"));
	}

	public static ApiException unknownException(Throwable e){
		List<String> detail = new LinkedList<>();
		for ( StackTraceElement stackTraceElement:e.getStackTrace()) {
			detail.add(stackTraceElement.toString());
		}
		return new ApiException(500, String.format(e.getMessage()), detail);
	}

	public static ApiException parameterRequiredException(String parameterName){
		return new ApiException(501, String.format("参数%s必填", parameterName));
	}

	public static ApiException parameterTypeException(String parameterName,String parameterType){
		return new ApiException(502, String.format("参数%s数据类型(%s)不匹配", parameterName,parameterType));
	}

	public static ApiException parameterValidException(String message){
		return new ApiException(503, message);
	}

	public static ApiException resourceNotFondException(String resource){
		return new ApiException(510, String.format("资源%s不存在", resource));
	}

}