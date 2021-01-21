package fun.utils.api.core.persistence;

import lombok.Data;

import java.util.Date;

/**
 * Description:接口应用表
 */
@Data
public class ApplicationDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【名称】唯一
     */
    private String name;

    /**
     * 【标题】
     */
    private String title;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【作者】
     */
    private String owner;

    /**
     * 【版本号】
     */
    private String version;

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
