package fun.utils.api.core.runtime;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ApiRunner {

    @Setter @Getter
    RunContext runContext;

    public void doInitialize() throws Exception{

        JSONObject input = new JSONObject();

        if (StrUtils.isEBlank(method)) {
            input = RpcTools.getJsonByInput(http_request);
        } else {
            JSONObject params = RpcTools.getJsonByParameters(http_request);
            input.put("method", method);
            input.put("params", params);
        }

        //打包参数到 parameters 对象

    }

    public void onEnter() throws Exception{

        //运行 application onEnter过滤器
        //运行 interface onEnter过滤器

    }

    public void doValidate() throws Exception {

        //运行 参数验证

    }

    public void onExecute() throws Exception {

        //运行 application onExecute
        //运行 interface onExecute
    }

    public void execute() throws Exception{

        //运行 interface 方法

    }

    public void onReturn() throws Exception {

        //运行 interface onReturn
        //运行 application onReturn

    }

    public void run() throws Exception {

        doInitialize();

        onEnter();

        doValidate();

        onExecute();

        execute();

        onReturn();

    }



}
