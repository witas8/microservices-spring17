package mw.microservices.orderservice.service;

import lombok.RequiredArgsConstructor;
import mw.microservices.orderservice.dto.InventoryResponse;
import mw.microservices.orderservice.dto.OrderLineItemsDto;
import mw.microservices.orderservice.dto.OrderRequest;
import mw.microservices.orderservice.event.OrderPlacedEvent;
import mw.microservices.orderservice.model.Order;
import mw.microservices.orderservice.model.OrderLineItems;
import mw.microservices.orderservice.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder; //used to be private final WebClient webClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order(); //why generated new instance?
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        //prepare a list to send it to the inventory service via webclient
        List<String> skuCodesList = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        //call inventory service and place order if product is in stock
        InventoryResponse[] inventoryResponseArr = webClientBuilder.build()
                .get()
                //.uri("http://localhost:8082/api/inventory",
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodesList).build())
                .retrieve()
                //public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode)
                //InventoryResponse copied and pasted from inventory service that we communicate with
                .bodyToMono(InventoryResponse[].class)
                //block sets a synchronous communication in the webclient
                .block();

        //assert inventoryResponseArr != null;
        boolean existAllProductsInStock = Arrays.stream(inventoryResponseArr)
                .allMatch(InventoryResponse::isInStock);

        //if(Boolean.TRUE.equals(existAllProductsInStock)) { //Boolean.TRUE for not null pointer assertion
        if(existAllProductsInStock){
            orderRepository.save(order);
            //create new class just to make it easier to send data as JSON
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
            return "Order Place Successfully";
        } else {
            throw new IllegalArgumentException("Product is not in stock, please try again later.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}