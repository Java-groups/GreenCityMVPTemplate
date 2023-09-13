package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.language.LanguageDTO;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitFactService;
import greencity.service.LanguageService;
import greencity.service.UserService;
import greencity.validator.LanguageTranslationValidator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.verify;
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

    @InjectMocks
    private LanguageTranslationValidator languageTranslationValidator;

    @Mock
    private LanguageService languageService;
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(habitFactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
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
                "    \"complexity\": 0,\n" +
                "    \"id\": 0,\n" +
                "    \"image\": \"string\"\n" +
                "  },\n" +
                "  \"id\": 0,\n" +
                "  \"translations\": [\n" +
                "    {\n" +
                "      \"content\": \"string\",\n" +
                "      \"factOfDayStatus\": \"POTENTIAL\",\n" +
                "      \"id\": 0,\n" +
                "      \"language\": {\n" +
                "        \"code\": \"string\",\n" +
                "        \"id\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        LanguageDTO uaLanguage = new LanguageDTO(1L, "ua");
        LanguageDTO enLanguage = new LanguageDTO(2L, "en");
        LanguageDTO ruLanguage = new LanguageDTO(3L, "ru");
        List<LanguageDTO> languages = Arrays.asList(uaLanguage, enLanguage, ruLanguage);
        Mockito.when(languageService.getAllLanguages()).thenReturn(languages);
        languageTranslationValidator.initialize(null);

        mockMvc.perform(post(factsLink)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated());

        HabitFactPostDto fact = objectMapper.readValue(content, HabitFactPostDto.class);

        verify(habitFactService).save(fact);
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
    }

    @Test
    void deleteFailedHabitFactTest() throws Exception {

        Mockito.when(habitFactService.delete(99L)).thenThrow(NotFoundException.class);

        mockMvc.perform(delete(factsLink + "/{id}", 99))
                .andExpect(status().isNotFound());

        verify(habitFactService).delete(99L);
    }
}
