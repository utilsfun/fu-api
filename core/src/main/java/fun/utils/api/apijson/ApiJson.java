package fun.utils.api.apijson;

import apijson.Log;
import apijson.RequestMethod;
import apijson.framework.APIJSONCreator;
import apijson.framework.APIJSONSQLConfig;
import apijson.framework.APIJSONSQLExecutor;
import apijson.orm.SQLConfig;
import apijson.orm.SQLExecutor;
import com.alibaba.fastjson.JSONObject;
import fun.utils.api.apijson.ApiJsonParser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ApiJson {

    private final DataSource dataSource;
    private final APIJSONCreator creator ;

    private final String dbName;
    private final String dbVersion;
    private final String dbSchema;

    public ApiJson(DataSource dataSource) throws SQLException {

        this.dataSource = dataSource;
        this.dbName = "MYSQL";
        this.dbVersion = "8.0.11";
        this.dbSchema = null;

//        try (Connection connection = dataSource.getConnection()){
//            this.dbName = connection.getMetaData().getDatabaseProductName();
//            this.dbVersion = connection.getMetaData().getDatabaseProductVersion();
//            this.dbSchema = connection.getSchema();
//        }

        this.creator = new APIJSONCreator() {
            @Override
            public SQLConfig createSQLConfig() {
                APIJSONSQLConfig sqlConfig = new APIJSONSQLConfig(){
                    @Override
                    public String getSQLDatabase() {
                       return dbName;
                    }
                    @Override
                    public String getSQLSchema() {
                        return dbSchema;
                    }
                    @Override
                    public String getDBVersion() {
                        return dbVersion;
                    }

                };

                return sqlConfig;
            }

            public SQLExecutor createSQLExecutor() {
                return new APIJSONSQLExecutor(){

                    //  可重写以下方法，支持 Redis 等单机全局缓存或分布式缓存
                    //	@Override
                    //	public List<JSONObject> getCache(String sql, int type) {
                    //		return super.getCache(sql, type);
                    //	}
                    //	@Override
                    //	public synchronized void putCache(String sql, List<JSONObject> list, int type) {
                    //		super.putCache(sql, list, type);
                    //	}
                    //	@Override
                    //	public synchronized void removeCache(String sql, int type) {
                    //		super.removeCache(sql, type);
                    //	}

                    // 适配连接池，如果这里能拿到连接池的有效 Connection，则 SQLConfig 不需要配置 dbVersion, dbUri, dbAccount, dbPassword
                    @Override
                    public Connection getConnection(SQLConfig config) throws Exception {
                        Connection connection = connectionMap.get(config.getDatabase());
                        if (connection == null || connection.isClosed()) {
                            try {
                                // 另一种方式是 DruidConfig 初始化获取到 Datasource 后给静态变量 DATA_SOURCE 赋值： ds = DruidConfig.DATA_SOURCE.getConnection();
                                connection = dataSource.getConnection();
                                connectionMap.put(config.getDatabase(),connection);
                            } catch (Exception e) {
                                Log.e(TAG, "SQLExecutor getConnection " + e.getMessage());
                            }
                        }
                        // 必须最后执行 super 方法，因为里面还有事务相关处理。
                        // 如果这里是 return c，则会导致 增删改 多个对象时只有第一个会 commit，即只有第一个对象成功插入数据库表
                        return super.getConnection(config);
                    }
                };
            }
        };
    }

    public JSONObject get(JSONObject request){
        return new ApiJsonParser(RequestMethod.GET,false,creator).parseResponse(request);
    }

    public JSONObject post(JSONObject request){
        return new ApiJsonParser(RequestMethod.POST,false,creator).parseResponse(request);
    }

    public JSONObject delete(JSONObject request){
        return new ApiJsonParser(RequestMethod.DELETE,false,creator).parseResponse(request);
    }

    public JSONObject put(JSONObject request){
        return new ApiJsonParser(RequestMethod.PUT,false,creator).parseResponse(request);
    }

}
