package com.my.proj.resource;

import static com.my.proj.resource.TestAuth.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/** GET /firms success path, and the race on the unique firm code. */
@QuarkusTest
class FirmListTest {

    private static String code() {
        return "L" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }

    private static int create(String code) {
        return given().contentType(ContentType.JSON).body(Map.of("name", "Firm " + code, "code", code))
            .when().post("/firms").then().statusCode(201).extract().path("id");
    }

    @Test
    void listReturnsActiveAndInactiveFirms() {
        String active = code();
        String inactive = code();
        create(active);
        int id = create(inactive);
        given().when().delete("/firms/{id}", id).then().statusCode(204);

        given().when().get("/firms").then().statusCode(200)
            .body("find { it.code == '" + active + "' }.status", is("ACTIVE"))
            .body("find { it.code == '" + inactive + "' }.status", is("INACTIVE"))
            .body("code", hasItem(active));
    }

    @Test
    void concurrentCreatesWithTheSameCodeGiveOneCreatedAndTheRest409() throws Exception {
        String code = code();
        int n = 6;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Callable<Integer> call = () -> {
                go.await();
                return given().contentType(ContentType.JSON).body(Map.of("name", "Race", "code", code))
                    .when().post("/firms").then().extract().statusCode();
            };
            results.add(pool.submit(call));
        }
        go.countDown();
        int created = 0;
        for (Future<Integer> f : results) {
            int status = f.get();
            if (status == 201) {
                created++;
            } else {
                assertEquals(409, status, "a losing request is a 409, never a 500");
            }
        }
        pool.shutdown();
        assertEquals(1, created);
    }
}
