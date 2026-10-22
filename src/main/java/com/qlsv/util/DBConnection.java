package com.qlsv.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Mở kết nối JDBC tới CSDL qlsv.
 *
 * <p>Mỗi thông số lấy từ biến môi trường trước (DB_URL, DB_USER, DB_PASSWORD — service
 * {@code maven} trong Docker truyền sẵn), không có thì đọc từ {@code db.properties} trên classpath.
 * Người gọi phải tự đóng {@link Connection}, nên dùng try-with-resources.
 */
public final class DBConnection {

    private static final String PROPERTIES_FILE = "db.properties";

    private static Properties properties;

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = get("DB_URL", "db.url");
        String user = get("DB_USER", "db.user");
        String password = get("DB_PASSWORD", "db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static String get(String envName, String propertyKey) {
        String value = System.getenv(envName);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        value = loadProperties().getProperty(propertyKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình kết nối CSDL: đặt biến môi trường "
                    + envName + " hoặc khóa " + propertyKey + " trong " + PROPERTIES_FILE);
        }
        return value.trim();
    }

    private static synchronized Properties loadProperties() {
        if (properties == null) {
            Properties loaded = new Properties();
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
                if (in != null) {
                    // File lưu UTF-8; load(InputStream) mặc định đọc ISO-8859-1
                    loaded.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                throw new IllegalStateException("Không đọc được file " + PROPERTIES_FILE, e);
            }
            properties = loaded;
        }
        return properties;
    }
}
