package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

public class RunInterface {

    @Getter @Setter
    public JSONObject config;

    @Getter @Setter
    public RunApplication runApplication;

}
