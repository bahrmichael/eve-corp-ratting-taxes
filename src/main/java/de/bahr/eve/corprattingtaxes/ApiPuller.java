package de.bahr.eve.corprattingtaxes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiPuller {

    private final JsonService jsonService;
    private Map<LocalDate, Double> sums = new HashMap<>();
    private List<Long> completedRefIds = new ArrayList<>();

    public ApiPuller(final JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @PostConstruct
    public void init() {
        pull();
    }

    @Async
    @Scheduled(cron = "0 * * * * *")
    public void trigger() {
        pull();
    }

    @Async
    @Scheduled(cron = "0 0 0 10 * ?") // every 10th day of a month
    public void clearLastMonthsEntries() {
        LocalDate lastMonthDate = LocalDate.now().minusMonths(1);
        YearMonth yearMonthObject = YearMonth.of(lastMonthDate.getYear(), lastMonthDate.getMonthValue());
        int daysInMonth = yearMonthObject.lengthOfMonth();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate date = LocalDate.of(lastMonthDate.getYear(), lastMonthDate.getMonthValue(), i);
            sums.remove(date);
        }
    }

    void pull() {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonService.loadCorpJournals();
        } catch (UnirestException e) {
            log.error("UnirestException", e);
        }
        if (null == jsonArray) {
            return;
        }


        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject journalEntry = jsonArray.getJSONObject(i);

            long refId = journalEntry.getLong("ref_id");
            if (completedRefIds.contains(refId)) {
                continue;
            }
            completedRefIds.add(refId);

            if (isBountyTax(journalEntry)) {
                final double tax = journalEntry.getDouble("amount");
                final LocalDate date = parseDate(journalEntry.getString("date"));
                addTax(date, tax);
            }
        }
    }

    LocalDate parseDate(final String date) {
        return LocalDate.parse(date, DateTimeFormatter.ISO_DATE_TIME);
    }

    void addTax(final LocalDate date, final double tax) {
        if (sums.containsKey(date)) {
            sums.put(date, sums.get(date) + tax);
        } else {
            sums.put(date, tax);
        }
    }

    boolean isBountyTax(final JSONObject journalEntry) {
        return journalEntry.has("ref_type")
               && journalEntry.getString("ref_type").equals("bounty_prize_corporation_tax");
    }

    Double getTodaysSum() {
        return getSumOfDay(LocalDate.now());
    }

    Double getSumOfDay(final LocalDate date) {
        return sums.getOrDefault(date, 0.0);
    }
}
