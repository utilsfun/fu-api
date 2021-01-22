package fun.utils.api.core.persistence;

import com.alibaba.fastjson.JSONObject;
import fun.utils.api.core.runtime.ErrorCode;
import lombok.Data;

import java.util.Date;
import java.util.List;

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
     * 【配置】 json
     */
    private JSONObject config;

    /**
     * 【错误码】
     */
    private List<ErrorCode> errorCodes;
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


    private List<Long> documentIds;
    private List<Long> parameterIds;
    private List<Long> filterIds;
    private List<String> interfaceNames;

}