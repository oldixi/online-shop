package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mapper.ItemInOrderMapper;
import ru.yandex.practicum.mapper.OrderMapper;
import ru.yandex.practicum.model.dto.CartDto;

import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.dto.OrderItemDetailDto;
import ru.yandex.practicum.model.entity.ItemInOrder;
import ru.yandex.practicum.model.entity.Order;
import ru.yandex.practicum.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ItemService itemService;
    private final ItemInOrderService itemInOrderService;
    private final ItemInOrderMapper itemInOrderMapper;
    private final JdbcTemplate jdbcTemplate;

    private final static String SQL_ORDER = """
            select o.total_sum, io.image_path, io.description, io.title, io.price, io.count, io.item_id id
            from orders o
                 left outer join items_in_order io on o.id = io.order_id
            where o.id = ?
            """;

    private final static String SQL_ORDERS = """
            select o.id, o.total_sum, io.title, io.price, io.count, io.item_id
            from orders o
                 left outer join items_in_order io on o.id = io.order_id
            """;

    @Transactional
    public Long buy() {
        CartDto cart = itemService.getCart();
        log.info("Start buy: cart={}", cart);
        if (!cart.isEmpty()) {
            OrderDto orderDto = OrderDto.builder()
                    .totalSum(cart.getTotal())
                    .items(cart.getItems().values().stream().toList())
                    .build();
            Order order = orderRepository.save(orderMapper.toOrder(orderDto));
            log.trace("Processing buy: order={}", order);
            List<ItemInOrder> items = itemInOrderMapper.toItemInOrderList(cart.getItems().values().stream().toList());
            items.forEach(item -> item.setOrder(order));
            log.trace("Processing buy: items={}", items);
            itemInOrderService.saveItemsInOrder(items);
            itemService.clearCart();
            return order.getId();
        } else return 0L;
    }

    public OrderDto getOrderById(Long orderId) {
        BigDecimal totalSum = BigDecimal.valueOf(0);
        ItemDto item = new ItemDto();
        List<ItemDto> items = List.of(item);
        List<OrderItemDetailDto> itemsDetails = jdbcTemplate.query(SQL_ORDER,
                (rs, rowNum) -> OrderItemDetailDto.builder()
                        .title(rs.getString("title"))
                        .price(rs.getBigDecimal("price") == null ? BigDecimal.valueOf(0) : rs.getBigDecimal("price"))
                        .count(rs.getInt("count"))
                        .id(rs.getLong("id"))
                        .imagePath(rs.getString("image_path"))
                        .totalSum(rs.getBigDecimal("total_sum"))
                        .build(), orderId);
        if (!itemsDetails.isEmpty()) {
            totalSum = itemsDetails.getFirst().getTotalSum();
            log.trace("Processing getOrderById: itemsDetails={}, items={}", itemsDetails, items);
            items = itemInOrderMapper.toItemDtoList(itemsDetails);
        }
        log.trace("Processing getOrderById: items={}", items);
        return OrderDto.builder()
                 .id(orderId)
                 .items(items)
                 .totalSum(totalSum)
                 .build();
    }

    public List<OrderDto> getOrders() {
        List<OrderItemDetailDto> itemsDetails = jdbcTemplate.query(SQL_ORDERS,
                (rs, rowNum) -> OrderItemDetailDto.builder()
                        .title(rs.getString("title"))
                        .price(rs.getBigDecimal("price") == null ? BigDecimal.valueOf(0) : rs.getBigDecimal("price"))
                        .count(rs.getInt("count"))
                        .orderId(rs.getLong("id"))
                        .totalSum(rs.getBigDecimal("total_sum"))
                        .build());
        List<Long> orderIds = itemsDetails.stream().map(OrderItemDetailDto::getOrderId).distinct().toList();
        List<OrderDto> orders = orderIds.stream().map(orderId -> OrderDto.builder()
                .id(orderId)
                .totalSum(itemsDetails.stream()
                        .filter(item -> orderId.equals(item.getOrderId()))
                        .findFirst()
                        .get().getTotalSum())
                .items(itemInOrderMapper.toItemDtoList(itemsDetails.stream()
                        .filter(item -> orderId.equals(item.getOrderId()))
                        .toList()))
                .build()).toList();
        log.trace("Processing getOrderById: orders={}", orders);
        return orders;
    }
}
