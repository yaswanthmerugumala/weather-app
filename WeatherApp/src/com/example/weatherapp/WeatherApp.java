package com.example.weatherapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class WeatherApp {
    private static final String API_KEY = "e60f15f263ccdd2bd27d67d555908fb6"; // Replace with your OpenWeatherMap API key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String AIR_QUALITY_URL = "https://api.openweathermap.org/data/2.5/air_pollution";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==== Welcome to the WeatherApp ====");
        System.out.println("Type 'exit' to quit the application at any time.");

        while (true) {
            System.out.print("Enter city name (or type 'exit' to quit): ");
            String city = scanner.nextLine().trim();
            if (city.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the application. Thank you for using WeatherApp!");
                break;
            }

            if (!city.isEmpty()) {
                fetchWeatherData(city);
                fetchAirQualityData(city);
            } else {
                System.out.println("City name cannot be empty. Please try again.");
            }
        }

        scanner.close();
    }

    private static void fetchWeatherData(String city) {
        String urlString = BASE_URL + "?q=" + city + "&appid=" + API_KEY + "&units=metric";

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
                double temperature = jsonResponse.getJSONObject("main").getDouble("temp");
                double feelsLike = jsonResponse.getJSONObject("main").getDouble("feels_like");
                int humidity = jsonResponse.getJSONObject("main").getInt("humidity");
                double windSpeed = jsonResponse.getJSONObject("wind").getDouble("speed");
                long sunrise = jsonResponse.getJSONObject("sys").getLong("sunrise");
                long sunset = jsonResponse.getJSONObject("sys").getLong("sunset");

                System.out.println("\n===== Weather Details for " + city + " =====");
                System.out.println("Current weather: " + weather);
                System.out.println("Temperature: " + temperature + "°C");
                System.out.println("Feels Like: " + feelsLike + "°C");
                System.out.println("Humidity: " + humidity + "%");
                System.out.println("Wind Speed: " + windSpeed + " m/s");
                System.out.println("Sunrise: " + convertUnixTime(sunrise));
                System.out.println("Sunset: " + convertUnixTime(sunset));

                // Outfit suggestion based on temperature and weather
                provideOutfitSuggestion(temperature, weather);
            } else {
                System.out.println("Error: Unable to fetch weather data for " + city + ". Response Code: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void fetchAirQualityData(String city) {
        try {
            String coordUrl = BASE_URL + "?q=" + city + "&appid=" + API_KEY;
            URL coordRequest = new URL(coordUrl);
            HttpURLConnection coordConn = (HttpURLConnection) coordRequest.openConnection();
            coordConn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(coordConn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject coordResponse = new JSONObject(response.toString());
            double lat = coordResponse.getJSONObject("coord").getDouble("lat");
            double lon = coordResponse.getJSONObject("coord").getDouble("lon");

            String aqiUrl = AIR_QUALITY_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
            URL aqiRequest = new URL(aqiUrl);
            HttpURLConnection aqiConn = (HttpURLConnection) aqiRequest.openConnection();
            aqiConn.setRequestMethod("GET");

            if (aqiConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader aqiIn = new BufferedReader(new InputStreamReader(aqiConn.getInputStream()));
                StringBuilder aqiResponse = new StringBuilder();

                while ((inputLine = aqiIn.readLine()) != null) {
                    aqiResponse.append(inputLine);
                }
                aqiIn.close();

                JSONObject aqiJson = new JSONObject(aqiResponse.toString());
                int aqi = aqiJson.getJSONArray("list").getJSONObject(0).getJSONObject("main").getInt("aqi");

                System.out.println("\n===== Air Quality Index (AQI) for " + city + " =====");
                System.out.println("AQI Level: " + getAqiLevel(aqi));
                System.out.println("AQI Value: " + aqi);
            } else {
                System.out.println("Error: Unable to fetch air quality data for " + city);
            }
        } catch (Exception e) {
            System.out.println("Error fetching AQI data: " + e.getMessage());
        }
    }

    private static void provideOutfitSuggestion(double temperature, String weather) {
        String suggestion;

        if (temperature > 30) {
            suggestion = "It's hot outside! Wear light clothing, sunglasses, and stay hydrated.";
        } else if (temperature > 20) {
            suggestion = "Moderate temperature. A t-shirt and jeans should be comfortable.";
        } else if (temperature > 10) {
            suggestion = "A bit chilly. Consider a light jacket.";
        } else {
            suggestion = "Cold weather! Dress warmly with layers, a coat, and a hat.";
        }

        if (weather.contains("rain")) {
            suggestion += " Don't forget an umbrella or raincoat!";
        }

        System.out.println("Outfit Suggestion: " + suggestion);
    }

    private static String getAqiLevel(int aqi) {
        if (aqi == 1) return "Good";
        else if (aqi == 2) return "Fair";
        else if (aqi == 3) return "Moderate";
        else if (aqi == 4) return "Poor";
        else return "Very Poor";
    }

    private static String convertUnixTime(long unixTime) {
        java.util.Date date = new java.util.Date(unixTime * 1000L);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getDefault());
        return sdf.format(date);
    }
}