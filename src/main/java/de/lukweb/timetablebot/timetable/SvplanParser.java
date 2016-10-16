package de.lukweb.timetablebot.timetable;

import de.lukweb.timetablebot.timetable.repres.TimetableDay;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class SvplanParser {

    private String basePath;
    private String authorization;
    protected Logger logger;

    public SvplanParser(String basePath, String authorization) {
        this.basePath = basePath;
        this.authorization = authorization;
        logger = LoggerFactory.getLogger(getClass());
    }

    protected String generateUrl(TimetableDay day, int page) {
        String pageStr = String.format("%03d", page);
        return basePath + day.getWebPath() + "/subst_" + pageStr + ".htm";
    }

    protected Document requestHTML(String url) {
        try {
            // Using the custom request method, because the Jsoup.connect returns sometimes incomplete data ):
            URLConnection connection = new URL(url).openConnection();
            // Using the http basic authorization the string 'authorization' should be Base64 Encoded
            // Generator: https://www.blitter.se/utils/basic-authentication-header-generator/
            connection.setRequestProperty("Authorization", "Basic " + authorization);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder string = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                string.append(inputLine);
            }

            String lastScannedHTML = string.toString();
            return Jsoup.parse(lastScannedHTML);
        } catch (IOException e) {
            logger.warn("Cannot GET the page {}", url, e);
        }
        return null;
    }
}
