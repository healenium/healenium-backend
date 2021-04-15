package com.epam.healenium;

import com.epam.healenium.initializer.TestContainersInitializer;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.dto.HealingRequestDto;
import com.epam.healenium.model.dto.HealingResultDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.epam.healenium.repository.HealingRepository;
import com.epam.healenium.repository.HealingResultRepository;
import com.epam.healenium.repository.ReportRepository;
import com.epam.healenium.repository.SelectorRepository;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.treecomparing.Node;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.restassured.parsing.Parser.JSON;

@Slf4j
@DisplayName("A Healenium backend")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ExtendWith(MockitoExtension.class)
public class HealingTest extends TestContainersInitializer {

    @SpyBean
    private HealingService healingService;

    private final SelectorRepository selectorRepository;
    private final HealingRepository healingRepository;
    private final HealingResultRepository healingResultRepository;
    private final ReportRepository reportRepository;

    private final ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@LocalServerPort int localPort, @Autowired ObjectMapper mapper) {
        log.info("Use localPort: {}", localPort);
        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((type, s) -> mapper)
        );
        RestAssured.baseURI = "http://localhost:" + localPort + "/healenium";
        RestAssured.defaultParser = JSON;
    }

    @Test
    @DisplayName("Successfully save new healing for existed selector")
    void saveHealing() throws IOException {
        // firstly create selector
        SelectorRequestDto selectorRequest = buildSelectorRequest();
        healingService.saveSelector(selectorRequest);
        Assertions.assertEquals(1, selectorRepository.count());

        // proceed healing
        HealingRequestDto healingRequest = buildHealingRequest();
        Map<String, String> headers = getHeaders();
        MultipartFile screenshot = buildMultipart();
        healingService.saveHealing(healingRequest, screenshot, headers);
        Assertions.assertEquals(1, healingRepository.count());
        Assertions.assertEquals(1, healingResultRepository.count());

        // healing results can be returned
        Set<HealingResultDto> resultDtos = healingService.getHealingResults(buildRequest());
        Assertions.assertEquals(1, resultDtos.size(), "Incorrect amount of healing results were found");
        Assertions.assertEquals(.538, resultDtos.iterator().next().getScore(), "Healing result score not match expected");

    }

    @Nested
    @DisplayName("recheck via rest")
    class RestTests {

        @BeforeEach
        public void setUp(@LocalServerPort int localPort) throws IOException {
//        // create request data
//        dto = new HealingRequestDto();
//        dto.setPageContent(new String(StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/index.html"))));
//        dto.setType("xpath");
//        dto.setLocator("//div[@title='inner']");
//        dto.setClassName("com.epam.healenium.FormattedLocatorTest");
//        dto.setMethodName("testNotThrowingExceptionWithFormattedLocator");
        }

        @Test
        @DisplayName("will return 204 if healing is impossible")
        @Disabled("Need to fix and recheck")
        public void noHealing() {
//        given().
//            contentType(ContentType.JSON)
//            .accept(ContentType.JSON)
//            .body(dto)
//            .when()
//            .post("/heal")
//            .then()
//            .using().assertThat()
//            .statusCode(HttpStatus.NO_CONTENT.value());
        }

        @Test
        @DisplayName("successfully perform healing if history data exists")
        @Disabled("Need to fix and recheck")
        public void healingWithResult() {
//        Selector selector = buildElement();
//        selectorRepository.save(selector);
//        given().contentType(ContentType.JSON)
//            .accept(ContentType.JSON)
//            .body(dto)
//            .when()
//            .post("/heal")
//            .then()
//            .using().defaultParser(JSON).assertThat()
//            .statusCode(HttpStatus.OK.value())
//            .body("type", containsString("cssSelector"));
        }

    }


    @AfterEach
    public void cleanUp() {
        reportRepository.deleteAll();
        healingResultRepository.deleteAll();
        healingRepository.deleteAll();
        selectorRepository.deleteAll();
    }

    private RequestDto buildRequest() {
        RequestDto dto = new RequestDto();
        dto.setLocator("//div[@title='inner']");
        dto.setClassName("com.epam.healenium.SomeTest");
        dto.setMethodName("testMethod()");
        return dto;
    }

    private MultipartFile buildMultipart() throws IOException {
        String name = "screenshots";
        String originalFileName = "test_image.png";
        String contentType = "application/octet-stream";
        return new MockMultipartFile(name, originalFileName, contentType, StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/test_image.png")));
    }

    private HealingRequestDto buildHealingRequest() throws IOException {
        HealingResultDto healingResultDto = buildHealingResultDto();

        HealingRequestDto dto = new HealingRequestDto();
        dto.setLocator("//div[@title='inner']");
        dto.setType("xpath");
        dto.setClassName("com.epam.healenium.SomeTest");
        dto.setMethodName("testMethod()");
        dto.setPageContent(new String(StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/index.html"))));
        dto.setResults(Collections.singletonList(healingResultDto));
        dto.setUsedResult(healingResultDto);
        return dto;
    }

    private HealingResultDto buildHealingResultDto() {
        HealingResultDto dto = new HealingResultDto();
        dto.setLocator(new Locator("//div[@title='inner2']", "xpath"));
        dto.setScore(.538);
        return dto;
    }

    private SelectorRequestDto buildSelectorRequest() {
        SelectorRequestDto dto = new SelectorRequestDto();
        dto.setLocator("//div[@title='inner']");
        dto.setType("xpath");
        dto.setClassName("com.epam.healenium.SomeTest");
        dto.setMethodName("testMethod()");
        dto.setNodePath(getLastValidPath());
        return dto;
    }

    private List<List<Node>> getLastValidPath() {
        try {
            byte[] bytes = StreamUtils.copyToByteArray(this.getClass().getResourceAsStream("/nodes"));
            //noinspection unchecked
            return objectMapper.readValue(bytes, NodePathWrapper.class).getNodePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("sessionKey", UUID.randomUUID().toString());
        headers.put("hostProject", getClass().getName());
        headers.put("instance", RestAssured.DEFAULT_URI);
        return headers;
    }

}
