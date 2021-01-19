package fun.utils.api.core.common;

public class RpcException extends Exception {

	private static final long serialVersionUID = -3683450436146794017L;

	public int code = 500;

	public String detail = "";

	public RpcException(int code, String message) {
		super(message);
		this.code = code;
	}

	public RpcException(int code, String message, String detail){
		super(message);
		this.code = code;
		this.detail = detail;
	}

}