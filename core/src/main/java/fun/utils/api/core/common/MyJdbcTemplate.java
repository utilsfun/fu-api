package fun.utils.api.core.common;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public class MyJdbcTemplate extends JdbcTemplate {

    public MyJdbcTemplate(DataSource dataSource) {
        super(dataSource);

    }

}
