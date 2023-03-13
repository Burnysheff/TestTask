package test.task;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // get file from resources
        InputStream file = Main.class.getResourceAsStream("/tickets.json");

        // read data
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (file, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException exception) {
            return;
        }

        JSONObject obj = new JSONObject(textBuilder.toString());

        // list for durations
        List<Duration> diffs = new ArrayList<>();
        if (obj.has("tickets")) {
            // get all the tickets
            JSONArray tickets = obj.getJSONArray("tickets");
            for (int i = 0; i < tickets.length(); ++i) {
                JSONObject ticket = tickets.getJSONObject(i);

                // check all the flights we want (direction not specified)
                if (!ticket.getString("destination_name").equals("Тель-Авив") && ticket.getString("origin_name").equals("Владивосток") &&
                       !ticket.getString("origin_name").equals("Владивосток") && ticket.getString("destination_name").equals("Тель-Авив")) {
                    return;
                }

                // get times
                String departTime = ticket.getString("departure_time");
                String departDate = ticket.getString("departure_date");
                String arriveTime = ticket.getString("arrival_time");
                String arriveDate = ticket.getString("arrival_date");

                // formatters
                DateTimeFormatter formatterDepart = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
                if (departTime.length() == 4) {
                    formatterDepart = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
                }
                DateTimeFormatter formatterArrive = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
                if (arriveTime.length() == 4) {
                    formatterArrive = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
                }

                // get time and set the timezone
                LocalDateTime depart = LocalDateTime.parse(departDate + " " + departTime, formatterDepart);
                LocalDateTime arrive = LocalDateTime.parse(arriveDate + " " + arriveTime, formatterArrive);

                ZonedDateTime dep = ZonedDateTime.of(depart, ZoneId.of("Asia/Vladivostok"));
                ZonedDateTime arr = ZonedDateTime.of(arrive, ZoneId.of("Asia/Tel_Aviv"));

                // get duration and put to list
                Duration duration = Duration.between(dep, arr);
                diffs.add(duration);
            }
        }

        // if at least one flight found
        if (diffs.size() > 0) {
            Duration duration = diffs.get(0);
            for (int i = 1; i < diffs.size(); ++i) {
                duration = duration.plus(diffs.get(i));
            }

            // find average and show
            long seconds = duration.dividedBy(diffs.size()).getSeconds();
            System.out.println("Average duration is: " + seconds / 3600 + " Hours, " + (seconds % 3600) / 60 + " Minutes and " + seconds % 60 + " seconds");
        } else {
            System.out.println("No such tickets!");
        }

        // sort durations
        int index = 0;
        diffs.sort(Duration::compareTo);

        // we find closes to 0.9 as we have a discrete distribution here
        for (int i = 0; i < diffs.size(); ++i) {
            if (i / (double) diffs.size() < 0.9) {
                index = i;
            } else {
                break;
            }
        }
        long seconds = diffs.get(index).getSeconds();
        System.out.println("90% percentile is: " + seconds / 3600 + " Hours, " + (seconds % 3600) / 60 + " Minutes and " + seconds % 60 + " seconds");
    }
}
