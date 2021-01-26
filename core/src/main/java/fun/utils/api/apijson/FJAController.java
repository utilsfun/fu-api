package fun.utils.api.apijson;

import apijson.framework.APIJSONController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apijson.RequestMethod;
import apijson.StringUtil;
import apijson.framework.APIJSONController;
import apijson.orm.Parser;

import javax.servlet.http.HttpSession;
import java.net.URLDecoder;

public class FJAController extends APIJSONController {

    @Override
    public Parser<Long> newParser(HttpSession session, RequestMethod method) {
        //TODO 这里关闭校验，方便新手快速测试，实际线上项目建议开启
        return super.newParser(session, method).setNeedVerify(false);
    }

    @PostMapping(value = "get")
    @Override
    public String get(@RequestBody String request, HttpSession session) {
        return super.get(request, session);
    }

    /**获取
     * 只为兼容HTTP GET请求，推荐用HTTP POST，可删除
     * @param request 只用String，避免encode后未decode
     * @param session
     * @return
     * @see {@link RequestMethod#GET}
     */
    @RequestMapping("get/{request}")
    public String openGet(@PathVariable String request, HttpSession session) {
        try {
            request = URLDecoder.decode(request, StringUtil.UTF_8);
        } catch (Exception e) {
            // Parser会报错
        }
        return get(request, session);
    }
}
