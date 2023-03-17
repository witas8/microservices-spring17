package mw.microservices.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import mw.microservices.inventoryservice.dto.InventoryResponse;
import mw.microservices.inventoryservice.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    @SneakyThrows //not good in a production, but catch an exception (sleep)
    public List<InventoryResponse> isInStock(List<String> skuCode) {
        log.info("Wait Started (for circuit breaker pattern)");
        //Thread.sleep(10000);
        log.info("Wait Ended (for circuit breaker pattern)");
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();
    }
}