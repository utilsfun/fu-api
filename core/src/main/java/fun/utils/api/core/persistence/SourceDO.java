package fun.utils.api.core.persistence;

import lombok.Data;

import java.util.Date;

/**
 * Description:接口方法表
 */

@Data
public class SourceDO {

    /**
     * 【编号】自增长编号
     */
    private long id;

    /**
     * 【应用Id】
     */
    private long applicationId;

    /**
     * 【名称】 应用 + 类型 + 名称 唯一
     */
    private String name;

    /**
     * 【类型】database,redis
     */
    private String type;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【配置】 json,yaml
     */
    private String config;


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
