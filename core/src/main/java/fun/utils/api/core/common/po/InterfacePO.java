package fun.utils.api.core.common.po;


import fun.utils.api.core.vo.ErrorCode;
import lombok.Data;

import java.util.List;

@Data
public class InterfacePO {

    public String id; //接口ID

    public String appId; //应用ID

    public String name; //名称

    public String group; //分组名称

    public int sort;// 排序 ,仅用于接口文档

    public String title; //标题,仅用于接口文档

    public String note; // 说明,仅用于接口文档

    //接口地址 默认 ${interface.name}.japi ;
    //实际地址Url为 http(s)://{服务器}:{端口}/{基础路径}/{应用名称}/{接口uri}
    public String uri;

    public String httpMethod; //http 方法 get/put/post/delete

    public String requestExample;    //调用示例 支持mock变量
    public String responseExample; //返回示例 支持mock变量


    public String implementType; //实现类型 sql/url/groovy/bean/javascript
    public String implementCode; //实现代码

    public List<ErrorCode> errorCodes; //错误码

}
