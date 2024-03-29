import org.apache.commons.io.IOUtils
import org.redisson.api.RMap
import org.redisson.api.RedissonClient
import org.springframework.web.context.WebApplicationContext

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

Map<String,Object> context = $context;

HttpServletRequest request = context.get("request") ;
HttpServletResponse response = context.get("response") ;
HttpSession session = context.get("session") ;
WebApplicationContext webApplicationContext = context.get("webApplicationContext") ;

RedissonClient redissonClient = webApplicationContext.getBean("fu-api.redisson-client");

String md5 = request.getParameter("id");
String mapKey = "image:md5:" + md5;
RMap imageMap = redissonClient.getMap(mapKey);


redissonClient.getMapCache()

if (imageMap == null || !imageMap.isExists() || imageMap.isEmpty() ){
    response.sendError(404,mapKey);
}else{
    byte[] contentBytes = (byte[]) imageMap.get("content");
    String contentType =  (String) imageMap.get("type");
    Long size = (Long) imageMap.get("size");
    response.setContentType(contentType);
    response.setContentLengthLong(size);
    IOUtils.write(contentBytes,response.getOutputStream());
}


