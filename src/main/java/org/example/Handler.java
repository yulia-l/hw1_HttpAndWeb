package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Handler {
    private final String requestData;
    private final String processedData;

    public Handler() {
        this.requestData = getRequestData();
        this.processedData = processRequestData(this.requestData);
        handleRequest();
    }

    private String getRequestData() {
        return "";
    }

    public String processRequestData(String requestData) {
        if (requestData != null && !requestData.isEmpty()) {
            return requestData.replaceAll("\\s+", "");
        } else {
            return "";
        }
    }

    public void handleRequest() {
        sendResponse(this.processedData);
    }

    public String sendRequest() throws IOException {
        // Создание объекта URL с данными запроса
        URL url = new URL(requestData);
        // Открытие соединения с URL-адресом
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Установить метод запроса GET
        connection.setRequestMethod("GET");
        // Отправить запрос и получить код ответа
        int responseCode = connection.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (responseCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            System.out.println("Ошибка отправки запроса");
            return null;
        }
    }

    public void sendResponse(String processedData) {
        try {
            // Создание объекта URL с данными запроса
            URL url = new URL(getRequestData());
            // Открытие соединения с URL-адресом
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Установить метод запроса POST
            connection.setRequestMethod("POST");
            // Указание типа содержимого тела запроса
            connection.setRequestProperty("Content-Type", "application/json");
            // Отправка данных ответа
            connection.getOutputStream().write(processedData.getBytes());
            // Получение кода ответа
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("Ответ отправлен успешно");
            } else {
                System.out.println("Ошибка отправки ответа");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
