package fun.utils.api.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class IndexController {

    @GetMapping("/demo/{path1}.html")
    public String html(@PathVariable String path1){
        return "index";
    }


    @RequestMapping("/demo/{path1}.page")
    @ResponseBody
    public JSONObject page(@PathVariable String path1) throws IOException {

        JSONObject result = new JSONObject();
        result.put("status",0);
        result.put("msg",0);

        JSONObject data = JSON.parseObject(this.getClass().getResourceAsStream("/static/demo/"+path1+".json"),JSONObject.class);

        JSON aside = JSON.parseObject(this.getClass().getResourceAsStream("/static/demo/common/aside.json"),JSON.class);
        data.put("aside",aside);

        result.put("data",data);
        return result;
    }


}
