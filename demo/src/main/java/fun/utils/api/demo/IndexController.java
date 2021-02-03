package fun.utils.api.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class IndexController {

    @GetMapping("/demo/{path1}.html")
    public String html(@PathVariable String path1) {
        return "index";
    }


    @RequestMapping("/demo/{path1}.page")
    @ResponseBody
    public JSONObject page(@PathVariable String path1) throws IOException {

        JSONObject result = new JSONObject();
        result.put("status", 0);
        result.put("msg", 0);

        JSONObject page = JSON.parseObject(this.getClass().getResourceAsStream("/demo/" + path1 + ".json"), JSONObject.class);

        JSON aside = JSON.parseObject(this.getClass().getResourceAsStream("/demo/common/aside.json"), JSON.class);
        page.put("aside", aside);

        JSON data = JSON.parseObject(this.getClass().getResourceAsStream("/demo/common/data.json"), JSON.class);
        page.put("data", data);


        result.put("data", page);
        return result;
    }


}
