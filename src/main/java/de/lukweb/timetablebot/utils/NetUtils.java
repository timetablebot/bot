package de.lukweb.timetablebot.utils;

import java.io.IOException;
import java.util.Base64;
import org.jsoup.Jsoup;

public class NetUtils {

    public static boolean checkAuthenication(String url, String username, String password) {
        String base64 = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        try {
            Jsoup.connect(url).header("Authorization", "Basic " + base64).get();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
