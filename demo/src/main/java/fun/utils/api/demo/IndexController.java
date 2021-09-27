package fun.utils.api.demo;


import com.alibaba.fastjson.JSONObject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class IndexController {

    @RequestMapping("/helloworld.do")
    public Object helloworld(@RequestParam() @Nullable String user)  {
        JSONObject result = new JSONObject();
        result.put("code",200);
        result.put("message","success");
        result.put("data","helloworld: " + user);
        return result;
    }


}
