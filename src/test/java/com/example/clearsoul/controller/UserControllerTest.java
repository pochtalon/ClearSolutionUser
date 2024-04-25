package com.example.clearsoul.controller;

import com.example.clearsoul.dto.UserCreateDto;
import com.example.clearsoul.dto.UserDto;
import com.example.clearsoul.dto.UserUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
class UserControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static final Long ID = 2L;
    private static final String EMAIL = "arthur_gordon_pym@mail.com";
    private static final String FIRST_NAME = "Arthur";
    private static final String LAST_NAME = "Pym";
    private static final LocalDate BIRTH_DATE = LocalDate.of(1838, 6, 16);
    private static final String ADDRESS = "Nantucket";
    private static final String PHONE_NUMBER = "+380969609696";
    private static final List<UserDto> USER_DTO_LIST = new ArrayList<>();

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
        clearTable(dataSource);
        userListInit();
    }

    @BeforeEach
    @SneakyThrows
    void addEntitiesToDataBase(@Autowired DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/add-three-users-to-db.sql")
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
                    new ClassPathResource("database/users/clear-users-table.sql")
            );
        }
    }

    @Test
    @DisplayName("Create new user")
    public void addUser_UserCreateDto_ReturnUserDto() throws Exception {
        UserCreateDto createDto = createUserCreateDto();
        String jsonRequest = objectMapper.writeValueAsString(createDto);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto expected = createUserDto();
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @Test
    @DisplayName("Create user with invalid data")
    public void addUser_InvalidUserCreateDto_BadResponse() throws Exception {
        String phoneWithoutPlus = "3805612384569";
        String phoneWithLiteral = "+38061A326598";
        String emailWithoutDog = "wrong_email.com";
        String emailDoubleDog = "wrong@email@com";
        LocalDate tooYoung = LocalDate.of(2020, 6, 6);

        UserCreateDto dtoWithLiteral = createUserCreateDto();
        dtoWithLiteral.setPhoneNumber(phoneWithLiteral);
        String jsonRequest1 = objectMapper.writeValueAsString(dtoWithLiteral);

        mockMvc.perform(post("/api/users")
                        .content(jsonRequest1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithoutPlus = createUserCreateDto();
        dtoWithoutPlus.setPhoneNumber(phoneWithoutPlus);
        String jsonRequest2 = objectMapper.writeValueAsString(dtoWithoutPlus);

        mockMvc.perform(post("/api/users")
                        .content(jsonRequest2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithoutDog = createUserCreateDto();
        dtoWithoutDog.setEmail(emailWithoutDog);
        String jsonRequest3 = objectMapper.writeValueAsString(dtoWithoutDog);

        mockMvc.perform(post("/api/users")
                        .content(jsonRequest3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoWithDogs = createUserCreateDto();
        dtoWithDogs.setEmail(emailDoubleDog);
        String jsonRequest4 = objectMapper.writeValueAsString(dtoWithDogs);

        mockMvc.perform(post("/api/users")
                        .content(jsonRequest4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        UserCreateDto dtoTooYoung = createUserCreateDto();
        dtoTooYoung.setBirthDate(tooYoung);
        String jsonRequest5 = objectMapper.writeValueAsString(dtoWithDogs);

        mockMvc.perform(post("/api/users")
                        .content(jsonRequest5)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Get all users")
    public void getUsers_ReturnListDto() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDto[].class);
        Assertions.assertEquals(3, actual.length);
        Assertions.assertEquals(USER_DTO_LIST, Arrays.stream(actual).toList());
    }

    @Test
    @DisplayName("Get user by id")
    public void getUserById_ValidId_ReturnUserDto() throws Exception {
        MvcResult resultFirstId = mockMvc.perform(get("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual1 = objectMapper.readValue(resultFirstId.getResponse().getContentAsByteArray(), UserDto.class);
        Assertions.assertEquals(USER_DTO_LIST.get(0), actual1);

        MvcResult resultSecondId = mockMvc.perform(get("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto actual2 = objectMapper.readValue(resultSecondId.getResponse().getContentAsByteArray(), UserDto.class);
        Assertions.assertEquals(USER_DTO_LIST.get(1), actual2);
    }

    @Test
    @DisplayName("Update user with valid data")
    public void updateUser_ValidData_UpdatedUser() throws Exception {
        UserUpdateDto updateDto = createUserUpdateDto();
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        MvcResult result = mockMvc.perform(put("/api/users/" + ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        UserDto expected = getUserDto();
        expected.setId(ID);
        UserDto actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDto.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update user with invalid data")
    public void updateUser_InvalidData_BadResponse() throws Exception {
        String phoneWithoutPlus = "3805612384569";
        String emailDoubleDogs = "my@wrong@mail.com";

        UserUpdateDto invalidDto = createUserUpdateDto();
        invalidDto.setPhoneNumber(phoneWithoutPlus);
        String jsonRequest1 = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(put("/api/users/" + ID)
                        .content(jsonRequest1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        invalidDto.setEmail(emailDoubleDogs);
        String jsonRequest2 = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(put("/api/users/" + ID)
                        .content(jsonRequest2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @DisplayName("Delete user by valid id")
    public void deleteUser_ValidId_ChangedUsersCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        mockMvc.perform(delete("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        Assertions.assertEquals(customersBefore.length - 1, customersAfter.length);
    }

    @Test
    @DisplayName("Delete user by invalid id")
    public void deleteUser_InvalidId_NonChangedUserCount() throws Exception {
        MvcResult beforeDeleting = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersBefore = objectMapper
                .readValue(beforeDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        mockMvc.perform(delete("/api/users/100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult afterDeleting = mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] customersAfter = objectMapper
                .readValue(afterDeleting.getResponse().getContentAsByteArray(), UserDto[].class);

        Assertions.assertEquals(customersBefore.length, customersAfter.length);
    }

    @Test
    @DisplayName("Search user by valid date range")
    public void searchByDateRange_ValidRange_ReturnListOfUser() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/search")
                        .param("fromDate", "1805-12-12")
                        .param("toDate", "1810-12-12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        UserDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDto[].class);
        List<UserDto> expected = List.of(USER_DTO_LIST.get(1), USER_DTO_LIST.get(2));
        Assertions.assertEquals(2, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    private static void userListInit(){
        USER_DTO_LIST.add(new UserDto()
                .setId(1L)
                .setEmail("cthulhu@mail.com")
                .setFirstName("Howard")
                .setLastName("Lovecraft")
                .setBirthDate(LocalDate.of(1890,8,20))
                .setAddress("Providence")
                .setPhoneNumber("+380567891265"));
        USER_DTO_LIST.add(new UserDto()
                .setId(2L)
                .setEmail("raven@mail.com")
                .setFirstName("Edgar")
                .setLastName("Poe")
                .setBirthDate(LocalDate.of(1809,1,19))
                .setAddress("Baltimore")
                .setPhoneNumber("+380567891357"));
        USER_DTO_LIST.add(new UserDto()
                .setId(3L)
                .setEmail("conan@mail.com")
                .setFirstName("Robert")
                .setLastName("Howard")
                .setBirthDate(LocalDate.of(1806,1,22))
                .setAddress("Texas")
                .setPhoneNumber("+380567891146"));
    }

    private static UserCreateDto createUserCreateDto() {
        return new UserCreateDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private static UserDto createUserDto() {
        return new UserDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private UserDto getUserDto() {
        return new UserDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }

    private UserUpdateDto createUserUpdateDto() {
        return new UserUpdateDto()
                .setEmail(EMAIL)
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setBirthDate(BIRTH_DATE)
                .setAddress(ADDRESS)
                .setPhoneNumber(PHONE_NUMBER);
    }
}
