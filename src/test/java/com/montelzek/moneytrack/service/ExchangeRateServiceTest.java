package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.util.ExchangeRateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    public void updateRates_successResponse_shouldUpdateRates() {
        // Arrange
        ExchangeRateResponse response = new ExchangeRateResponse();
        response.setResult("success");
        Map<String, Double> conversionRates = new HashMap<>();
        conversionRates.put("EUR", 0.93);
        conversionRates.put("PLN", 4.19);
        conversionRates.put("GBP", 0.79);
        conversionRates.put("CHF", 0.91);
        conversionRates.put("JPY", 151.5);
        conversionRates.put("AAA", 22.2);
        response.setConversionRates(conversionRates);

        when(restTemplate.getForObject(anyString(), eq(ExchangeRateResponse.class))).thenReturn(response);

        // Act
        exchangeRateService.updateRates();
        Map<String, BigDecimal> rates = exchangeRateService.getRates();

        // Assert
        assertThat(rates.size()).isEqualTo(5);
        assertThat(rates.get("EUR")).isEqualTo(new BigDecimal("0.93"));
        assertThat(rates.get("PLN")).isEqualTo(new BigDecimal("4.19"));
        assertThat(rates.get("GBP")).isEqualTo(new BigDecimal("0.79"));
        assertThat(rates.get("CHF")).isEqualTo(new BigDecimal("0.91"));
        assertThat(rates.get("JPY")).isEqualTo(new BigDecimal("151.5"));
    }

    @Test
    public void updateRates_errorResponse_shouldNotUpdateRates() {
        // Arrange
        ExchangeRateResponse mockResponse = new ExchangeRateResponse();
        mockResponse.setResult("error");
        when(restTemplate.getForObject(anyString(), eq(ExchangeRateResponse.class))).thenReturn(mockResponse);

        // Act
        exchangeRateService.updateRates();
        Map<String, BigDecimal> rates = exchangeRateService.getRates();

        // Assert
        assertThat(rates).isEmpty();
    }

    @Test
    public void convertToUSD_currencyUSD_shouldReturnTheSameAmount() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(10);

        // Act
        BigDecimal convertedAmount = exchangeRateService.convertToUSD("USD", amount);

        // Assert
        assertThat(convertedAmount).isEqualTo(amount);
    }

    @Test
    public void convertToUSD_differentThanUSD_andValidCurrency_shouldReturnConvertedAmount() {
        // Arrange
        BigDecimal amount = BigDecimal.valueOf(10);
        exchangeRateService.getRates().put("EUR", new BigDecimal("0.93"));

        // Act
        BigDecimal convertedAmount = exchangeRateService.convertToUSD("EUR", amount);

        // Assert
        assertThat(convertedAmount).isEqualTo(new BigDecimal("10.75"));
    }

    @Test
    public void convertToUSD_missingRate_shouldThrowException() {
        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> exchangeRateService.convertToUSD("AAA", BigDecimal.valueOf(10)))
                .withMessage("No rate for the currency: AAA");
    }
}
