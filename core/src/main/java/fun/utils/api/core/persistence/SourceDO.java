package fun.utils.api.core.persistence;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
     * 【名称】 应用 + 名称 唯一
     */
    private String name;

    /**
     * 【类型】mysql,redis
     */
    private String type;

    /**
     * 【说明】
     */
    private String note;

    /**
     * 【配置】 json
     */
    private JSONObject config;


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
