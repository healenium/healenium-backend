package com.epam.healenium.service.impl;

import com.epam.healenium.model.domain.Llm;
import com.epam.healenium.model.domain.Vcs;
import com.epam.healenium.model.dto.VcsDto;
import com.epam.healenium.repository.LlmRepository;
import com.epam.healenium.repository.VcsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceImplTest {

    @Mock
    private VcsRepository vcsRepository;
    @Mock
    private LlmRepository llmRepository;
    @InjectMocks
    private IntegrationServiceImpl service;

    @Test
    void testShortTokenIsMaskedForNonAiCaller() {
        when(vcsRepository.findAll()).thenReturn(List.of(vcs("abc"), vcs("abcdef")));

        List<VcsDto> vcsList = service.getVcsAll();

        assertEquals(List.of("*****", "*****"), vcsList.stream().map(VcsDto::getAccessToken).toList());
    }

    @Test
    void testLongTokenKeepsFirstAndLastThreeForNonAiCaller() {
        when(vcsRepository.findAll()).thenReturn(List.of(vcs("abcdefg")));

        List<VcsDto> vcsList = service.getVcsAll();

        assertEquals("abc*****efg", vcsList.get(0).getAccessToken());
    }

    @Test
    void testShortTokenIsReturnedWholeToAiCaller() {
        when(vcsRepository.findAll()).thenReturn(List.of(vcs("abcdef")));

        VcsDto active = service.getActiveVcs();

        assertEquals("abcdef", active.getAccessToken());
    }

    @Test
    void testLongTokenIsReturnedWholeToAiCaller() {
        when(llmRepository.findAll()).thenReturn(List.of(llm("abcdefghij")));

        assertEquals("abcdefghij", service.getActiveLlm().getAccessToken());
    }

    private Vcs vcs(String token) {
        return new Vcs()
                .setUid(token)
                .setName(token)
                .setActive(true)
                .setAccessToken(token);
    }

    private Llm llm(String token) {
        return new Llm()
                .setUid(token)
                .setName(token)
                .setModel("model")
                .setActive(true)
                .setAccessToken(token);
    }
}
