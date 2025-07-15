package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.controller.ShopController;
import ru.yandex.practicum.model.dto.ItemCreateDto;
import ru.yandex.practicum.model.dto.OrderDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegrationControllerTest extends OnlineShopApplicationTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ShopController shopController;

    @Test
    void testController() {
        assertNotNull(shopController);
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
    @Test
    void testGetItems() throws Exception {
        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Витрина товаров</title>"));
                });
    }

    /*
        г) GET "/cart/items" - список товаров в корзине
        Возвращает: шаблон "cart.html"
        		    используется модель для заполнения шаблона:
        			    "items" - List<Item> - список товаров в корзине (id, title, decription, imgPath, count, price)
        			    "total" - суммарная стоимость заказа
        			    "empty" - true, если в корзину не добавлен ни один товар
    */
    @Test
    void testGetItemsInCart() throws Exception {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Корзина товаров</title>"));
                });
    }

    /*
        в) POST "/main/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/main/items"
    */
    @Test
    void testChangeItemsCountInCartWhenInItems() throws Exception {
        getLastItem().publishOn(Schedulers.boundedElastic()).doOnNext(itemDto -> {
            webTestClient.post()
                    .uri("/main/items/" + itemDto.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("action=MINUS")
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().valueEquals("Location", "/main/items");
        }).subscribe();
    }

    /*
       д) POST "/cart/items/{id}" - изменить количество товара в корзине
       Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
       Возвращает: редирект на "/cart/items"
   */
    @Test
    void testChangeItemsCountInCartWhenInCart() throws Exception {
        getLastItem().publishOn(Schedulers.boundedElastic()).doOnNext(itemDto -> {
            webTestClient.post()
                    .uri("/cart/items/" + itemDto.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("action=PLUS")
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().valueEquals("Location", "/cart/items");
        }).subscribe();
    }

    /*
     ж) POST "/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/items/{id}"
    */
    @Test
    void testChangeItemsCountInCartWhenInItem() throws Exception {
        getLastItem().publishOn(Schedulers.boundedElastic()).doOnNext(itemDto -> {
            webTestClient.post()
                    .uri("/items/" + itemDto.getId())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue("action=PLUS")
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().valueEquals("Location", "/items/" + itemDto.getId());
        }).subscribe();
    }

     /*
     е) GET "/items/{id}" - карточка товара
        Возвращает: шаблон "item.html"
                    используется модель для заполнения шаблона:
                    "item" - товаров (id, title, description, imgPath, count, price)
    */
    @Test
    void testGetItem() throws Exception {
        getAnyItem().publishOn(Schedulers.boundedElastic()).doOnNext(itemDto ->
            webTestClient.get()
                    .uri("/items/" + itemDto.getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.TEXT_HTML)
                    .expectBody(String.class).consumeWith(response -> {
                        String body = response.getResponseBody();
                        assertNotNull(body);
                        assertTrue(body.contains("<title>Витрина товаров</title>"));
                    })).subscribe();
    }

    /*
     з) POST "/buy" - купить товары в корзине (выполняет покупку товаров в корзине и очищает ее)
     Возвращает: редирект на "/orders/{id}?newOrder=true"
    */
    @Test
    void testBuy() throws Exception {
        getLastOrder().publishOn(Schedulers.boundedElastic()).doOnNext(orderDto ->
            webTestClient.post()
                    .uri("/buy")
                    .bodyValue(OrderDto.builder()
                            .totalSum(orderDto.getTotalSum())
                            .items(orderDto.getItems())
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection()
                    .expectHeader().valueEquals("Location", "/orders/"+ (orderDto.getId() + 1) + "?newOrder=true"))
                .log()
                .subscribe();
    }

    /*
     и) GET "/orders" - список заказов
     Возвращает: шаблон "orders.html"
                 используется модель для заполнения шаблона:
                     "orders" - List<Order> - список заказов:
                        "id" - идентификатор заказа
                        "items" - List<Item> - список товаров в заказе (id, title, decription, imgPath, count, price)
    */
    @Test
    void testGetOrders() throws Exception {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Заказы</title>"));
                });
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
    @Test
    void testGetOrder() throws Exception {
        getLastOrder().publishOn(Schedulers.boundedElastic()).doOnNext(orderDto ->
                webTestClient.get()
                .uri("/orders/" + orderDto.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<title>Заказ</title>"));
                }))
                .subscribe();
    }

    /*
        GET "main/items/add" - страница добавления товара
        Возвращает: шаблон "add-item.html"
    */
    @Test
    void testAddItemPage() throws Exception {
        webTestClient.get()
                .uri("/main/items/add")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class).consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertNotNull(body);
                    assertTrue(body.contains("<h3>Изображение</h3>"));
                });
    }

    /*
        POST "/main/items" - добавление товара
        Принимает: "multipart/form-data"
        Параметры: "title" - название товара
                    "description" - текст товара
                    "image" - файл картинки товара (класс MultipartFile)
                    "price" - цена товара
        Возвращает: редирект на созданный "/items/{id}"
    */
    @Test
    void testAddItem() throws Exception {
        getLastItem().map(itemDto ->
            Mono.just(ItemCreateDto.builder()
                    .title(itemDto.getTitle() + "_NEW")
                    .price(itemDto.getPrice())
                    .description(itemDto.getDescription() + "_NEW")
                    .build())
                    .publishOn(Schedulers.boundedElastic())
                    .doOnNext(item -> webTestClient.post()
                        .uri("/main/items")
                        .bodyValue(item)
                        .exchange()
                        .expectStatus().is3xxRedirection()
                        .expectHeader().valueEquals("Location", "/items/" + (itemDto.getId() + 1))))
                .subscribe();
    }
}
