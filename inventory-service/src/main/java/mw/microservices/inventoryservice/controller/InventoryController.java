package mw.microservices.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import mw.microservices.inventoryservice.dto.InventoryResponse;
import mw.microservices.inventoryservice.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    //accept a list not a single string to do not check each object separately (through sku code)
    //order service communicates with this endpoint
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}