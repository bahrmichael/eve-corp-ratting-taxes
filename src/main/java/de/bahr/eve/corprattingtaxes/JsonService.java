package de.bahr.eve.corprattingtaxes;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JsonService {

    private static final String BASE_URL = "https://esi.tech.ccp.is";

    @Value("${CLIENT_ID}")
    private String clientId;

    @Value("${CLIENT_SECRET}")
    private String clientSecret;

    @Value("${REFRESH_TOKEN}")
    private String refreshToken;

    @Value("${CORPORATION_ID}")
    private Long corporationId;

    @Value("${WALLET_DIVISION ?: 1}")
    private Integer walletDivision;

    JSONArray loadCorpJournals() throws UnirestException {
        String path = "/v1/corporations/" + corporationId + "/wallets/" + walletDivision + "/journal/";
        String accessToken = getAccessToken();
        if (null == accessToken) {
            return null;
        }
        String url = BASE_URL + path + "?token=" + accessToken;

        HttpResponse<JsonNode> response = Unirest.get(url).asJson();
        if (response.getStatus() != 200) {
            log.warn("Received {} while pulling from wallet.", response.getStatus());
            return null;
        }

        return response.getBody().getArray();
    }

    private String getAccessToken() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post("https://login.eveonline.com/oauth/token")
                                                 .header("User-Agent", "Eve: Rihan Shazih")
                                                 .field("grant_type","refresh_token")
                                                 .field("refresh_token", refreshToken)
                                                 .basicAuth(clientId, clientSecret)
                                                 .asJson();
        if (response.getStatus() != 200) {
            log.warn("Received {} while creating access token.", response.getStatus());
            return null;
        }

        JSONObject object = response.getBody().getObject();
        return object.getString("access_token");
    }
}
