package fun.utils.api.core.controller;


import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ImageController {


    @Resource(name = "fu-api.redisson-client")
    @Getter
    private RedissonClient redissonClient;

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public Object upload(@RequestParam("file") MultipartFile multipartFile , HttpServletRequest request) throws IOException {

        byte[] contentBytes = multipartFile.getBytes();
        String contentType = multipartFile.getContentType();
        String fileName = multipartFile.getOriginalFilename();
        String md5 = DigestUtils.md5DigestAsHex(contentBytes);
        String base64 = Base64Utils.encodeToString(contentBytes);
        String mapKey = "image:md5:" + md5;
        RMap imageMap = redissonClient.getMap(mapKey);
        imageMap.expire(1, TimeUnit.HOURS);

        imageMap.put("content", contentBytes);
        imageMap.put("type", contentType);
        imageMap.put("name", multipartFile.getName());
        imageMap.put("md5", md5);
        imageMap.put("filename", fileName);
        imageMap.put("size", multipartFile.getSize());

        String baseUrl = StringUtils.substringBeforeLast(request.getRequestURL().toString(),"/upload");
        String downloadUrl = baseUrl + "/download/" + md5;

        String value = "data:" + contentType + ";base64," + base64;

        String key = "image:filename:" + fileName;
        RBucket bucket = redissonClient.getBucket(key);
        bucket.expire(1, TimeUnit.HOURS);
        bucket.set(md5);

        JSONObject result = new JSONObject();
        result.put("status", 0);
        result.put("msg", "success");
        result.put("data", new JSONObject());
        result.getJSONObject("data").put("value", value);
        result.getJSONObject("data").put("md5", md5);
        result.getJSONObject("data").put("url", downloadUrl);
        result.getJSONObject("data").put("filename", fileName);
        return result;

    }

    @RequestMapping(value = "/download/{md5}",method = RequestMethod.POST)
    public void get(@PathVariable("md5") String md5 , HttpServletResponse response) throws IOException {

        String mapKey = "image:md5:" + md5;
        RMap imageMap = redissonClient.getMap(mapKey);

        byte[] contentBytes = (byte[]) imageMap.get("content");
        String contentType =  (String) imageMap.get("type");
        Long size = (Long) imageMap.get("size");
        response.setContentType(contentType);
        response.setContentLengthLong(size);
        IOUtils.write(contentBytes,response.getOutputStream());

    }

}