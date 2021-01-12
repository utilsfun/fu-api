package fun.utils.api.core.common.po;

import lombok.Data;

@Data
public class DocumentPO {

    public String id; //文档ID

    public String parentType; //上级类型 application,interface
    public String parentId; //上级ID

    public String title; //标题,仅用于接口文档

    public String note; // 说明,仅用于接口文档

    public String format;  //内容格式 java,js,markdown,html,json,xml,...

    public String content;  //内容体

    public String permission  ; //权限 公开（public），保护（protected），私有（private）

}
