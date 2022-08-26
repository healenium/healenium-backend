package com.epam.healenium;

import com.epam.healenium.initializer.TestContainersInitializer;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.dto.*;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.*;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TestSaveLocators extends TestContainersInitializer {

    @SpyBean
    private HealingService healingService;

    private final SelectorRepository selectorRepository;
    private final HealingRepository healingRepository;
    private final HealingResultRepository healingResultRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private SelectorRequestDto selectorRequest;

    @BeforeEach
    public void setUp() {
        selectorRequest = new SelectorRequestDto();

        selectorRequest.setLocator("//div[@title='inner']");
        selectorRequest.setType("xpath");
        selectorRequest.setClassName("com.epam.healenium.SomeTest");
        selectorRequest.setMethodName("testMethod()");
        selectorRequest.setUrl("https://test-url");
        selectorRequest.setNodePath(getLastValidPath());
    }

    @AfterEach
    public void cleanUp() {
        healingResultRepository.deleteAll();
        healingRepository.deleteAll();
        selectorRepository.deleteAll();
    }

    @Test
    public void testSelectorRequestSaved() {
        healingService.saveSelector(selectorRequest);
        Assertions.assertEquals(1, selectorRepository.count());
    }

    @Test
    public void testSaveLocatorHealing() throws IOException {
        healingService.saveSelector(selectorRequest);

        HealingResultDto healingResultDto = new HealingResultDto();
        healingResultDto.setLocator(new Locator("//div[@title='inner2']", "xpath"));
        healingResultDto.setScore(.538);

        HealingRequestDto dto = new HealingRequestDto();
        dto.setLocator("//div[@title='inner']");
        dto.setType("xpath");
        dto.setClassName("com.epam.healenium.SomeTest");
        dto.setMethodName("testMethod()");
        dto.setUrl("https://test-url");
        dto.setPageContent(new String(StreamUtils.copyToByteArray(this.getClass()
                .getResourceAsStream("/index.html"))));
        dto.setResults(Collections.singletonList(healingResultDto));
        dto.setUsedResult(healingResultDto);

        HealingResultRequestDto healingRequest = new HealingResultRequestDto()
                .setRequestDto(dto);

        Map<String, String> headers = new HashMap<>();
        headers.put("sessionkey", UUID.randomUUID().toString());
        headers.put("hostproject", getClass().getName());
        headers.put("instance", RestAssured.DEFAULT_URI);

        healingService.saveHealing(healingRequest, headers);
        Assertions.assertEquals(1, healingRepository.count());
        Assertions.assertEquals(1, healingResultRepository.count());
    }

    @Test
    public void testHealingResultFromEmptyDb() {
        Set<HealingResultDto> resultDtos = healingService.getHealingResults(selectorRequest);
        Assertions.assertNotEquals(1, resultDtos.size(), "Incorrect amount of healing results were found");
    }

    private List<List<Node>> getLastValidPath() {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/nodes"));
            return this.objectMapper.readValue(bytes, NodePathWrapper.class).getNodePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
