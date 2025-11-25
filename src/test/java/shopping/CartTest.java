package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.Product;

/**
 * Тесты бизнес-логики для Cart
 *
 * @author Daniil Mezev
 */
public class CartTest {

    private Customer customer;

    /**
     * Создает нового покупателя для каждого теста
     */
    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "999");
    }

    /**
     * Тестирование добавления товара с отрицательным количеством
     *
     * Шаги теста:
     * <ol>
     *     <li>Создается корзина и товар с количеством на складе 10</li>
     *     <li>Пробуем добавить товар с количеством -3</li>
     *     <li>Ожидаем IllegalArgumentException как для некорректного количества</li>
     * </ol>
     *
     * ОШИБКА ЛОГИКИ - метод add допускает отрицательное количество и не выбрасывает IllegalArgumentException
     */
    @Test
    void testAddNegativeCountThrowsIllegalArgumentException() {
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 10);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cart.add(milk, -3),
                "Ожидали IllegalArgumentException при добавлении товара с отрицательным количеством"
        );
    }

    /**
     * Тестирование изменения количества товара на отрицательное
     *
     * Шаги теста:
     * <ol>
     *     <li>Создается корзина и товар с количеством на складе 10</li>
     *     <li>Добавляем товар в корзину с количеством 3</li>
     *     <li>Пробуем изменить количество на -5</li>
     *     <li>Ожидаем IllegalArgumentException как для некорректного количества</li>
     * </ol>
     *
     * ОШИБКА ЛОГИКИ - метод edit допускает отрицательное количество и не выбрасывает IllegalArgumentException
     */
    @Test
    void testEditNegativeCountThrowsIllegalArgumentException() {
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 10);
        cart.add(milk, 3);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> cart.edit(milk, -5),
                "Ожидали IllegalArgumentException при изменении количества на отрицательное"
        );
    }

    /**
     * Тестирование поведения метода add при добавлении количества товара,
     * который уже находится в корзине
     *
     * Шаги теста:
     * <ol>
     *     <li>Создаем корзину и товар</li>
     *     <li>Добавляем товар с количеством 2</li>
     *     <li>Повторно вызываем add с количеством 3</li>
     *     <li>Ожидаем, что итоговое количество станет 5</li>
     *     <li>Фактическое поведение: количество становится 3 (перезапись)</li>
     * </ol>
     *
     * ОШИБКА ЛОГИКИ: метод add не увеличивает количество, а перезаписывает его
     */
    @Test
    void testAddShouldIncreaseCountButOverwrites() {
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 10);

        cart.add(milk, 2);  // кладём 2
        cart.add(milk, 3);  // добавляем ещё 3

        Integer actual = cart.getProducts().get(milk);

        Assertions.assertEquals(
                5,
                actual,
                "Ожидали, что повторный вызов add увеличит количество до 5, но метод перетирает значение"
        );
    }

}
