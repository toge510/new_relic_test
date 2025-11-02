import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WeatherServlet extends HttpServlet {

    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String city = req.getParameter("city");
        String action = req.getParameter("action");

        String dbHost = System.getenv("DB_HOST");
        String dbPort = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");

        if ("history".equals(action)) {
            if (city == null || city.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().write("City is missing for history lookup.");
                return;
            }

            if (dbHost != null && dbName != null && dbUser != null) {
                String portPart = (dbPort == null || dbPort.isEmpty()) ? "3306" : dbPort;
                String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", dbHost, portPart, dbName);
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    try (Connection c = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                        String querySql = "SELECT id, city, body, created_at FROM weather_history WHERE city = ? ORDER BY created_at DESC LIMIT 10";
                        try (PreparedStatement ps = c.prepareStatement(querySql)) {
                            ps.setString(1, city);
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                StringBuilder jsonResult = new StringBuilder();
                                jsonResult.append("[");
                                boolean first = true;
                                while (rs.next()) {
                                    if (!first) {
                                        jsonResult.append(",");
                                    }
                                    jsonResult.append("{");
                                    jsonResult.append("\"id\":").append(rs.getInt("id")).append(",");
                                    jsonResult.append("\"city\":\"").append(rs.getString("city")).append("\",");
                                    jsonResult.append("\"body\":").append(rs.getString("body")).append(",");
                                    jsonResult.append("\"created_at\":\"").append(rs.getTimestamp("created_at")).append("\"");
                                    jsonResult.append("}");
                                    first = false;
                                }
                                jsonResult.append("]");
                                resp.setContentType("application/json;charset=UTF-8");
                                resp.getWriter().write(jsonResult.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    resp.setStatus(500);
                    resp.getWriter().write("Could not retrieve history: " + e.getMessage());
                    e.printStackTrace(System.err);
                }
            } else {
                resp.setStatus(500);
                resp.getWriter().write("Database connection not configured.");
            }
            return;
        }

        String apiKey = System.getenv("WEATHER_API_KEY");

        if (city == null || city.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            resp.setStatus(400);
            resp.getWriter().write("City or API key is missing.");
            return;
        }

        String urlString = String.format(API_URL, city, apiKey);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();

        if (responseCode != 200) {
            InputStream errorStream = conn.getErrorStream();
            String errorBody = "Error: Could not get weather data. Response code: " + responseCode;
            if (errorStream != null) {
                Scanner errScanner = new Scanner(errorStream).useDelimiter("\\A");
                if (errScanner.hasNext()) {
                    errorBody = errScanner.next();
                }
                errScanner.close();
            }
            resp.setStatus(responseCode);
            resp.setContentType("text/plain;charset=UTF-8");
            resp.getWriter().write(errorBody);
            return;
        }

        InputStream inputStream = conn.getInputStream();
        Scanner scanner = new Scanner(inputStream);
        String responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        scanner.close();

        if (dbHost != null && dbName != null && dbUser != null) {
            String portPart = (dbPort == null || dbPort.isEmpty()) ? "3306" : dbPort;
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC", dbHost, portPart, dbName);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                try (Connection c = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
                    String insertSql = "INSERT INTO weather_history (city, body, created_at) VALUES (?, ?, NOW())";
                    try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                        ps.setString(1, city);
                        ps.setString(2, responseBody);
                        ps.executeUpdate();
                        System.out.println("[WeatherServlet] Inserted history for city=" + city);
                    }
                }
            } catch (Exception e) {
                System.err.println("[WeatherServlet] Could not write history: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(responseBody);
    }
}
