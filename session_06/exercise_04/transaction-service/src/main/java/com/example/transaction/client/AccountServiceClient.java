package com.example.transaction.client;

import com.example.transaction.dto.AccountDto;
import com.example.transaction.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                                @Value("${account.service.url:http://account-service:8080}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    public Optional<AccountDto> getAccountByNumber(String accountNumber) {
        String url = accountServiceUrl + "/api/accounts/number/" + accountNumber;
        log.info("[Inter-Service] Calling account-service via internal DNS: {}", url);

        try {
            ResponseEntity<ApiResponse<AccountDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ApiResponse<AccountDto>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getData() != null) {
                log.info("[Inter-Service] Successfully retrieved account {} from account-service", accountNumber);
                return Optional.of(response.getBody().getData());
            }
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("[Inter-Service] Account {} not found in account-service (404)", accountNumber);
        } catch (Exception e) {
            log.error("[Inter-Service] Failed to call account-service for account {}: {}", accountNumber, e.getMessage());
        }

        return Optional.empty();
    }

    public boolean adjustBalance(String accountNumber, BigDecimal amountDelta) {
        String url = accountServiceUrl + "/api/accounts/" + accountNumber + "/adjust-balance?amountDelta=" + amountDelta;
        log.info("[Inter-Service] Updating balance in account-service: {}", url);

        try {
            ResponseEntity<ApiResponse<AccountDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    HttpEntity.EMPTY,
                    new ParameterizedTypeReference<ApiResponse<AccountDto>>() {}
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("[Inter-Service] Failed to adjust balance for account {}: {}", accountNumber, e.getMessage());
            return false;
        }
    }
}
