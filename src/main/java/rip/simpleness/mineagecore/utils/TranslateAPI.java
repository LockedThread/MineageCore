package rip.simpleness.mineagecore.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class TranslateAPI {

    private static final String API_KEY = "trnsl.1.1.20190201T012532Z.291bfacf982d1a9e.b2473b135e7a0ac7a1282be1048fdf6359072fc6";

    private static String request(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla");

            InputStream inputStream = urlConnection.getInputStream();

            String received = new BufferedReader(new InputStreamReader(inputStream)).readLine();

            inputStream.close();
            return received;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String translate(String text, String sourceLang, String targetLang) {
        return request("https://translate.yandex.net/api/v1.5/tr.json/translate?key=" + API_KEY +
                "&text=" + text +
                "&lang=" + sourceLang + "-" + targetLang);
    }
}