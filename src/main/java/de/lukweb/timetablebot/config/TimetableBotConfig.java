package de.lukweb.timetablebot.config;

import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class TimetableBotConfig {

    private String dbAddress;
    private String dbUser;
    private String dbPassword;
    private String dbDatabase;

    private boolean instantRun;
    private String botName;
    private String botToken;

    private String tableBasePath;
    private String tableAuth;

    private Logger logger;

    public TimetableBotConfig() {
        logger = LoggerFactory.getLogger(getClass());
        read(new File("config.ini"));
    }

    private void read(File file) {
        try {
            writeIfNotExists(file);
            Wini ini = new Wini(file);

            Section database = ini.get("database");
            dbAddress = database.get("address");
            dbUser = database.get("user");
            dbPassword = database.get("password");
            dbDatabase = database.get("database");

            Section query = ini.get("query");
            instantRun = query.get("instant_run").equalsIgnoreCase("true");

            Section telegram = ini.get("telegram");
            botName = telegram.get("bot_name");
            botToken = telegram.get("bot_token");

            Section timetable = ini.get("timetable");
            tableBasePath = timetable.get("base_path");
            tableAuth = timetable.get("authentication");

        } catch (IOException e) {
            logger.error("Error while writing or reading config", e);
        } catch (NullPointerException e) {
            logger.error("Please update your configuration to the latest version", e);
            System.exit(1);
        }

        if (instantRun) {
            logger.info("Instant Run is enabled");
        }
    }

    private void writeIfNotExists(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) return;
        file.createNewFile();

        Wini ini = new Wini(file);

        Section database = ini.add("database");
        database.add("address", "localhost:3306");
        database.add("user", "timetablebot");
        database.add("password", "secret");
        database.add("database", "timetablebot");

        Section query = ini.add("query");
        query.add("instant_run", false);

        Section telegram = ini.add("telegram");
        telegram.add("bot_name", "timetablebot");
        telegram.add("bot_token", "111111:AAAAAAAAAAAAAAAAAAAA");

        Section timetable = ini.add("timetable");
        // The slash at the end is important
        timetable.add("base_path", "http://svplan.alte-landesschule.de/");
        timetable.add("authentication", "Generate an Base64 String at " +
                "https://www.blitter.se/utils/basic-authentication-header-generator/");

        ini.store();
    }

    public String getDbAddress() {
        return dbAddress;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbDatabase() {
        return dbDatabase;
    }

    public boolean isInstantRun() {
        return instantRun;
    }

    public String getBotName() {
        return botName;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getTableBasePath() {
        return tableBasePath;
    }

    public String getTableAuth() {
        return tableAuth;
    }
}
