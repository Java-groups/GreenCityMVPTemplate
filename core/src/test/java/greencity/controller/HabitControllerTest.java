package greencity.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.config.SecurityConfig;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.dto.habit.HabitDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Import(SecurityConfig.class)
@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    @Mock
    TagsService tagsService;
    private MockMvc mockMvc;
    @Mock
    private HabitService habitService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private HabitController habitController;
    private final Principal principal = getPrincipal();

    @BeforeEach
    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        mockMvc = MockMvcBuilders.standaloneSetup(habitController)
                .setCustomArgumentResolvers(new UserArgumentResolver(userService, modelMapper),
                        new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    public void testGetHabitById() throws Exception {
        Long habitId = 1L;
        String languageCode = "en";
        HabitDto habitDto = new HabitDto().setId(1L).setImage("image");

        when(habitService.getByIdAndLanguageCode(habitId, languageCode)).thenReturn(habitDto);

        mockMvc.perform(get("/habit/{id}", habitId)
                        .param("locale", languageCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.id").value(habitDto.getId()));

        verify(habitService, times(1)).getByIdAndLanguageCode(habitId, languageCode);
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void testGetAll() throws Exception {
        String languageCode = "en";

        List<HabitDto> habitList = Arrays.asList(new HabitDto().setId(1L), new HabitDto().setId(2L));

        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitList, habitList.size(), 0, 10);

        when(habitService.getAllHabitsByLanguageCode(any(), any(Pageable.class), eq(languageCode)))
                .thenReturn(pageableDto);

        mockMvc.perform(get("/habit")
                        .param("locale", languageCode)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.page[0].id").value(habitList.get(0).getId()))
                .andExpect(jsonPath("$.page[1].id").value(habitList.get(1).getId()));

        verify(habitService, times(1)).getAllHabitsByLanguageCode(any(), any(Pageable.class), eq(languageCode));
        verifyNoMoreInteractions(habitService);
    }


    @Test
    void testGetShoppingListItemsTest() throws Exception {
        Long habitId = 1L;
        String languageCode = "en";
        List<ShoppingListItemDto> shoppingList = Arrays.asList(new ShoppingListItemDto().setId(1L).setText("Item 1").setStatus("active"),
                new ShoppingListItemDto().setId(2L).setText("Item 2").setStatus("active"));

        when(habitService.getShoppingListForHabit(habitId, languageCode)).thenReturn(shoppingList);

        mockMvc.perform(get("/habit/{id}/shopping-list", habitId)
                        .param("locale", languageCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").value(shoppingList.get(0).getId()))
                .andExpect(jsonPath("$[0].text").value(shoppingList.get(0).getText()))
                .andExpect(jsonPath("$[0].status").value(shoppingList.get(0).getStatus()))
                .andExpect(jsonPath("$[1].id").value(shoppingList.get(1).getId()))
                .andExpect(jsonPath("$[1].text").value(shoppingList.get(1).getText()))
                .andExpect(jsonPath("$[1].status").value(shoppingList.get(1).getStatus()));

        verify(habitService, times(1)).getShoppingListForHabit(habitId, languageCode);
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void testGetAllByTagsAndLanguageCodeTest() throws Exception {
        String languageCode = "en";
        List<String> tags = Arrays.asList("tag1", "tag2");
        List<HabitDto> habitList = Arrays.asList(new HabitDto().setId(1L), new HabitDto().setId(2L));

        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitList, habitList.size(), 0, 10);

        when(habitService.getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(languageCode)))
                .thenReturn(pageableDto);

        mockMvc.perform(get("/habit/tags/search")
                        .param("locale", languageCode)
                        .param("tags", tags.toArray(new String[0]))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.page[0].id").value(habitList.get(0).getId()))
                .andExpect(jsonPath("$.page[1].id").value(habitList.get(1).getId()));

        verify(habitService, times(1)).getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(languageCode));
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void testGetAllByDifferentParametersTest() throws Exception {
        String languageCode = "en";
        List<String> tags = Arrays.asList("tag1", "tag2");
        Boolean isCustom = true;
        List<Integer> complexities = Arrays.asList(1, 2);
        List<HabitDto> habitList = Arrays.asList(new HabitDto().setId(1L), new HabitDto().setId(2L));

        PageableDto<HabitDto> pageableDto = new PageableDto<>(habitList, habitList.size(), 0, 10);

        when(habitService.getAllByDifferentParameters(any(), any(Pageable.class), eq(Optional.of(tags)), eq(Optional.of(isCustom)), eq(Optional.of(complexities)), eq(languageCode)))
                .thenReturn(pageableDto);

        mockMvc.perform(get("/habit/search")
                        .param("locale", languageCode)
                        .param("tags", tags.toArray(new String[0]))
                        .param("isCustomHabit", isCustom.toString())
                        .param("complexities", complexities.stream().map(String::valueOf).toArray(String[]::new))
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.page[0].id").value(habitList.get(0).getId()))
                .andExpect(jsonPath("$.page[1].id").value(habitList.get(1).getId()));

        verify(habitService, times(1)).getAllByDifferentParameters(any(), any(Pageable.class), eq(Optional.of(tags)), eq(Optional.of(isCustom)), eq(Optional.of(complexities)), eq(languageCode));
        verifyNoMoreInteractions(habitService);
    }

    @Test
    void testFindAllHabitsTagsTest() throws Exception {
        String languageCode = "en";
        List<String> tagsList = Arrays.asList("tag1", "tag2", "tag3");

        when(tagsService.findAllHabitsTags(eq(languageCode))).thenReturn(tagsList);

        mockMvc.perform(get("/habit/tags")
                        .param("locale", languageCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0]").value("tag1"))
                .andExpect(jsonPath("$[1]").value("tag2"))
                .andExpect(jsonPath("$[2]").value("tag3"));

        verify(tagsService, times(1)).findAllHabitsTags(eq(languageCode));
        verifyNoMoreInteractions(tagsService);
    }

    @Test
    void testAddCustomHabitTest() throws Exception {
        String content = "{\n" +
                "  \"complexity\": 2,\n" +
                "  \"customShoppingListItemDto\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"status\": \"ACTIVE\",\n" +
                "      \"text\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"defaultDuration\": 30,\n" +
                "  \"habitTranslations\": [\n" +
                "    {\n" +
                "      \"description\": \"string\",\n" +
                "      \"habitItem\": \"string\",\n" +
                "      \"languageCode\": \"string\",\n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"image\": \"string\",\n" +
                "  \"tagIds\": [\n" +
                "    0,\n" +
                "    1\n" +
                "  ]\n" +
                "}";

        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        byte[] image = "softServe academy".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile imageFile = new MockMultipartFile("image", "testImage.png", "image/png", image);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/habit/custom")
                        .file(imageFile)
                        .principal(principal)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .content(content))
                .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        AddCustomHabitDtoRequest dto = mapper.readValue(content, AddCustomHabitDtoRequest.class);

        verify(habitService).addCustomHabit(eq(dto), any(), eq(principal.getName()));

    }


//    public static void main(String[] args) throws IOException, JsonProcessingException {
//        String content = "{\n" +
//                "  \"complexity\": 2,\n" +
//                "  \"customShoppingListItemDto\": [\n" +
//                "    {\n" +
//                "      \"id\": 1,\n" +
//                "      \"status\": \"ACTIVE\",\n" +
//                "      \"text\": \"string\"\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"defaultDuration\": 30,\n" +
//                "  \"habitTranslations\": [\n" +
//                "    {\n" +
//                "      \"description\": \"string\",\n" +
//                "      \"habitItem\": \"string\",\n" +
//                "      \"languageCode\": \"string\",\n" +
//                "      \"name\": \"string\"\n" +
//                "    }\n" +
//                "  ],\n" +
//                "  \"image\": \"string\",\n" +
//                "  \"tagIds\": [\n" +
//                "    0,\n" +
//                "    1\n" +
//                "  ]\n" +
//                "}";
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        AddCustomHabitDtoRequest dto = mapper.readValue(content, AddCustomHabitDtoRequest.class);
//        System.out.println(dto);
//
//        String serializedBack = mapper.writeValueAsString(dto);
//        System.out.println(serializedBack);
//    }


    @Test
    void getFriendsAssignedToHabitProfilePicturesTest() throws Exception {
        Long habitId = 1L;
        UserVO userVO = getUserVO();

        List<UserProfilePictureDto> expectedResponse = Collections.singletonList(
                new UserProfilePictureDto(1L, "Test User", "test.jpg")
        );

        when(habitService.getFriendsAssignedToHabitProfilePictures(habitId, getUserVO().getId()))
                .thenReturn(expectedResponse);
        when(userService.findByEmail(anyString())).thenReturn(userVO);

        mockMvc.perform(get("/habit/" + habitId + "/friends/profile-pictures")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$[0].id").value(expectedResponse.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(expectedResponse.get(0).getName()))
                .andExpect(jsonPath("$[0].profilePicturePath").value(expectedResponse.get(0).getProfilePicturePath()));

        verify(habitService, times(1)).getFriendsAssignedToHabitProfilePictures(habitId, getUserVO().getId());
        verifyNoMoreInteractions(habitService);
    }

}