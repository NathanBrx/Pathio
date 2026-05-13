package fds.hai811i.pathio.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtils {

    /**
     * Convertit l'heure UTC du serveur en heure locale du téléphone
     */
    public static String getLocalTime(String serverUtcTime) {
        if (serverUtcTime == null) return "";

        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(serverUtcTime);

            SimpleDateFormat localFormat = new SimpleDateFormat("dd MMM yyyy, HH'h'mm", Locale.getDefault());
            localFormat.setTimeZone(TimeZone.getDefault());

            return localFormat.format(date);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            return serverUtcTime;
        }
    }
}