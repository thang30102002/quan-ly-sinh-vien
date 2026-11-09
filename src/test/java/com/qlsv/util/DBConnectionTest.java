package com.qlsv.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

class DBConnectionTest {

    @Test
    void getConnection_ketNoiDuocToiDatabaseQlsv() throws Exception {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT current_database(), 1")) {
            assertTrue(conn.isValid(5));
            assertTrue(rs.next());
            assertTrue(rs.getString(1).startsWith("qlsv"));
            assertEquals(1, rs.getInt(2));
        }
    }
}
