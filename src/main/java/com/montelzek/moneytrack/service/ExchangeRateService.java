package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.util.ExchangeRateResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Value("${currency.exchange.api.key}")
    private String apiKey;

    @Value("${currency.exchange.api.url}")
    private String apiUrl;

    private Map<String, Double> rates = new HashMap<>();

    private final RestTemplate restTemplate;

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void updateRates() {

        String url = apiUrl + apiKey + "/latest/USD";
        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            System.out.println("API Response: " + response);
            if (response != null && "success".equals(response.getResult())) {
                rates = response.getConversionRates();
            } else {
                System.err.println("Failed to fetch rates: response is null, not successful, or conversion rates are null");
            }
        } catch (Exception e) {
            System.err.println("Error during rates fetching: " + e. getMessage());
        }
    }

    public Double convertToUSD(String currency, Double amount) {

        if ("USD".equals(currency)) {
            return amount;
        }

        Double rate = rates.get(currency);

        if (rate != null && rate != 0) {
            return amount / rate;
        } else {
            throw new RuntimeException("No rate for the currency: " + currency);
        }
    }
}
