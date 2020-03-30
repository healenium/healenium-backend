package com.epam.healenium.mapper;

import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.HealingResultDto;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HealingMapper {

    default HealingResult resultDtoToModel(HealingResultDto dto) {
        HealingResult result = new HealingResult();
        result.setLocator(dto.getLocator());
        result.setScore(dto.getScore());
        result.setCreateDate(LocalDateTime.now());
        return result;
    }

    @IterableMapping(elementTargetType = HealingResult.class)
    Set<HealingResult> resultDtoToModel(Collection<HealingResultDto> dto);

    @InheritInverseConfiguration
    HealingResultDto modelToResultDto(HealingResult model);

    @IterableMapping(elementTargetType = HealingResultDto.class)
    Set<HealingResultDto> modelToResultDto(Collection<HealingResult> model);

}
