package de.lukweb.timetablebot.cafeteria;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class CafeteriaSQL {

    private Handle handle;

    public CafeteriaSQL(Handle handle) {
        this.handle = handle;
    }

    private RowMapper<CafeteriaMeal> getMealMapper() {
        return (rs, ctx) -> new CafeteriaMeal(
                LocalDate.ofEpochDay(rs.getInt("day")),
                rs.getInt("vegetarian") == 1,
                rs.getString("meal"),
                rs.getDouble("price")
        );
    }

    public List<CafeteriaMeal> getMeals(long day) {
        return handle.select("SELECT * FROM cafeteria WHERE day = ?", day)
                .map(getMealMapper())
                .list();
    }

    public void saveMeals(List<CafeteriaMeal> meals) {
        PreparedBatch batch = handle.prepareBatch("INSERT INTO cafeteria (day, vegetarian, meal, price) " +
                "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE day=day");

        for (CafeteriaMeal meal : meals) {
            batch.add(meal.getEpochDay(), meal.isVegetarian(), meal.getDescription(), meal.getPrice());
        }

        if (batch.size() > 0) {
            int[] modCounts = batch.execute();

            int modCount = IntStream.of(modCounts).sum();
            if (modCount > 0) {
                Logger logger = LoggerFactory.getLogger(getClass());
                // TODO There are always eight rows modified
                // => Problem: This number doesn't say anything
                //    https://mariadb.com/kb/en/library/insert-on-duplicate-key-update/
                logger.info("{} rows modified while saving cafeteria meals", modCount);
            }
        }
        batch.close();
    }

}
