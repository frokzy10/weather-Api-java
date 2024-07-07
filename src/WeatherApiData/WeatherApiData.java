package WeatherApiData;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class WeatherApiData {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            String city;
            do {
                System.out.println("=========================");
                System.out.println("Введите город (Напишите No чтобы выйти): ");
                city = scanner.nextLine();

                if (city.equalsIgnoreCase("No")) break;

                JSONObject cityLocationData = getLocationData(city);
                if (cityLocationData == null) {
                    System.out.println("Не удалось найти данные о городе.");
                    continue;
                }

                double latitude = (double) cityLocationData.get("latitude");
                double longitude = (double) cityLocationData.get("longitude");

                if (latitude == 0 || longitude == 0) {
                    System.out.println("Не удалось получить координаты города.");
                    continue;
                }

                displayWeatherData(latitude, longitude);
            } while (!city.equalsIgnoreCase("No"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void displayWeatherData(double latitude, double longitude) {
        try {
            String url = "https://api.open-meteo.com/v1/forecast?latitude=" +
                    latitude + "&longitude=" + longitude + "&current_weather=true";
            HttpURLConnection apiConnection = fetchApiResponse(url);

            if (apiConnection.getResponseCode() != 200) {
                System.out.println("Ошибка в поиске API");
                return;
            }

            String jsonResponse = readApiResponse(apiConnection);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            JSONObject currentWeather = (JSONObject) jsonObject.get("current_weather");

            if (currentWeather == null) {
                System.out.println("Не удалось получить данные о текущей погоде.");
                return;
            }

            String time = (String) currentWeather.get("time");
            System.out.println("Время: " + time);

            double temp = (double) currentWeather.get("temperature");
            System.out.println("Погода в градусах: " + temp);

            double windSpeed = (double) currentWeather.get("windspeed");
            System.out.println("Скорость ветра: " + windSpeed);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getLocationData(String city) {
        city = city.replaceAll(" ", "+");
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                city + "&count=1&language=en&format=json";

        try {
            HttpURLConnection apiConnection = fetchApiResponse(urlString);

            if (apiConnection.getResponseCode() != 200) {
                System.out.println("API не найдена");
                return null;
            }

            String jsonResponse = readApiResponse(apiConnection);
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(jsonResponse);

            JSONArray locationData = (JSONArray) resultJsonObj.get("results");

            if (locationData == null || locationData.isEmpty()) {
                System.out.println("Не удалось найти данные о местоположении.");
                return null;
            }

            return (JSONObject) locationData.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readApiResponse(HttpURLConnection apiConnection) {
        try {
            StringBuilder resultJson = new StringBuilder();

            Scanner scanner = new Scanner(apiConnection.getInputStream());

            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            return resultJson.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}