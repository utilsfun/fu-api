package fun.utils.api.core.persistence;

import lombok.Data;

import java.util.Date;

/**
 * Description:【接口文档表】
 */
@Data
public class DocumentDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【上级类型】application,interface
     */
    private String parentType;

    /**
     * 【上级ID】
     */
    private long parentId;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【内容格式】text,java,js,markdown,html,json,xml,...
     */
    private String format;

    /**
     * 【内容体】
     */
    private String content;

    /**
     * 【权限】公开（public），保护（protected），私有（private）
     */
    private String permission;

    /**
     * 【状态】0正常 1停用
     */
    private int status;

    /**
     * 【创建时间】
     */
    private Date gmtCreate;

    /**
     * 【修改时间】
     */
    private Date gmtModified;
}