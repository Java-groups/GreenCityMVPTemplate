package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
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

import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
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
        String json = "{" +
                "\"habit\": {" +
                "  \"id\": 1" +
                "}," +
                "\"translations\": [" +
                "  {" +
                "    \"content\": \"content content content\"," +
                "    \"language\": {" +
                "      \"code\": \"en\"," +
                "      \"id\": 2" +
                "    }" +
                "  }" +
                "]" +
                "}";

        MockMultipartFile jsonFile = new MockMultipartFile("habitFactPostDto",
                "",
                "application/json",
                json.getBytes());

        this.mockMvc.perform(multipart(factsLink)
                        .file(jsonFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }


//    @Test
//    void updateHabitFactTest() throws Exception {
//        HabitFactUpdateDto updateDto = new HabitFactUpdateDto();
//
//        mockMvc.perform(put(factsLink + "/{id}", 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(updateDto)))
//                .andExpect(status().isOk());
//
//        verify(habitFactService).update(updateDto, 1L);
//    }

    @Test
    void deleteFailedHabitFactTest() throws Exception {

        Mockito.when(habitFactService.delete(99L)).thenThrow(NotFoundException.class);

        mockMvc.perform(delete(factsLink + "/{id}", 99))
                .andExpect(status().isNotFound());

        verify(habitFactService).delete(99L);
    }
}
