package com.automation.utilities.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public final class ApiUtil {

    private ApiUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /* =========================
       BASIC CONFIG
       ========================= */

    public static void setBaseUri(String baseUri) {
        RestAssured.baseURI = baseUri;
    }


    /* =========================
       GET
       ========================= */

    public static Response get(String endpoint) {

        return RestAssured
                .given()
                .log().ifValidationFails()
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }


    public static Response get(String endpoint,
                               Map<String, String> headers) {

        return RestAssured
                .given()
                .headers(headers)
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }


    /* =========================
       POST
       ========================= */

    public static Response post(String endpoint,
                                Object body) {

        return RestAssured
                .given()
                .contentType("application/json")
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }


    public static Response post(String endpoint,
                                Map<String, String> headers,
                                Object body) {

        return RestAssured
                .given()
                .headers(headers)
                .contentType("application/json")
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }


    /* =========================
       PUT
       ========================= */

    public static Response put(String endpoint,
                               Object body) {

        return RestAssured
                .given()
                .contentType("application/json")
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }


    /* =========================
       DELETE
       ========================= */

    public static Response delete(String endpoint) {

        return RestAssured
                .given()
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }


    /* =========================
       COMMON VALIDATIONS
       ========================= */

    public static void validateStatus(Response response,
                                      int expectedStatus) {

        if (response.getStatusCode() != expectedStatus) {

            throw new RuntimeException(
                    "Expected status: "
                            + expectedStatus
                            + " but got: "
                            + response.getStatusCode());
        }
    }
}
