package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemCreateDto;
import ru.yandex.practicum.model.dto.ItemsWithPagingDto;
import ru.yandex.practicum.service.CartService;
import ru.yandex.practicum.service.ItemService;
import ru.yandex.practicum.service.OrderService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ShopController {
    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;

    /*
        а) GET "/" - редирект на "/main/items"
    */
    @GetMapping("/")
    public Mono<String> redirectItems() {
        return Mono.just("redirect:/main/items");
    }

    /*
    	б) GET "/main/items" - список всех товаров плиткой на главной странице
		Параметры:
			search - строка с поисков по названию/описанию товара (по умолчанию, пустая строка - все товары)
            sort - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не использовать сортировку)
            pageSize - максимальное число товаров на странице (по умолчанию, 10)
            pageNumber - номер текущей страницы (по умолчанию, 1)
        Возвращает: шаблон "main.html"
            		используется модель для заполнения шаблона:
            			"items"  - List<List<Item>> - список товаров по N в ряд (id, title, description, imgPath, count, price)
            			"search" - строка поиска (по умолчанию, пустая строка - все товары)
            			"sort"   - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не использовать сортировку)
            			"paging":
            				"pageNumber" - номер текущей страницы (по умолчанию, 1)
            				"pageSize" - максимальное число товаров на странице (по умолчанию, 10)
            				"hasNext" - можно ли пролистнуть вперед
            				"hasPrevious" - можно ли пролистнуть назад
    */
    @GetMapping("/main/items")
    public Mono<String> getItems(Model model,
                                 @RequestParam(defaultValue = "", name = "search") String search,
                                 @RequestParam(defaultValue = "NO", name = "sort") String sort,
                                 @RequestParam(defaultValue = "1", name = "pageNumber") int pageNumber,
                                 @RequestParam(defaultValue = "10", name = "pageSize") int pageSize) {
        log.info("Start getItems");
        Mono<ItemsWithPagingDto> items = itemService.getItems(search, sort, pageNumber, pageSize);
        model.addAttribute("items", items.map(ItemsWithPagingDto::getItems));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", items.map(ItemsWithPagingDto::getPaging));
        return Mono.just("main");
    }

    /*
        в) POST "/main/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/main/items"
    */
    @PostMapping("/main/items/{id}")
    public Mono<String> changeItemCount(@PathVariable("id") Long id,
                                        @RequestParam(name = "action") String action) {
        itemService.actionWithItemInCart(id, action);
        return Mono.just("redirect:/main/items");
    }

    /*
        г) GET "/cart/items" - список товаров в корзине
        Возвращает: шаблон "cart.html"
        		    используется модель для заполнения шаблона:
        			    "items" - List<Item> - список товаров в корзине (id, title, decription, imgPath, count, price)
        			    "total" - суммарная стоимость заказа
        			    "empty" - true, если в корзину не добавлен ни один товар
    */
    @GetMapping("/cart/items")
    public Mono<String> getChart(Model model) {
        CartDto cartCopy = cartService.getCart();
        model.addAttribute("items", cartCopy.getItems().values());
        model.addAttribute("total", cartCopy.getTotal());
        model.addAttribute("empty", cartCopy.isEmpty());
        return Mono.just("cart");
    }

    /*
       	д) POST "/cart/items/{id}" - изменить количество товара в корзине
       	Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/cart/items"
    */
    @PostMapping("/cart/items/{id}")
    public Mono<String> changeItemCountInCart(@PathVariable("id") Long id,
                                              @RequestParam(name = "action") String action) {
        itemService.actionWithItemInCart(id, action);
        return Mono.just("redirect:/cart/items");
    }

    /*
        е) GET "/items/{id}" - карточка товара
       	Возвращает: шаблон "item.html"
       			    используется модель для заполнения шаблона:
       				"item" - товаров (id, title, description, imgPath, count, price)
    */
    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") Long id, Model model) {
        model.addAttribute("item", itemService.getItemDtoById(id));
        return Mono.just("item");
    }

    /*
        ж) POST "/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/items/{id}"
    */
    @PostMapping("/items/{id}")
    public Mono<String> changeItemsCount(@PathVariable("id") Long id,
                                         @RequestParam(required = false) String action) {
        log.info("Start changeItemsCount: id={}, action={}", id, action);
        String actionData = action == null ? "PLUS" : action;
        itemService.actionWithItemInCart(id, actionData);
        return Mono.just("redirect:/items/" + id);
    }

    /*
        з) POST "/buy" - купить товары в корзине (выполняет покупку товаров в корзине и очищает ее)
        Возвращает: редирект на "/orders/{id}?newOrder=true"
    */
    @PostMapping("/buy")
    public Mono<String> buy() {
        log.info("Start buy");
        return orderService.buy()
                .map(id -> "redirect:/orders/" + id + "?newOrder=true");
    }

    /*
	    и) GET "/orders" - список заказов
		Возвращает: шаблон "orders.html"
        		    используется модель для заполнения шаблона:
        			    "orders" - List<Order> - список заказов:
        				    "id" - идентификатор заказа
        				    "items" - List<Item> - список товаров в заказе (id, title, decription, imgPath, count, price)
    */
    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        model.addAttribute("orders", orderService.getOrders());
        return Mono.just("orders");
    }


    /*
        к) GET "/orders/{id}" - карточка заказа
       	Параматры: newOrder - true, если переход со страницы оформления заказа (по умолчанию, false)
        Возвращает: шаблон "order.html"
       			    используется модель для заполнения шаблона:
       				    "order" - заказ Order
       					    "id" - идентификатор заказа
        				    "items" - List<Item> - список товаров в заказе (id, title, decription, imgPath, count, price)
        			    "newOrder" - true, если переход со страницы оформления заказа (по умолчанию, false)

     */
    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(Model model, @PathVariable("id") Long id,
                           @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder) {
        model.addAttribute("newOrder", newOrder);
        model.addAttribute("order", orderService.getOrderById(id));
        return Mono.just("order");
    }


    /*
        GET "/items/image/{id}" -эндпоинт, возвращающий набор байт картинки поста
        Параметры: "id" - идентификатор поста
    */
    @GetMapping("/items/image/{id}")
    @ResponseBody
    public Mono<byte[]> getImage(@PathVariable("id") Long id) {
        return itemService.getImage(id);
    }

    /*
        GET "/main/items/add" - страница добавления товара
        Возвращает: шаблон "add-item.html"
    */
    @GetMapping("/main/items/add")
    public Mono<String> addItemPage() {
        return Mono.just("add-item");
    }

    /*
        POST "/main/items" - добавление товара
        Принимает: "multipart/form-data"
        Параметры:  "title" - название товара
                    "description" - текст товара
                    "image" - файл картинки товара (класс MultipartFile)
                    "price" - цена товара
        Возвращает: редирект на созданный "/items/{id}"
    */
    @PostMapping("/main/items")
    public Mono<String> addItem(@ModelAttribute("item") ItemCreateDto item) {
        return itemService.saveItem(item)
                .map(itemDto -> "redirect:/items/" + itemDto.getId());
    }
}
