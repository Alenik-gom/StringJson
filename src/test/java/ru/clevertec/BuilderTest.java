package ru.clevertec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.clevertec.domain.Customer;
import ru.clevertec.domain.Order;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {
    private final EasyRandom generator = new EasyRandom();
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        // Регистрация модуля для поддержки Java 8 DateTime API с кастомным сериализатором
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        mapper.registerModule(javaTimeModule);
        // Отключение записи дат как timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Customer createCustomer() {
      return generator.nextObject(Customer.class);
    }

   private Order createOrder() {
        return generator.nextObject(Order.class);
    }

    @Test
    void shouldCreateCustomerFromJson() throws Exception {
        //given
        Customer customerExpected = createCustomer();
        String jsonString = mapper.writeValueAsString(customerExpected);
        //when
        Customer customerActual = Builder.fromJson(jsonString, Customer.class);
        //then
        assertNotNull(customerActual);
        assertAll(
                () -> assertEquals(customerExpected.getId(), (customerActual.getId())),
                () -> assertEquals(customerExpected.getId().getClass(), customerActual.getId().getClass()),
                () -> assertEquals(customerExpected.getLastName(), customerActual.getLastName()),
                () -> assertEquals(customerExpected.getOrders().size(), customerActual.getOrders().size()),
                () -> assertEquals(customerActual.getOrders().getFirst().getProducts().size(),
                        customerExpected.getOrders().getFirst().getProducts().size())
        );
    }

    @Test
    void shouldCreateEmptyCustomerFromJson() throws Exception {
        //given
        Customer customerExpected = new Customer();
        String jsonString = mapper.writeValueAsString(customerExpected);
        //when
        Customer customerActual = Builder.fromJson(jsonString, Customer.class);
        //then
        assertNotNull(customerActual);
        assertAll(
                () -> assertNull(customerActual.getId()),
                () -> assertNull(customerActual.getOrders()),
                () -> assertNull(customerActual.getDateBirth()),
                () -> assertNull(customerActual.getFirstName()),
                () -> assertNull(customerActual.getLastName())
        );
    }

    @Test
    void ShouldCreateJsonFromCustomer() throws Exception {
        //given
        Customer customer = createCustomer();
        String jsonStringExpected = mapper.writeValueAsString(customer);
        //when
        String jsonStringActual = Builder.toJson(customer);
        //then
        assertNotNull(jsonStringActual);
        assertEquals(jsonStringActual.length(), jsonStringExpected.length());
    }
    @Test
    void ShouldCreateJsonFromEmptyCustomer() throws Exception {
        //given
        Customer customer = new Customer();
        String jsonStringExpected = mapper.writeValueAsString(customer);
        //when
        String jsonStringActual = Builder.toJson(customer);
        //then
        assertNotNull(jsonStringActual);
        assertEquals(jsonStringActual.length(), jsonStringExpected.length());
    }

    @Test
    void shouldCreateOrderFromJson() throws Exception {
        //given
        Order orderExpected = createOrder();
        String jsonString = mapper.writeValueAsString(orderExpected);
        //when
        Order orderActual = Builder.fromJson(jsonString, Order.class);
        //then
        assertNotNull(orderActual);
        assertAll(
                () -> assertEquals(orderExpected.getId(), orderActual.getId()),
                () -> assertEquals(orderExpected.getCreateDate(), orderActual.getCreateDate()),
                () -> assertEquals(orderExpected.getProducts().size(), orderActual.getProducts().size()),
                () -> assertEquals(orderExpected.getProducts().getFirst().getPrices().size(),
                        orderActual.getProducts().getFirst().getPrices().size()),
                () -> assertEquals(orderExpected.getProducts().getFirst().getId(),
                        orderActual.getProducts().getFirst().getId())
        );
    }
    @Test
    void ShouldCreateJsonFromOrder() throws Exception {
        //given
        Order order = createOrder();
        String jsonStringExpected = mapper.writeValueAsString(order);
        //when
        String jsonStringActual = Builder.toJson(order);
        //then
        assertNotNull(jsonStringActual);
        assertEquals(jsonStringActual.length(), jsonStringExpected.length());
    }

}