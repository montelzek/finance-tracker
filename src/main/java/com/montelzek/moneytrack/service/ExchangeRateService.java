package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.util.ExchangeRateResponse;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Value("${currency.exchange.api.key}")
    private String apiKey;

    @Value("${currency.exchange.api.url}")
    private String apiUrl;

    @Getter
    private Map<String, BigDecimal> rates = new HashMap<>();

    private static final List<String> TARGET_CURRENCIES = Arrays.asList("EUR", "PLN", "GBP", "CHF", "JPY");

    private final RestTemplate restTemplate;

    public ExchangeRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void updateRates() {

        String url = apiUrl + apiKey + "/latest/USD";
        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            if (response != null && "success".equals(response.getResult())) {
                Map<String, Double> doubleRates = response.getConversionRates();
                rates.clear();
                for (Map.Entry<String, Double> entry : doubleRates.entrySet()) {
                    if (TARGET_CURRENCIES.contains(entry.getKey())) {
                        rates.put(entry.getKey(), new BigDecimal(entry.getValue().toString()));
                    }
                }
            } else {
                System.err.println("Failed to fetch rates: response is null, not successful, or conversion rates are null");
            }
        } catch (Exception e) {
            System.err.println("Error during rates fetching: " + e. getMessage());
        }
    }

    public BigDecimal convertToUSD(String currency, BigDecimal amount) {

        if ("USD".equals(currency)) {
            return amount;
        }

        BigDecimal rate = rates.get(currency);

        if (rate != null && rate.compareTo(BigDecimal.ZERO) != 0) {
            return amount.divide(rate, 2, RoundingMode.HALF_UP);
        } else {
            throw new RuntimeException("No rate for the currency: " + currency);
        }
    }
}
