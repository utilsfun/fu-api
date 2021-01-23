package fun.utils.api.core.exception;

public class ApiException extends Exception {

	private static final long serialVersionUID = -3683450436146794017L;

	public int code = 500;

	public String detail = "";

	public ApiException(int code, String message) {
		super(message);
		this.code = code;
	}

	public ApiException(int code, String message, String detail){
		super(message);
		this.code = code;
		this.detail = detail;
	}


	public static ApiException parameterRequiredException(String parameterName){
		return new ApiException(501, String.format("参数%s必填", parameterName));
	}

	public static ApiException parameterTypeException(String parameterName,String parameterType){
		return new ApiException(502, String.format("参数%s数据类型(%s)不匹配", parameterName,parameterType));
	}

}