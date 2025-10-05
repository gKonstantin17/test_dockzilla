package org.dock.service;

import com.google.gson.Gson;
import org.dock.entity.LocationData;
import org.dock.entity.WeatherResponse;
import org.dock.utils.HttpClientUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    private static final String WEATHER_API_URL = "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&hourly=temperature_2m";
    private final Gson gson = new Gson();
    private final GeocodingService geocodingService;
    private final CacheService cacheService;
    private final ChartService chartService;

    public WeatherService() {
        this.geocodingService = new GeocodingService();
        this.cacheService = new CacheService();
        this.chartService = new ChartService();
    }

    public String getWeatherData(String city) throws IOException, InterruptedException {
        String cachedData = cacheService.get(city);
        if (cachedData != null) {
            System.out.println("Returning cached data for: " + city);
            return cachedData;
        }

        System.out.println("Fetching fresh data for: " + city);

        // Если в кэше нет, получаем новые данные
        LocationData location = geocodingService.getCoordinates(city);
        String url = String.format(WEATHER_API_URL, location.getLatitude(), location.getLongitude());

        String weatherData = HttpClientUtil.get(url);
        WeatherResponse weatherResponse = gson.fromJson(weatherData, WeatherResponse.class);

        String htmlResponse = generateHtmlResponse(city, location, weatherResponse);

        cacheService.put(city, htmlResponse);

        return htmlResponse;
    }

    private String generateHtmlResponse(String city, LocationData location, WeatherResponse weatherResponse) {
        // Получаем только ближайшие 24 часа
        int hoursToShow = Math.min(weatherResponse.getHourly().getTime().size(), 24);

        // Подготовка данных для графика
        String[] labels = new String[hoursToShow];
        double[] temperatures = new double[hoursToShow];

        for (int i = 0; i < hoursToShow; i++) {
            String timeStr = weatherResponse.getHourly().getTime().get(i);
            // Формат "HH:MM" из "2024-01-01T12:00"
            if (timeStr.length() >= 16) {
                labels[i] = timeStr.substring(11, 16);
            } else {
                labels[i] = timeStr;
            }
            temperatures[i] = weatherResponse.getHourly().getTemperature_2m().get(i);
        }

        // Генерируем график
        String chartHtml = chartService.generateTemperatureChart(labels, temperatures);

        // Форматируем текущее время
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Формируем HTML-ответ
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Weather Forecast for " + city + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
                "        .container { max-width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
                "        h1 { color: #2c3e50; margin-bottom: 10px; }\n" +
                "        .location { color: #7f8c8d; margin-bottom: 20px; font-size: 14px; }\n" +
                "        .chart-container { margin-top: 30px; position: relative; height: 400px; }\n" +
                "        .info { margin-bottom: 10px; color: #95a5a6; font-size: 12px; }\n" +
                "        .stats { display: flex; justify-content: space-around; margin: 20px 0; }\n" +
                "        .stat-box { text-align: center; padding: 15px; background: #ecf0f1; border-radius: 5px; flex: 1; margin: 0 10px; }\n" +
                "        .stat-value { font-size: 24px; font-weight: bold; color: #2c3e50; }\n" +
                "        .stat-label { font-size: 12px; color: #7f8c8d; margin-top: 5px; }\n" +
                "    </style>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1> Weather Forecast for " + city + "</h1>\n" +
                "        <div class=\"location\"> " + location.getName() + ", " + location.getCountry() +
                " (" + String.format("%.4f", location.getLatitude()) + ", " + String.format("%.4f", location.getLongitude()) + ")</div>\n" +
                "        <div class=\"info\"> Last updated: " + currentTime + " (cached for 15 minutes)</div>\n" +
                generateStatsHtml(temperatures) +
                "        <div class=\"chart-container\">\n" +
                chartHtml +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String generateStatsHtml(double[] temperatures) {
        if (temperatures.length == 0) return "";

        double min = temperatures[0];
        double max = temperatures[0];
        double sum = 0;

        for (double temp : temperatures) {
            if (temp < min) min = temp;
            if (temp > max) max = temp;
            sum += temp;
        }

        double avg = sum / temperatures.length;

        return "        <div class=\"stats\">\n" +
                "            <div class=\"stat-box\">\n" +
                "                <div class=\"stat-value\">" + String.format("%.1f°C", min) + "</div>\n" +
                "                <div class=\"stat-label\">Minimum</div>\n" +
                "            </div>\n" +
                "            <div class=\"stat-box\">\n" +
                "                <div class=\"stat-value\">" + String.format("%.1f°C", avg) + "</div>\n" +
                "                <div class=\"stat-label\">Average</div>\n" +
                "            </div>\n" +
                "            <div class=\"stat-box\">\n" +
                "                <div class=\"stat-value\">" + String.format("%.1f°C", max) + "</div>\n" +
                "                <div class=\"stat-label\">Maximum</div>\n" +
                "            </div>\n" +
                "        </div>\n";
    }
}