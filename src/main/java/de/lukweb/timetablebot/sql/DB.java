package de.lukweb.timetablebot.sql;

import de.lukweb.timetablebot.config.TimetableBotConfig;
import org.jdbi.v3.core.Jdbi;

public class DB {

    private static Jdbi jdbi;

    public static void init(TimetableBotConfig config) {
        String host = config.getDbAddress();
        String database = config.getDbDatabase();
        String username = config.getDbUser();
        String password = config.getDbPassword();

        int port = 3306;
        if (host.contains(":")) {
            String[] hostSplit = host.split(":");
            host = hostSplit[0];
            port = Integer.parseInt(hostSplit[1]);
        }

        jdbi = Jdbi.create("jdbc:mysql://" + host + ":" + port + "/" + database +
                "?autoReconnect=true&serverTimezone=UTC", username, password);
        setupTables();
    }

    private static void setupTables() {
        jdbi.useHandle(handle -> {
            handle.execute("CREATE TABLE IF NOT EXISTS `teachers` ( " +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT, " +
                    "  `name` varchar(100) DEFAULT NULL UNIQUE, " +
                    "  PRIMARY KEY (`id`) " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            handle.execute("CREATE TABLE IF NOT EXISTS `users` ( " +
                    "  `chatid` INT(11) NOT NULL, " +
                    "  `blocked` INT(1) NOT NULL, " +
                    "  `disabled` INT(1) NOT NULL, " +
                    "  `verified` INT(1) NOT NULL, " +
                    "  `rank` VARCHAR(99) NOT NULL, " +
                    "  `grade` INT(11) DEFAULT NULL, " +
                    "  `classChar` VARCHAR(1) DEFAULT NULL, " +
                    "  `name` varchar(99) DEFAULT NULL, " +
                    "  `created` int(16) DEFAULT NULL, " +
                    "  `updated` int(16) DEFAULT NULL, " +
                    "  UNIQUE KEY `users_chatid_uindex` (`chatid`) " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            handle.execute("CREATE TABLE IF NOT EXISTS `amendments` ( " +
                    " `id` INT(11) NOT NULL AUTO_INCREMENT, " +
                    " `type` VARCHAR(99) NOT NULL, " +
                    " `date` INT(15) NOT NULL, " +
                    " `lesson` VARCHAR(99) NOT NULL, " +
                    " `teacher` VARCHAR(99) NOT NULL, " +
                    " `subject` VARCHAR(99) NOT NULL, " +
                    " `replacement` VARCHAR(99) NOT NULL, " +
                    " `grade` VARCHAR(99) NOT NULL, " +
                    " `room` VARCHAR(99) NOT NULL, " +
                    " `addtional_information` MEDIUMTEXT NOT NULL," +
                    " `deleted` int(1) DEFAULT 0 NULL, " +
                    " PRIMARY KEY (`id`) " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            handle.execute("CREATE TABLE IF NOT EXISTS `feedback` ( " +
                    "  `id` INT(11) NOT NULL AUTO_INCREMENT, " +
                    "  `from` INT(11) DEFAULT NULL, " +
                    "  `feedback` MEDIUMTEXT, " +
                    "  `timestamp` INT(11) DEFAULT NULL," +
                    "  CONSTRAINT `feedback_users_chatid_fk` FOREIGN KEY (`from`) REFERENCES `users` (`chatid`) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "  PRIMARY KEY (`id`) " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            handle.execute("CREATE TABLE IF NOT EXISTS `users_teachers` ( " +
                    "  `user` int(11) NOT NULL, " +
                    "  `teacher` int(11) NOT NULL, " +
                    "  PRIMARY KEY (`user`,`teacher`), " +
                    "  KEY `userTeachers_teachers_id_fk` (`teacher`), " +
                    "  CONSTRAINT `userTeachers_teachers_id_fk` FOREIGN KEY (`teacher`) REFERENCES `teachers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE, " +
                    "  CONSTRAINT `userTeachers_users_chatid_fk` FOREIGN KEY (`user`) REFERENCES `users` (`chatid`) ON DELETE CASCADE ON UPDATE CASCADE " +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            handle.execute("CREATE TABLE IF NOT EXISTS cafeteria ( " +
                    "    day int, " +
                    "    vegetarian tinyint, " +
                    "    meal VARCHAR(300), " +
                    "    price double, " +
                    "  PRIMARY KEY (`day`,`vegetarian`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        });
    }

    public static Jdbi get() {
        return jdbi;
    }

}
