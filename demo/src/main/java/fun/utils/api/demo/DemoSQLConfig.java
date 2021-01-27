package fun.utils.api.demo;

/*Copyright ©2016 TommyLemon(https://github.com/TommyLemon/APIJSON)
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/


import apijson.RequestMethod;
import apijson.orm.SQLConfig;
import com.alibaba.fastjson.annotation.JSONField;

import apijson.framework.APIJSONSQLConfig;


/**SQL配置
 * TiDB 用法和 MySQL 一致
 * @author Lemon
 */
public class DemoSQLConfig extends APIJSONSQLConfig {

    @Override
    public String getSQLDatabase() {

        return "MYSQL";
    }

    @Override
    public String getSQLSchema() {
        return null;
       // return "fuapi";
    }

    @Override
    public String getDBVersion() {
        return "5.7.22";  // "8.0.11";  // TODO 改成你自己的 MySQL 或 PostgreSQL 数据库版本号  // MYSQL 8 和 7 使用的 JDBC 配置不一样
    }

    // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
    @JSONField(serialize = false)
    @Override
    public String getDBPassword() {
        return "db@@2020";
    }

    // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
    @JSONField(serialize = false)
    @Override
    public String getDBAccount() {
        return "db_root";
    }

    // 不在日志打印 账号/密码 等敏感信息，用了 UnitAuto 则一定要加
    @JSONField(serialize = false)
    @Override
    public String getDBUri() {
        return "jdbc:mysql://rm-wz9c07bgnhh23pjm4ko.mysql.rds.aliyuncs.com/fuapi?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=GMT%2B8&useSSL=false&allowMultiQueries=true";
    }

    //如果确定只用一种数据库，可以重写方法，这种数据库直接 return true，其它数据库直接 return false，来减少判断，提高性能
    @Override
    public boolean isMySQL() {
        return true;
    }




}