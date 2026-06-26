package ru.yandex.practicum.explore_with_me;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@SqlGroup({
        @Sql(
                scripts = {
                        "/sql/clear-data.sql",
                        "/sql/test-data/category-data.sql",
                        "/sql/test-data/users-data.sql",
                        "/sql/test-data/events-data.sql",
                        "/sql/test-data/participationrequest-data.sql"
                },
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
        )
})
@SpringBootTest
public abstract class BaseIntegrationTest {
}