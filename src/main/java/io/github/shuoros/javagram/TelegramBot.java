package io.github.shuoros.javagram;

import io.github.shuoros.javagram.method.Method;
import io.github.shuoros.jterminal.JTerminal;
import io.github.shuoros.jterminal.ansi.Color;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TelegramBot implements Javagram {

    private final String TELEGRAM = "https://api.telegram.org/bot";
    private String token;
    private boolean debug;

    public TelegramBot(String token) {
        this(token, false);
    }

    public TelegramBot(String token, boolean debug) {
        this.token = token;
        this.debug = debug;
    }

    @Override
    public String sendRequest(Method method) {
        JSONObject json = new JSONObject(method);
        json = telegramizeParameters(json);
        if (debug) {
            JTerminal.println("You send:", Color.AQUA);
            JTerminal.println(json.toString(), Color.AQUA);
        }
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(TELEGRAM + token + method.getMethod());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            byte[] input = json.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            if (debug) {
                JTerminal.println("You get:", Color.AQUA);
                JTerminal.println(response.toString(), Color.AQUA);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    private static JSONObject telegramizeParameters(JSONObject input) {
        JSONObject output = new JSONObject();
        input.keySet().forEach(key -> {
            if (input.get(key).getClass().equals(JSONObject.class)) {
                output.put(camelCaseToSnakeCase(key), telegramizeParameters(input.getJSONObject(key)));
            } else if (input.get(key).getClass().equals(JSONArray.class)) {
                output.put(camelCaseToSnakeCase(key), telegramizeArrayParameters(input.getJSONArray(key)));
            } else {
                output.put(camelCaseToSnakeCase(key), input.get(key));
            }
        });
        return output;
    }

    private static JSONArray telegramizeArrayParameters(JSONArray input) {
        JSONArray output = new JSONArray();
        for (int i = 0; i < input.length(); i++) {
            if (input.get(i).getClass().equals(JSONObject.class)) {
                output.put(telegramizeParameters(input.getJSONObject(i)));
            } else if (input.get(i).getClass().equals(JSONArray.class)) {
                output.put(telegramizeArrayParameters(input.getJSONArray(i)));
            } else if (input.get(i).getClass().equals(String.class)) {
                output.put(camelCaseToSnakeCase(input.getString(i)));
            }
        }
        return output;
    }

    private static String camelCaseToSnakeCase(String input) {
        StringBuilder output = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isLowerCase(c)) {
                output.append(c);
            } else {
                output.append("_" + Character.toLowerCase(c));
            }
        }
        return output.toString();
    }

}
