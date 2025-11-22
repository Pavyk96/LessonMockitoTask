package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

/**
 * Тесты бизнес логики для ShoppingService
 *
 * @author Daniil Mezev
 */
@ExtendWith(MockitoExtension.class)
public class ShoppingServiceTest {

    private final ShoppingService shoppingService;
    private final ProductDao productDaoMock;

    private Customer customer;

    public ShoppingServiceTest(@Mock ProductDao productDaoMock) {
        this.productDaoMock = productDaoMock;
        this.shoppingService = new ShoppingServiceImpl(productDaoMock);
    }

    /**
     * Создает нового покупателя, для каждого теста
     */
    @BeforeEach
    void setUp() {
            customer = new Customer(1L, "999");
    }

    /**
     * Тестирование получения корзины покупателя <p>
     *
     * Шаги теста:
     * <ol>
     *     <li>Создается покупатель</li>
     *     <li>Получаем корзину покупателя дважды</li>
     *     <li>Проверяем, что обе корзины идентичны</li>
     * </ol>
     * <p>
     * ОШИБКА ЛОГИКИ: при каждом получении корзины, создается новая
     */
    @Test
    void testGetCart() {
        Cart firstCart = shoppingService.getCart(customer);
        Cart secondCart = shoppingService.getCart(customer);

        Assertions.assertSame(
                firstCart,
                secondCart,
                "Ожидали одну и ту же корзину для одного покупателя"
        );
    }

    /**
     * Тестирование получения всех товаров <p>
     * Данный метод в сервисе только вызывает метод getAll() из слоя DAO, не выполняя дополнительной логики <p>
     * Поэтому тест будет пустым, и мы не проверяем логику сервиса, а только взаимодействие с DAO <p>
     */
    @Test
    void testGetAllProducts() {
        // Тест не содержит тела, так как метод не выполняет дополнительной логики.
    }

    /**
     * Тестирование поиска товара по имени (полное совпадение) <p>
     *
     * Данный метод в сервисе только вызывает метод getByName() из слоя DAO, не выполняя дополнительной логики <p>
     * Поэтому тест будет пустым, и мы не проверяем логику сервиса, а только взаимодействие с DAO <p>
     */
    @Test
    void testGetProductByName() {
        // Тест не содержит тела, так как метод не выполняет дополнительной логики.
    }

    /**
     * Тестирование случая, когда корзина пуста <p>
     *
     * Шаги теста:
     * <ol>
     *     <li>Создается пустая корзина покупателя</li>
     *     <li>Пытаемся совершить покупку</li>
     *     <li>Ожидаем, что покупка вернет false</li>
     *     <li>Проверяем, что метод DAO не был вызван</li>
     * </ol>
     */
    @Test
    void testBuyEmptyCart() throws BuyException {
        Cart cart = new Cart(customer);

        boolean result = shoppingService.buy(cart);

        Assertions.assertFalse(
                result,
                "Для пустой корзины ожидали false"
        );

        Mockito.verifyNoInteractions(productDaoMock);
    }

    /**
     * Тестирование успешной покупки товаров в корзине <p>
     *
     * Шаги теста:
     * <ol>
     *     <li>Создается покупатель и корзина.</li>
     *     <li>Добавляется товар в корзину (например, молоко).</li>
     *     <li>Покупка подтверждается через метод buy.</li>
     *     <li>Проверяется, что покупка прошла успешно.</li>
     *     <li>Проверяется, что количество товара уменьшилось.</li>
     *     <li>Проверяется, что метод save был вызван с правильным продуктом и количеством.</li>
     *     <li>Проверяется, что корзина очищена после покупки.</li>
     * </ol>
     *
     * ОШИБКА ЛОГИКИ: после успешной покупки корзина не очищается
     */
    @Test
    void testBuySuccess() throws BuyException {
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 10);

        cart.add(milk, 3);

        boolean result = shoppingService.buy(cart);

        Assertions.assertTrue(
                result,
                "Ожидали успешную покупку для корзины с товарами"
        );

        Assertions.assertEquals(
                7,
                milk.getCount(),
                "Количество товара должно уменьшиться на купленное число"
        );

        Mockito.verify(productDaoMock, Mockito.times(1))
                .save(Mockito.argThat(product ->
                        product.getName().equals("Milk") && product.getCount() == 7
                ));

        Assertions.assertTrue(
                cart.getProducts().isEmpty(),
                "После успешной покупки корзина должна быть пустой"
        );

    }

    /**
     * Тестирование случая, когда в корзине недостаточно товара для покупки <p>
     *
     * <ol>
     *     <li>Добавляем товар в корзину с количеством 3</li>
     *     <li>Уменьшаем доступное количество товара до 0, имитируя нехватку товара</li>
     *     <li>Пытаемся совершить покупку</li>
     *     <li>Проверяем, что выбрасывается исключение BuyException</li>
     *     <li>Проверяем, что сообщение об ошибке содержит информацию о недостаточном количестве товара</li>
     *     <li>Корзина не должна очищаться при ошибке</li>
     * </ol>
     */
    @Test
    void testBuyThrowsBuyExceptionWhenNotEnoughStock() {
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 5);

        // Добавляем товар в корзину с количеством 3
        cart.add(milk, 3);

        // Уменьшаем доступное количество товара до 0
        // (имитирует ситуацию, когда товара на складе не хватает)
        milk.subtractCount(3);

        BuyException exception = Assertions.assertThrows(
                BuyException.class,
                () -> shoppingService.buy(cart),
                "Ожидали BuyException при недостаточном количестве товара"
        );

        Assertions.assertEquals(
                "В наличии нет необходимого количества товара 'Milk'",
                exception.getMessage(),
                "Сообщение об ошибке должно содержать информацию о недостаточном количестве товара"
        );

        Assertions.assertFalse(
                cart.getProducts().isEmpty(),
                "При ошибке покупки корзина не должна очищаться"
        );
    }

    /**
     * Тестирование покупки при переданной null-корзине
     *
     * Шаги теста:
     * <ol>
     *     <li>Передаем в метод buy значение null вместо корзины</li>
     *     <li>Ожидаем, что метод выбросит BuyException как доменное исключение</li>
     * </ol>
     *
     * ОШИБКА ЛОГИКИ: метод buy не проверяет аргумент на null и фактически падает с NullPointerException
     */
    @Test
    void testBuyWithNullCartThrowsBuyException() {
        Assertions.assertThrows(
                BuyException.class,
                () -> shoppingService.buy(null),
                "При null-корзине ожидали BuyException, а не NPE"
        );
    }
}
