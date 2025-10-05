package org.dock.service;

public class ChartService {
    public String generateTemperatureChart(String[] labels, double[] temperatures) {
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

        return "<canvas id=\"temperatureChart\"></canvas>\n" +
                "<script>\n" +
                "    const ctx = document.getElementById('temperatureChart');\n" +
                "    new Chart(ctx, {\n" +
                "        type: 'line',\n" +
                "        data: {\n" +
                "            labels: " + labelsJson + ",\n" +
                "            datasets: [{\n" +
                "                label: 'Temperature (°C)',\n" +
                "                data: " + dataJson + ",\n" +
                "                borderColor: 'rgb(75, 192, 192)',\n" +
                "                backgroundColor: 'rgba(75, 192, 192, 0.1)',\n" +
                "                tension: 0.4,\n" +
                "                fill: true\n" +
                "            }]\n" +
                "        },\n" +
                "        options: {\n" +
                "            responsive: true,\n" +
                "            maintainAspectRatio: false,\n" +
                "            plugins: {\n" +
                "                title: {\n" +
                "                    display: true,\n" +
                "                    text: '24-Hour Temperature Forecast',\n" +
                "                    font: { size: 16 }\n" +
                "                },\n" +
                "                legend: {\n" +
                "                    display: true,\n" +
                "                    position: 'top'\n" +
                "                }\n" +
                "            },\n" +
                "            scales: {\n" +
                "                y: {\n" +
                "                    beginAtZero: false,\n" +
                "                    title: {\n" +
                "                        display: true,\n" +
                "                        text: 'Temperature (°C)'\n" +
                "                    }\n" +
                "                },\n" +
                "                x: {\n" +
                "                    title: {\n" +
                "                        display: true,\n" +
                "                        text: 'Time'\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    });\n" +
                "</script>";
    }
}
