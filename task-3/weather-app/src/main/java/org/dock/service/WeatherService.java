package org.dock.service;

import com.google.gson.Gson;
import org.dock.entity.LocationData;
import org.dock.entity.WeatherResponse;
import org.dock.utils.HttpClient;

import java.io.IOException;
import java.time.LocalDateTime;

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
        // Проверяем кэш
        String cachedData = cacheService.get(city);
        if (cachedData != null) {
            return cachedData;
        }

        // Если в кэше нет, получаем новые данные
        LocationData location = geocodingService.getCoordinates(city);
        String url = String.format(WEATHER_API_URL, location.getLatitude(), location.getLongitude());

        String weatherData = HttpClient.get(url);
        WeatherResponse weatherResponse = gson.fromJson(weatherData, WeatherResponse.class);

        // Создаем HTML-страницу с графиком
        String htmlResponse = generateHtmlResponse(city, weatherResponse);

        // Кэшируем результат
        cacheService.put(city, htmlResponse);

        return htmlResponse;
    }

    private String generateHtmlResponse(String city, WeatherResponse weatherResponse) {
        // Получаем только ближайшие 24 часа
        int hoursToShow = Math.min(weatherResponse.getHourly().getTime().size(), 24);

        // Подготовка данных для графика
        String[] labels = new String[hoursToShow];
        double[] temperatures = new double[hoursToShow];

        for (int i = 0; i < hoursToShow; i++) {
            labels[i] = weatherResponse.getHourly().getTime().get(i).substring(11, 16); // Формат "HH:MM"
            temperatures[i] = weatherResponse.getHourly().getTemperature_2m().get(i);
        }

        // Генерируем график
        String chartHtml = chartService.generateTemperatureChart(labels, temperatures);

        // Формируем HTML-ответ
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Weather Forecast for " + city + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                "        .container { max-width: 800px; margin: 0 auto; }\n" +
                "        h1 { color: #2c3e50; }\n" +
                "        .chart-container { margin-top: 20px; }\n" +
                "        .info { margin-bottom: 10px; color: #7f8c8d; }\n" +
                "    </style>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>Weather Forecast for " + city + "</h1>\n" +
                "        <div class=\"info\">Last updated: " + LocalDateTime.now() + "</div>\n" +
                "        <div class=\"chart-container\">\n" +
                chartHtml +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}