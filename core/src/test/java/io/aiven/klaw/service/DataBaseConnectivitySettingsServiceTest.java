package io.aiven.klaw.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataBaseConnectivitySettingsServiceTest {

    @Test
    public void updateConnectionStringSuccessfully() {
        DataBaseConnectivitySettingsService.performConnectionStringUpdate("jdbc:mysql://localhost:3306/test_db");
        assertEquals("jdbc:mysql://localhost:3306/test_db",
                DataBaseConnectivitySettingsService.obtainActualConnectionString());
    }

    @Test
    public void throwExceptionWhenConnectionStringIsInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DataBaseConnectivitySettingsService.performConnectionStringUpdate("");
        });
        assertEquals("Provided connection string is invalid!", exception.getMessage());
    }

    @Test
    public void resetConnectionStringToDefault() {
        DataBaseConnectivitySettingsService.performConnectionStringUpdate("jdbc:mysql://localhost:3306/another_db");

        DataBaseConnectivitySettingsService.resetConnectionStringToDefault();

        assertTrue(DataBaseConnectivitySettingsService.isUsingDefaultConnectionString());
    }
}
