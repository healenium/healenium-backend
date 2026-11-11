package com.epam.healenium.service;

import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorCandidate;
import com.epam.healenium.model.dto.SettingsOverride;

import java.util.List;

public interface SelectorCandidateService {

    boolean validateReference(ReferenceElementsDto referenceElements);

    /**
     * @param override per-run settings from the client; they take priority over the tenant settings
     */
    List<SelectorCandidate> getCandidates(RequestDto dto, ReferenceElementsDto referenceElements, SettingsOverride override);

}
