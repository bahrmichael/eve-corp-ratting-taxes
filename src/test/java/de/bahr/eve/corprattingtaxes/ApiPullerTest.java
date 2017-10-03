package de.bahr.eve.corprattingtaxes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.sun.javafx.binding.StringFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiPullerTest {

    private JsonService jsonServiceMock = mock(JsonService.class);
    private ApiPuller sut = new ApiPuller(jsonServiceMock);

    @Test
    void pull() throws Exception {
        final JSONArray array = new JSONArray("[\n"
                                              + "    {\n"
                                              + "      \"date\": \"2016-10-24T09:00:00Z\",\n"
                                              + "      \"ref_id\": 1,\n"
                                              + "      \"ref_type\": \"player_trading\"\n"
                                              + "    },{\n"
                                              + "      \"date\": \"2016-10-24T09:00:00Z\",\n"
                                              + "      \"ref_id\": 2,\n"
                                              + "      \"amount\": 3,\n"
                                              + "      \"ref_type\": \"bounty_prize_corporation_tax\"\n"
                                              + "    }\n"
                                              + "  ]");
        when(jsonServiceMock.loadCorpJournals()).thenReturn(array);

        sut.pull();
        assertEquals(3.0, sut.getSumOfDay(LocalDate.parse("2016-10-24T09:00:00Z", DateTimeFormatter.ISO_DATE_TIME)), 0.0);
    }

    @Test
    void pull_doesNotAddSameJournalTwice() throws Exception {
        final JSONArray array = new JSONArray("[\n"
                                              + "    {\n"
                                              + "      \"date\": \"2016-10-24T09:00:00Z\",\n"
                                              + "      \"ref_id\": 1,\n"
                                              + "      \"ref_type\": \"player_trading\"\n"
                                              + "    },{\n"
                                              + "      \"date\": \"2016-10-24T09:00:00Z\",\n"
                                              + "      \"ref_id\": 2,\n"
                                              + "      \"amount\": 3,\n"
                                              + "      \"ref_type\": \"bounty_prize_corporation_tax\"\n"
                                              + "    }\n"
                                              + "  ]");
        when(jsonServiceMock.loadCorpJournals()).thenReturn(array);

        LocalDate date = sut.parseDate("2016-10-24T09:00:00Z");
        sut.pull();
        assertEquals(3.0, sut.getSumOfDay(date), 0.0);
        sut.pull();
        assertEquals(3.0, sut.getSumOfDay(date), 0.0);
    }

    @Test
    void isBountyTax() throws Exception {
        final JSONObject entry = new JSONObject("{\"ref_type\": \"bounty_prize_corporation_tax\"}");
        assertTrue(sut.isBountyTax(entry));
    }

    @Test
    void isBountyTax_notABounty() throws Exception {
        final JSONObject entry = new JSONObject("{\"ref_type\": \"player_trading\"}");
        assertFalse(sut.isBountyTax(entry));
    }

    @Test
    void isBountyTax_noRefType() throws Exception {
        final JSONObject entry = new JSONObject();
        assertFalse(sut.isBountyTax(entry));
    }

    @Test
    void addTax_withNewDate() throws Exception {
        sut = new ApiPuller(jsonServiceMock);
        sut.addTax(LocalDate.now(), 1.0);

        assertEquals(1.0, sut.getTodaysSum(), 0.0);
    }

    @Test
    void addTax_withExistingDate() throws Exception {
        sut = new ApiPuller(jsonServiceMock);
        sut.addTax(LocalDate.now(), 2.0);
        sut.addTax(LocalDate.now(), 3.0);

        assertEquals(5.0, sut.getTodaysSum(), 0.0);
    }

    @Test
    void parseDate_1() throws Exception {
        LocalDate date = sut.parseDate("2016-10-24T09:00:00Z");
        assertEquals(2016, date.getYear());
        assertEquals(10, date.getMonthValue());
        assertEquals(24, date.getDayOfMonth());
    }

    @Test
    void parseDate_2() throws Exception {
        LocalDate date = sut.parseDate("2017-02-01T09:00:00Z");
        assertEquals(2017, date.getYear());
        assertEquals(2, date.getMonthValue());
        assertEquals(1, date.getDayOfMonth());
    }

    @Test
    void clearLastMonthsEntries() throws JSONException, UnirestException {
        LocalDate now = LocalDate.now();
        String date1 = StringFormatter.format("%d-%02d-%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth()).getValue() + "T09:00:00Z";
        String date2 = StringFormatter.format("%d-%02d-%02d", now.getYear(), now.getMonthValue() - 1 == 0 ? 12 : now.getMonthValue() - 1, now.getDayOfMonth()).getValue()  + "T09:00:00Z";
        final JSONArray array = new JSONArray("[\n"
                                              + "    {\n"
                                              + "      \"date\": \"" + date1 + "\",\n"
                                              + "      \"ref_id\": 1,\n"
                                              + "      \"amount\": 3,\n"
                                              + "      \"ref_type\": \"bounty_prize_corporation_tax\"\n"
                                              + "    },{\n"
                                              + "      \"date\": \"" + date2 + "\",\n"
                                              + "      \"ref_id\": 2,\n"
                                              + "      \"amount\": 4,\n"
                                              + "      \"ref_type\": \"bounty_prize_corporation_tax\"\n"
                                              + "    }\n"
                                              + "  ]");
        when(jsonServiceMock.loadCorpJournals()).thenReturn(array);

        sut.pull();

        assertEquals(3.0, sut.getSumOfDay(sut.parseDate(date1)), 0.0);
        assertEquals(4.0, sut.getSumOfDay(sut.parseDate(date2)), 0.0);

        sut.clearLastMonthsEntries();

        assertEquals(3.0, sut.getSumOfDay(sut.parseDate(date1)), 0.0);
        assertEquals(0.0, sut.getSumOfDay(sut.parseDate(date2)), 0.0);
    }
}
