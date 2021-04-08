import com.alibaba.fastjson.JSONObject
import fun.utils.common.DataUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.redisson.api.RBucket
import org.redisson.api.RMap
import org.redisson.api.RedissonClient
import org.springframework.util.Base64Utils
import org.springframework.util.DigestUtils
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit


Map<String,Object> context = $context;

HttpServletRequest request = context.get("request") ;
HttpServletResponse response = context.get("response") ;
HttpSession session = context.get("session") ;
WebApplicationContext webApplicationContext = context.get("webApplicationContext") ;

RedissonClient redissonClient = webApplicationContext.getBean("fu-api.redisson-client");


//MultipartResolver resolver = new CommonsMultipartResolver(application);
//MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);

// 转换request，解析出request中的文件
if (request instanceof MultipartHttpServletRequest) {

    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

// 获取文件map集合
    Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();

    MultipartFile multipartFile = fileMap.get("file");

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

    String baseUrl = StringUtils.substringBeforeLast(request.getRequestURL().toString(), "/upload.gsp");
    String downloadUrl = baseUrl + "/download.gsp?id=" + md5;

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

    //返回内容格式化为 application/json
    byte[] returnBytes = DataUtils.toWebJSONString(result).getBytes(StandardCharsets.UTF_8);
    response.setContentType("application/json; charset=utf-8");

    //写入返回流
    IOUtils.write(returnBytes, response.getOutputStream());

}else{
    response.sendError(500,"fail request data");
}
