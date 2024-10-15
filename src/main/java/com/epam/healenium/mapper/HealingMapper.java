package com.epam.healenium.mapper;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.dto.HealingDto;
import com.epam.healenium.model.dto.HealingResultDto;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HealingMapper {

    @Mappings({
            @Mapping(target = "locator", source = "selector.locator.value"),
            @Mapping(target = "className", source = "selector.className"),
            @Mapping(target = "methodName", source = "selector.methodName"),
    })
    HealingDto modelToDto(Healing model);

    @IterableMapping(elementTargetType = HealingDto.class)
    Set<HealingDto> modelToDto(Collection<Healing> model);

    default HealingResult resultDtoToModel(HealingResultDto dto) {
        HealingResult result = new HealingResult();
        result.setLocator(getLocator(dto));
        result.setScore(getScore(dto));
        result.setCreateDate(LocalDateTime.now());
        return result;
    }

    default Double getScore(HealingResultDto dto) {
        if (dto.getScore() == null || dto.getScore() < 0 || dto.getScore() > 1) {
            throw new RuntimeException("Invalid Score value: " + dto.getScore());
        }
        return dto.getScore();
    }

    default Locator getLocator(HealingResultDto dto) {
        if (dto.getLocator() == null || !dto.getLocator().getType().contains("By")) {
            throw new RuntimeException("Invalid Locator value: " + dto.getLocator());
        }
        return dto.getLocator();
    }

    @IterableMapping(elementTargetType = HealingResult.class)
    Set<HealingResult> resultDtoToModel(Collection<HealingResultDto> dto);

    @InheritInverseConfiguration
    HealingResultDto modelToResultDto(HealingResult model);

    @IterableMapping(elementTargetType = HealingResultDto.class)
    Set<HealingResultDto> modelToResultDto(Collection<HealingResult> model);

}
