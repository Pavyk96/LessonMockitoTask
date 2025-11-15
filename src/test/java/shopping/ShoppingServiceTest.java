package shopping;

import customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import product.Product;
import product.ProductDao;

import java.util.List;

/**
 * ShoppingServiceTest
 *
 * @author Daniil Mezev
 */
@ExtendWith(MockitoExtension.class)
public class ShoppingServiceTest {

    private final ShoppingService shoppingService;
    private final ProductDao productDaoMock;

    public ShoppingServiceTest(@Mock ProductDao productDaoMock) {
        this.productDaoMock = productDaoMock;
        this.shoppingService = new ShoppingServiceImpl(productDaoMock);
    }

    /**
     * Получить корзину покупателю
     *
     * ОШИБКА ЛОГИКИ: при каждом получении корзины, создается новая
     */
    @Test
    void testGetCart() {
        Customer customer = new Customer(1L, "999");

        Cart firstCart = shoppingService.getCart(customer);
        Cart secondCart = shoppingService.getCart(customer);

        Assertions.assertSame(
                firstCart,
                secondCart,
                "Ожидали одну и ту же корзину для одного покупателя"
        );
    }

    /**
     * Получить все продукты
     */
    @Test
    void testGetAllProducts() {
        Product product1 = new Product("Milk", 10);
        Product product2 = new Product("Bread", 5);
        List<Product> expectedProducts = List.of(product1, product2);

        Mockito.when(productDaoMock.getAll())
                .thenReturn(expectedProducts);

        List<Product> actualProducts = shoppingService.getAllProducts();

        Assertions.assertEquals(
                expectedProducts,
                actualProducts,
                "Сервис должен вернуть список товаров из DAO"
        );

        Mockito.verify(productDaoMock, Mockito.times(1)).getAll();
    }

    /**
     * Получить продукт по имени
     */
    @Test
    void testGetProductByName() {
        String name = "Milk";
        Product expectedProduct = new Product(name, 10);

        Mockito.when(productDaoMock.getByName(name))
                .thenReturn(expectedProduct);

        Product actualProduct = shoppingService.getProductByName(name);

        Assertions.assertEquals(
                expectedProduct,
                actualProduct,
                "Сервис должен вернуть товар, который вернул DAO по имени"
        );

        Mockito.verify(productDaoMock, Mockito.times(1))
                .getByName(name);
    }

    /**
     * В корзине нет товаров - получаем false
     */
    @Test
    void testBuyEmptyCart() throws BuyException {
        Customer customer = new Customer(1L, "999");
        Cart cart = new Cart(customer);

        boolean result = shoppingService.buy(cart);

        Assertions.assertFalse(
                result,
                "Для пустой корзины ожидали false"
        );

        Mockito.verifyNoInteractions(productDaoMock);
    }

    /**
     * В корзине есть товары - покупка проходит успешно (true)
     *
     * ОШИБКА ЛОГИКИ: после успешной покупки корзина не очищается
     */
    @Test
    void testBuySuccess() throws BuyException {
        Customer customer = new Customer(1L, "999");
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
                .save(milk);

        Assertions.assertTrue(
                cart.getProducts().isEmpty(),
                "После успешной покупки корзина должна быть пустой"
        );

    }

    /**
     * Недостаточно товара - выбрасываем BuyException
     */
    @Test
    void testBuyThrowsBuyException() {
        Customer customer = new Customer(1L, "999");
        Cart cart = new Cart(customer);
        Product milk = new Product("Milk", 5);

        cart.add(milk, 3);

        milk.subtractCount(3);

        Assertions.assertThrows(
                BuyException.class,
                () -> shoppingService.buy(cart),
                "Ожидали BuyException при недостаточном количестве товара"
        );

        Assertions.assertFalse(
                cart.getProducts().isEmpty(),
                "При ошибке покупки корзина не должна очищаться"
        );

        Mockito.verifyNoInteractions(productDaoMock);
    }

}
