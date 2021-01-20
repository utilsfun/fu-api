package fun.utils.api.core.persistence;

import java.util.Date;

/**
 * Description:接口应用表
 */

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

    /**
     * setter for column 【编号】自增长编号
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * getter for column 【编号】自增长编号
     */
    public long getId() {
        return this.id;
    }

    /**
     * setter for column 【名称】唯一
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter for column 【名称】唯一
     */
    public String getName() {
        return this.name;
    }

    /**
     * setter for column 【标题】
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * getter for column 【标题】
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * setter for column 【说明】
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * getter for column 【说明】
     */
    public String getNote() {
        return this.note;
    }

    /**
     * setter for column 【作者】
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * getter for column 【作者】
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * setter for column 【版本号】
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * getter for column 【版本号】
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * setter for column 【状态】0正常 1停用
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * getter for column 【状态】0正常 1停用
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * setter for column 【创建时间】
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * getter for column 【创建时间】
     */
    public Date getGmtCreate() {
        return this.gmtCreate;
    }

    /**
     * setter for column 【修改时间】
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * getter for column 【修改时间】
     */
    public Date getGmtModified() {
        return this.gmtModified;
    }
}
