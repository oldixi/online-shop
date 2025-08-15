package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.model.dto.*;
import ru.yandex.practicum.service.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.function.Function;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ShopController {
    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentsService paymentsService;
    private final UserService userService;

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
    public Mono<String> getItems(Model model, Principal principal,
                                 @RequestParam(defaultValue = "", name = "search") String search,
                                 @RequestParam(defaultValue = "NO", name = "sort") String sort,
                                 @RequestParam(defaultValue = "1", name = "pageNumber") int pageNumber,
                                 @RequestParam(defaultValue = "10", name = "pageSize") int pageSize) {
        log.debug("Start getItems");
        model.addAttribute("items", itemService.getItems(search, sort, pageNumber, pageSize,
                principal == null ? "" : principal.getName()));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", itemService.getPaging(search, sort, pageNumber, pageSize));
        return Mono.just("main");
    }

    /*
        в) POST "/main/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/main/items"
    */
    @PostMapping("/main/items/{id}")
    public Mono<String> changeItemCount(@PathVariable("id") Long id,
                                        ServerWebExchange exchange) {
        log.debug("Start changeItemCount: id={}, exchange={}", id, exchange);
        return inspectRequest(id, exchange)
                .map(itemDto -> "redirect:/main/items");
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
    public Mono<String> getChart(Model model, Principal principal) {
        return cartService.getCart(principal == null ? "" : principal.getName())
                .doOnNext(cart -> model.addAttribute("items", cart.getItems().values()))
                .doOnNext(cart -> model.addAttribute("total", cart.getTotal()))
                .doOnNext(cart -> model.addAttribute("empty", cart.isEmpty()))
                .zipWith(paymentsService.getBalance().onErrorReturn(BigDecimal.valueOf(-1)).log(), (cart, balance) ->
                    model.addAttribute("canBuy", balance.compareTo(cart.getTotal()) >= 0))
                .map(cart -> "cart");
    }

    /*
       	д) POST "/cart/items/{id}" - изменить количество товара в корзине
       	Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/cart/items"
    */
    @PostMapping("/cart/items/{id}")
    public Mono<String> changeItemsCountInCart(@PathVariable("id") Long id,
                                               ServerWebExchange exchange) {
        log.debug("Start changeItemsCountInCart: id={}, exchange={}", id, exchange);
        return inspectRequest(id, exchange)
                .onErrorComplete()
                .map(itemDto -> "redirect:/cart/items");
    }

    /*
        е) GET "/items/{id}" - карточка товара
       	Возвращает: шаблон "item.html"
       			    используется модель для заполнения шаблона:
       				"item" - товаров (id, title, description, imgPath, count, price)
    */
    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable("id") Long id, Model model, Principal principal) {
        return itemService.getItemDtoById(id, principal == null ? "" : principal.getName())
                .doOnNext(item -> model.addAttribute("item", item))
                .map(order -> "item");
    }

    /*
        ж) POST "/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/items/{id}"
    */
    @PostMapping("/items/{id}")
    public Mono<String> changeItemsCount(@PathVariable("id") Long id,
                                         ServerWebExchange exchange) {
        log.debug("Start changeItemsCount: id={}, exchange={}", id, exchange);
        return inspectRequest(id, exchange)
                .map(itemDto -> "redirect:/items/" + id);
    }

    /*
        з) POST "/buy" - купить товары в корзине (выполняет покупку товаров в корзине и очищает ее)
        Возвращает: редирект на "/orders/{id}?newOrder=true"
    */
    @PostMapping("/buy")
    public Mono<String> buy(Principal principal) {
        log.debug("Start buy");
        return orderService.buy(principal == null ? "" : principal.getName())
                .log()
                .map(id -> "redirect:/orders/" + id + "?newOrder=true")
                .onErrorReturn("redirect:/error?message=" + URLEncoder.encode("недостаточно средств. Пополните счет и повторите попытку"));
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
    @GetMapping("/admin/items/add")
    @PostAuthorize("hasRole('ADMIN')")
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
    @PostMapping("/admin/items/add")
    @PostAuthorize("hasRole('ADMIN')")
    public Mono<String> addItem(@ModelAttribute("item") Mono<ItemCreateDto> item) {
        return itemService.saveItem(item)
                .map(itemDto -> "redirect:/items/" + itemDto.getId());
    }

    /*
    GET "/main/items/add" - страница добавления товара
    Возвращает: шаблон "add-item.html"
    */
    @GetMapping("/signup")
    public Mono<String> addUserPage() {
        return Mono.just("add-user");
    }

    /*
    POST "/signup" - создание аккаунта
    Параметры:  "login" - название товара
                "password" - текст товара
    Возвращает: редирект на форму логина "/login"
    */
    @PostMapping("/signup")
    public Mono<String> addUser(@ModelAttribute("user") Mono<NewUserDto> user) throws UnsupportedEncodingException {
        return userService.addUser(user)
                .log()
                .onErrorReturn("redirect:/error?message=" + URLEncoder.encode("пользователь с таким логином уже существует", "UTF-8"))
                .map(login -> "redirect:/login");
    }

    /*
    GET "/error" - страница сообщения об ошибке
    Возвращает: шаблон "error.html"
    */
    @GetMapping("/error")
    public Mono<String> getError(Model model,
                                 @RequestParam(defaultValue = "повторите операцию позже", name = "message") String message)
            throws UnsupportedEncodingException {
        log.info("Start getError: message={}", message);
        model.addAttribute("message", URLDecoder.decode(message, "UTF-8"));
        return Mono.just("error");
    }

    private Mono<ItemDto> inspectRequest(Long id, ServerWebExchange exchange) {
        log.debug("Start inspectRequest: exchange={}", exchange);
        return exchange.getFormData()
                .map(MultiValueMap::toSingleValueMap)
                .map(map -> map.get("action"))
                .zipWith(exchange.getPrincipal().map(Principal::getName), (action, login) -> itemService.actionWithItemInCart(id, action, login))
                .flatMap(Function.identity());
    }
}
