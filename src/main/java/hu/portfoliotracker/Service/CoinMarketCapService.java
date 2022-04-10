package hu.portfoliotracker.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CoinMarketCapService {

    private static String apiKey = "{feltöltés miatt a privát kulcsom eltávolítottam}";
    private static String sandboxKey = "b54bcf4d-1bca-4e8e-9a24-22ff2c3d462c";

    @SneakyThrows
    public JsonNode getAllCryptocurrencyData() {
        RestTemplate restTemplate = new RestTemplate();
        String coinMarketCap = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/map";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CMC_PRO_API_KEY", apiKey);
        headers.set("Accept", "application/json");

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                coinMarketCap, HttpMethod.GET, entity, String.class);

        log.info("Sikeres adat lekérés: " + response.getStatusCode().is2xxSuccessful());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode data = root.path("data");

        return data;
    }

    @SneakyThrows
    public JsonNode getMockCryptocurrencyData() {
        RestTemplate restTemplate = new RestTemplate();
        String sandbox = "https://sandbox-api.coinmarketcap.com/v1/cryptocurrency/map";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-CMC_PRO_API_KEY", sandboxKey);
        headers.set("Accept", "application/json");

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                sandbox, HttpMethod.GET, entity, String.class);

        log.info("Sikeres mock adat lekérés: " + response.getStatusCode().is2xxSuccessful());
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode data = root.path("data");

        // Mokk adatok kiegészítése valós adatokkal
        ObjectNode btc = new ObjectNode(JsonNodeFactory.instance);
        btc.put("id", "1");
        btc.put("name", "Bitcoin");
        btc.put("symbol", "BTC");
        btc.put("slug", "bitcoin");
        btc.put("rank", "1");
        btc.put("is_active", "1");
        btc.put("first_historical_data", "2013-04-28T18:47:21.000Z");
        btc.put("last_historical_data", "2021-11-21T06:49:02.000Z");
        btc.put("platform", "binance");

        ((ArrayNode)data).add(btc);

        return data;
    }

}
