package ru.yandex.practicum;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.model.dto.CartDto;
import ru.yandex.practicum.model.dto.ItemDto;
import ru.yandex.practicum.model.dto.OrderDto;
import ru.yandex.practicum.model.entity.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnlineShopApplicationTests {
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	protected CartDto cart;
	protected Long maxItemId = 0L;
	protected Long maxOrderId = 0L;

	@Value("${shop.image.path}")
	protected String imagePath;

	@BeforeAll
	public void createData() {
		jdbcTemplate.update("insert into items(title, description, price) values('Товар№1', 'Тестовый товар номер один', 10)");
		jdbcTemplate.update("insert into items(title, description, price) values('Товар№2', 'Тестовый товар номер два', 20)");
		jdbcTemplate.update("insert into orders(total_sum) values(2200)");
		maxOrderId = jdbcTemplate.queryForObject("select coalesce(max(id),0) from orders", Long.class);
		maxItemId = jdbcTemplate.queryForObject("select coalesce(max(id),0) from items", Long.class);
		jdbcTemplate.update("insert into items_in_order(title, description, price, count, item_id, order_id) values('Товар№1', 'Тестовый товар номер один', 10, 2, ?, ?)", maxItemId, maxOrderId);

		addItemInCart();
	}

	@AfterAll
	public void tearDownData() {
		jdbcTemplate.update("delete from items_in_order");
		jdbcTemplate.update("delete from items");
		jdbcTemplate.update("delete from orders");
	}
	protected Optional<ItemDto> getLastItem() {
		String sql = "with last_item as (select last_value(i.id) over () max_id,\n" +
				"                          i.*\n" +
				"                   from items i)\n" +
				"select last_item.id, last_item.title, last_item.description, last_item.price, last_item.image\n" +
				"from last_item\n" +
				"where last_item.id = last_item.max_id";

		return Optional.of(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> ItemDto.builder()
				.id(rs.getLong("id"))
				.title(rs.getString("title"))
				.description(rs.getString("description"))
				.price(rs.getBigDecimal("price"))
				.imagePath(imagePath + rs.getLong("id"))
				.build()));
	}

	protected Optional<Item> getAnyItem() {
		String sql = "select id, title, description, price, image\n" +
				"from items\n" +
				"limit 1";

		return Optional.of(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> Item.builder()
				.id(rs.getLong("id"))
				.title(rs.getString("title"))
				.description(rs.getString("description"))
				.price(rs.getBigDecimal("price"))
				.image(rs.getBytes("image"))
				.build()));
	}

	protected Optional<OrderDto> getLastOrder() {
		String sql = "with last_order as (select last_value(o.id) over () max_id,\n" +
				"                          o.*\n" +
				"                   from orders o)\n" +
				"select last_order.id, last_order.total_sum\n" +
				"from last_order\n" +
				"where last_order.id = last_order.max_id";

		return Optional.of(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> OrderDto.builder()
				.id(rs.getLong("id"))
				.totalSum(rs.getBigDecimal("total_sum"))
				.build()));
	}

	protected void addItemInCart() {
		ItemDto item = getLastItem().get();
		item.setCount(1);
		cart.setEmpty(false);
		cart.setItems(new HashMap<Long, ItemDto>() {{
			put(item.getId(), item);
		}});
		cart.setTotal(item.getPrice());
	}
}
