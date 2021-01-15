package fun.utils.api.core.common.po;

import fun.utils.api.core.vo.ErrorCode;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ApplicationPO {

    public String id; //ID

    public String name; //名称

    public String title; //标题,仅用于接口文档

    public String note; // 说明,仅用于接口文档

    public String version;  //版本号

    public String owner; // 作者;

    public Date updateTime; // 更新时间;
    public List<String> updateLog; // 更新日志;

    public List<ErrorCode> errorCodes; //公共错误码

}
