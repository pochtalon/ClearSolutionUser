package com.example.clearSoul.controller;

import com.example.clearSoul.dto.UserCreateDto;
import com.example.clearSoul.dto.UserDto;
import com.example.clearSoul.dto.UserUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final Long ID = 2L;
    private static final String EMAIL = "eric.draven@mail.com";
    private static final String FULL_NAME = "Eric Draven";
    private static final String PHONE = "+380969609696";
    private static final List<UserDto> customerDtoList = new ArrayList<>();

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        clearTable(dataSource);
        customerListInit();
    }

    @BeforeEach
    @SneakyThrows
    void addEntitiesToDataBase(@Autowired DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/customers/add-two-customers-to-db.sql")
            );
        }
    }

    @AfterEach
    void tearDown(@Autowired DataSource dataSource) {
        clearTable(dataSource);
    }

    @SneakyThrows
    private static void clearTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/customers/clear-customers-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Create new customer")
    public void addCustomer_CustomerCreateDto_ReturnCustomerDto() throws Exception {
        UserCreateDto createDto = new UserCreateDto()
                .setEmail(EMAIL)
                .setPhone(PHONE)
                .setFullName(FULL_NAME);
        String jsonRequest = objectMapper.writeValueAsString(createDto);

        MvcResult result = mockMvc.perform(post("/api/customers")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto expected = new UserDto()
                .setPhone(PHONE)
                .setEmail(EMAIL)
                .setFullName(FULL_NAME);
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Create customer with invalid data")
    public void addCustomer_InvalidCustomerCreateDto_BadResponse() throws Exception {
        String phoneWithoutPlus = "3805612384569";
        String phoneWithLiteral = "+38061A326598";
        String emailWithoutDog = "wrong_email.com";
        String emailDoubleDog = "wrong@email@com";
        UserCreateDto dtoWithLiteral = new UserCreateDto()
                .setEmail(EMAIL)
                .setPhone(phoneWithLiteral)
                .setFullName(FULL_NAME);
        String jsonRequest1 = objectMapper.writeValueAsString(dtoWithLiteral);

        mockMvc.perform(post("/api/customers")
                        .content(jsonRequest1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithoutPlus = new UserCreateDto()
                .setEmail(EMAIL)
                .setPhone(phoneWithoutPlus)
                .setFullName(FULL_NAME);
        String jsonRequest2 = objectMapper.writeValueAsString(dtoWithoutPlus);

        mockMvc.perform(post("/api/customers")
                        .content(jsonRequest2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithoutDog = new UserCreateDto()
                .setEmail(emailWithoutDog)
                .setPhone(phoneWithLiteral)
                .setFullName(FULL_NAME);
        String jsonRequest3 = objectMapper.writeValueAsString(dtoWithoutDog);

        mockMvc.perform(post("/api/customers")
                        .content(jsonRequest3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithDogs = new UserCreateDto()
                .setEmail(emailDoubleDog)
                .setPhone(PHONE)
                .setFullName(FULL_NAME);
        String jsonRequest4 = objectMapper.writeValueAsString(dtoWithDogs);

        mockMvc.perform(post("/api/customers")
                        .content(jsonRequest4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Get all customers")
    public void getCustomers_ReturnListDto() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDto[].class);
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(customerDtoList, Arrays.stream(actual).toList());
    }

    @Test
    @DisplayName("Get customer by id")
    public void getCustomerById_ValidId_ReturnCustomerDto() throws Exception {
        MvcResult resultFirstId = mockMvc.perform(get("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual1 = objectMapper.readValue(resultFirstId.getResponse().getContentAsByteArray(), UserDto.class);
        Assertions.assertEquals(customerDtoList.get(0), actual1);

        MvcResult resultSecondId = mockMvc.perform(get("/api/customers/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual2 = objectMapper.readValue(resultSecondId.getResponse().getContentAsByteArray(), UserDto.class);
        Assertions.assertEquals(customerDtoList.get(1), actual2);
    }

    @Test
    @DisplayName("Update customer with valid data")
    public void updateCustomer_ValidData_UpdatedCustomer() throws Exception {
        UserUpdateDto updateDto = createCustomerUpdateDto();
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        MvcResult result = mockMvc.perform(put("/api/customers/" + ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto expected = getCustomerDto();
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update customer with invalid data")
    public void updateCustomer_InvalidData_BadResponse() throws Exception {
        String phoneWithoutPlus = "3805612384569";
        String phoneWithLiteral = "+38061A326598";

        UserUpdateDto invalidDto = createCustomerUpdateDto();
        invalidDto.setPhone(phoneWithLiteral);
        String jsonRequest1 = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(put("/api/customers/" + ID)
                        .content(jsonRequest1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        invalidDto.setPhone(phoneWithoutPlus);
        String jsonRequest2 = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(put("/api/customers/" + ID)
                        .content(jsonRequest2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private UserDto getCustomerDto() {
        return new UserDto()
                .setId(ID)
                .setEmail("poe@mail.com")
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
    }

    private UserUpdateDto createCustomerUpdateDto() {
        return new UserUpdateDto()
                .setFullName(FULL_NAME)
                .setPhone(PHONE);
    }

    @Test
    @DisplayName("Delete book by valid id")
    public void deleteCustomer_ValidId_ChangedCustomersCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        mockMvc.perform(delete("/api/customers/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        Assertions.assertEquals(customersBefore.length - 1, customersAfter.length);
    }

    @Test
    @DisplayName("Delete customer by invalid id")
    public void deleteCustomer_InvalidId_NonChangedCustomerCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        mockMvc.perform(delete("/api/customers/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        Assertions.assertEquals(customersBefore.length, customersAfter.length);
    }

    private static void customerListInit(){
        customerDtoList.add(new UserDto()
                .setId(1L)
                .setFullName("Brendon Lee")
                .setEmail("brendon@mail.com")
                .setPhone("+380567891265"));
        customerDtoList.add(new UserDto()
                .setId(2L)
                .setFullName("Edgar Poe")
                .setEmail("poe@mail.com")
                .setPhone("+38056789113"));
    }
}
