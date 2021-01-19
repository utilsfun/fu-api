package fun.utils.api.core.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;


public class LdxResult extends JSONObject {

	private static final long serialVersionUID = -3307140375714924531L;

	public static LdxResult genResult(String id, Object retObj) {

		if (retObj instanceof LdxResult) {
			return (LdxResult) retObj;
		}

		LdxResult result = new LdxResult();

		if (StringUtils.isNotBlank(id)) {
			result.put("id", id);
		}

		if (retObj == null) {

			JSONObject err = new JSONObject();
			result.put("error", err);
			err.put("code", -32603);
			err.put("message", "result is null");

		} else if (retObj instanceof RpcException) {

			RpcException re = (RpcException) retObj;
			JSONObject err = new JSONObject();
			result.put("error", err);
			err.put("code", re.code);
			err.put("message", re.getMessage());

		} else if (retObj instanceof Exception) {

			JSONObject err = new JSONObject();
			result.put("error", err);
			err.put("code", -32603);
			err.put("message", ((Exception) retObj).toString());

		} else {
			//
			result.put("result", JSON.toJSON(retObj));
		}

		return result;
	}

}
