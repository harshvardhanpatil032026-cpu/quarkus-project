package com.my.proj.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class Demo2AcceptanceTest2 {
    @Test
    void testDatabaseConnectionFailure() throws SQLException {
        DataSource mockDataSource = Mockito.mock(DataSource.class);
        Connection mockConnection = Mockito.mock(Connection.class);
        Mockito.when(mockDataSource.getConnection()).thenThrow(new SQLException("Mocked connection failure"));
        DatabaseResource resource = new DatabaseResource();
        resource.dataSource = mockDataSource;

        given()
          .when().get("/db")
          .then()
             .statusCode(500)
             .body(is("{\"message\":\"Unexpected error\"}"));
    }
}
