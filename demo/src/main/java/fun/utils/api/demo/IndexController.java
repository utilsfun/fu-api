package fun.utils.api.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
