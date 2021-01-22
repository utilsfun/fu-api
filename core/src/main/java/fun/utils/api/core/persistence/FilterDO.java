package fun.utils.api.core.persistence;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.Date;
/**
 * Description:【接口过滤器表】
 */

@Data
public class FilterDO {

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
     * 【配置】 json
     */
    private JSONObject config;

    /**
     * 【排序】
     */
    private int sort;

    /**
     * 【实现类型】groovy/bean,...
     */
    private String implementType;

    /**
     * 【实现代码】
     */
    private String implementCode;

    /**
     * 【过滤位置】enter,execute,return
     */
    private String point;

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