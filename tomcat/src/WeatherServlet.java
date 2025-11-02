import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
        String apiKey = System.getenv("WEATHER_API_KEY");

        if (city == null || apiKey == null || apiKey.isEmpty()) {
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

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(responseBody);
    }
}
