package com.homecloud.planner;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlywayMigrationIT {

    @Autowired
    private DataSource dataSource;

    @Test
    void flywayAppliesBaselineMigrationAgainstRealPostgres() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        Integer historyCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM flyway_schema_history WHERE script = 'V1__baseline.sql' AND success = true",
                Integer.class);
        assertThat(historyCount).isEqualTo(1);

        Integer baselineRowCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM schema_baseline", Integer.class);
        assertThat(baselineRowCount).isEqualTo(1);
    }
}
