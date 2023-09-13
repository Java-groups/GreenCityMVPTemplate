package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitFactService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HabitFactControllerTest {

    private static final String factsLink = "/facts";
    private MockMvc mockMvc;
    @InjectMocks
    private HabitFactController habitFactController;
    @Mock
    private HabitFactService habitFactService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Validator mockValidator;
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    public void setUp() {

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(habitFactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .setValidator(mockValidator)
                .build();
    }

    @Test
    void getRandomFactByHabitIdTest() throws Exception {
        mockMvc.perform(get(factsLink + "/random" + "/{id}", 1))
                .andExpect(status().isOk());

        verify(habitFactService).getRandomHabitFactByHabitIdAndLanguage(1L, "en");
    }

    @Test
    void getHabitFactOfTheDayTest() throws Exception {
        mockMvc.perform(get(factsLink + "/dayFact/{languageId}", 1))
                .andExpect(status().isOk());

        verify(habitFactService).getHabitFactOfTheDay(1L);
    }

    @Test
    void getAllHabitFactsTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        mockMvc.perform(get(factsLink + "?page=1"))
                .andExpect(status().isOk());

        verify(habitFactService).getAllHabitFacts(pageable, "en");
    }

    @Test
    void deleteHabitFactTest() throws Exception {
        mockMvc.perform(delete(factsLink + "/{id}", 1))
                .andExpect(status().isOk());

        verify(habitFactService).delete(1L);
    }

    @Test
    void saveHabitFactTest() throws Exception {

        String content = "{\n" +
                "  \"habit\": {\n" +
                "    \"id\": 1\n" +
                "  },\n" +
                "  \"translations\": [\n" +
                "    {\n" +
                "      \"content\": \"content content content\",\n" +
                "      \"language\": {\n" +
                "        \"code\": \"ua\",\n" +
                "        \"id\": 1\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"content content content\",\n" +
                "      \"language\": {\n" +
                "        \"code\": \"en\",\n" +
                "        \"id\": 2\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"content\": \"content content content\",\n" +
                "      \"language\": {\n" +
                "        \"code\": \"ru\",\n" +
                "        \"id\": 3\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post(factsLink)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        HabitFactPostDto dto = mapper.readValue(content, HabitFactPostDto.class);

        verify(habitFactService).save(dto);
    }

    @Test
    void updateHabitFactTest() throws Exception {
        String content = "{\n" +
                "  \"habit\": {\n" +
                "    \"id\": 1\n" +
                "  },\n" +
                "  \"translations\": [\n" +
                "    {\n" +
                "      \"content\": \"Test Test Test\",\n" +
                "      \"factOfDayStatus\": \"POTENTIAL\",\n" +
                "      \"language\": {\n" +
                "        \"code\": \"en\",\n" +
                "        \"id\": 2\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(put(factsLink + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        ObjectMapper mapper = new ObjectMapper();
        HabitFactUpdateDto dto = mapper.readValue(content, HabitFactUpdateDto.class);
        verify(habitFactService).update(dto, 1L);
    }

    @Test
    void deleteFailedHabitFactTest() throws Exception {

        Mockito.when(habitFactService.delete(1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(delete(factsLink + "/{id}", 1))
                .andExpect(status().isNotFound());

        verify(habitFactService).delete(1L);
    }
}
