package fun.utils.api.core.runtime;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunInterface {

    @Getter @Setter
    public String name;

    @Getter @Setter
    public JSONObject config;

    @Getter @Setter
    public RunApplication runApplication;

}
