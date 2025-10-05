package org.dock.service;

public class ChartService {
    public String generateTemperatureChart(String[] labels, double[] temperatures) {
        // Подготовка данных для Chart.js
        StringBuilder labelsJson = new StringBuilder("[");
        StringBuilder dataJson = new StringBuilder("[");

        for (int i = 0; i < labels.length; i++) {
            labelsJson.append("'").append(labels[i]).append("'");
            dataJson.append(temperatures[i]);

            if (i < labels.length - 1) {
                labelsJson.append(", ");
                dataJson.append(", ");
            }
        }

        labelsJson.append("]");
        dataJson.append("]");

        // Создание HTML с Canvas и скриптом Chart.js
        return "<canvas id=\"temperatureChart\"></canvas>\n" +
                "<script>\n" +
                "    const ctx = document.getElementById('temperatureChart');\n" +
                "    new Chart(ctx, {\n" +
                "        type: 'line',\n" +
                "        data: {\n" +
                "            labels: " + labelsJson.toString() + ",\n" +
                "            datasets: [{\n" +
                "                label: 'Temperature (°C)',\n" +
                "                data: " + dataJson.toString() + ",\n" +
                "                borderColor: 'rgb(75, 192, 192)',\n" +
                "                tension: 0.1\n" +
                "            }]\n" +
                "        },\n" +
                "        options: {\n" +
                "            responsive: true,\n" +
                "            plugins: {\n" +
                "                title: {\n" +
                "                    display: true,\n" +
                "                    text: '24-Hour Temperature Forecast'\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "</script>";
    }
}
