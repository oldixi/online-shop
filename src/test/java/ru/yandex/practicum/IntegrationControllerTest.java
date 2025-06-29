package ru.yandex.practicum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.controller.ShopController;
import ru.yandex.practicum.model.dto.ItemCreateDto;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.Item;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class IntegrationControllerTest extends OnlineShopApplicationTests {
    @Autowired
    private MockMvc mockMvc;
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
        mockMvc.perform(get("/main/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    void testGetPageableItems() throws Exception {
        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"));
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
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"));
    }

    /*
        в) POST "/main/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/main/items"
    */
    @Test
    void testChangeItemsCountInCartWhenInItems() throws Exception {
        ItemDto itemDto = getLastItem().orElse(new ItemDto());
        mockMvc.perform(post("/main/items/" + itemDto.getId())
                        .contentType("application/x-www-form-urlencoded")
                        .queryParam("action", "plus")
                        .flashAttr("item", itemDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    /*
       д) POST "/cart/items/{id}" - изменить количество товара в корзине
       Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
       Возвращает: редирект на "/cart/items"
   */
    @Test
    void testChangeItemsCountInCartWhenInCart() throws Exception {
        ItemDto itemFromDb = getLastItem().orElse(new ItemDto());
        mockMvc.perform(post("/cart/items/" + itemFromDb.getId())
                        .contentType("application/x-www-form-urlencoded")
                        .queryParam("action", "plus")
                        .flashAttr("item", itemFromDb))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
    }

    /*
     ж) POST "/items/{id}" - изменить количество товара в корзине
        Параматры: action - значение из перечисления PLUS|MINUS|DELETE (PLUS - добавить один товар, MINUS - удалить один товар, DELETE - удалить товар из корзины)
        Возвращает: редирект на "/items/{id}"
    */
    @Test
    void testChangeItemsCountInCartWhenInItem() throws Exception {
        ItemDto itemDto = getLastItem().orElse(new ItemDto());
        mockMvc.perform(post("/items/" + itemDto.getId())
                        .contentType("application/x-www-form-urlencoded")
                        .queryParam("action", "plus")
                        .flashAttr("item", itemDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/" + itemDto.getId()));
    }

    /*
     е) GET "/items/{id}" - карточка товара
        Возвращает: шаблон "item.html"
                    используется модель для заполнения шаблона:
                    "item" - товаров (id, title, description, imgPath, count, price)
   */
    @Test
    void testGetItem() throws Exception {
        Item item = getAnyItem().orElse(new Item());
        mockMvc.perform(get("/items/" + item.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    /*
     з) POST "/buy" - купить товары в корзине (выполняет покупку товаров в корзине и очищает ее)
     Возвращает: редирект на "/orders/{id}?newOrder=true"
   */
    @Test
    void testBuy() throws Exception {
        OrderDto orderDto = getLastOrder().orElse(new OrderDto());
        Long lastId = orderDto.getId();
        mockMvc.perform(post("/buy")
                        .contentType("application/x-www-form-urlencoded")
                        .flashAttr("order", orderDto))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/"+ (lastId + 1) + "?newOrder=true"));
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
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
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
        OrderDto orderDto = getLastOrder().orElse(new OrderDto());
        System.out.println("testGetOrder: orderDto=" + orderDto);
        mockMvc.perform(get("/orders/" + orderDto.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"));
    }

    /*
        GET "main/items/add" - страница добавления товара
        Возвращает: шаблон "add-item.html"
    */
    @Test
    void testAddItemPage() throws Exception {
        mockMvc.perform(get("/main/items/add"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("add-item"));
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
        ItemDto itemDto = getLastItem().orElse(new ItemDto());
        Long lastId = itemDto.getId();
        ItemCreateDto item = ItemCreateDto.builder()
                .title(itemDto.getTitle())
                .price(itemDto.getPrice())
                .description(itemDto.getDescription())
                .price(itemDto.getPrice())
                .build();
        mockMvc.perform(post("/main/items")
                        .contentType("application/x-www-form-urlencoded")
                        .flashAttr("item", item))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/" + (lastId + 1)));
    }
}
