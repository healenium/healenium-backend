package com.epam.healenium.service;

import com.epam.healenium.model.dto.ReferenceElementsDto;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorCandidate;

import java.util.List;

public interface SelectorCandidateService {

    boolean validateReference(ReferenceElementsDto referenceElements);

    List<SelectorCandidate> getCandidates(RequestDto dto, ReferenceElementsDto referenceElements);

}
