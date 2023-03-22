package com.epam.healenium.mapper;

import com.epam.healenium.constants.Constants;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SelectorMapper {

    default Selector toSelector(SelectorRequestDto dto, String id, Optional<Selector> existSelector) {
        Selector element = new Selector();
        element.setUid(id);
        element.setClassName(dto.getClassName());
        element.setMethodName(dto.getMethodName());
        element.setCommand(dto.getCommand());
        element.setUrl(dto.getUrl());
        element.setLocator(new Locator(dto.getLocator(), dto.getType()));
        element.setNodePathWrapper(new NodePathWrapper(dto.getNodePath()));
        element.setCreatedDate(LocalDateTime.now());
        element.setEnableHealing(existSelector
                .map(Selector::getEnableHealing)
                .orElseGet(() -> "findElement".equals(dto.getCommand())));
        return element;
    }

    default List<RequestDto> toRequestDto(List<Selector> selector) {
        List<RequestDto> requestDtoResult = new ArrayList<>();
        for (Selector selectorEntity : selector) {
            SelectorRequestDto requestDto = new SelectorRequestDto();
            requestDto.setId(selectorEntity.getUid());
            requestDto.setLocator(selectorEntity.getLocator().getType() + "(" + selectorEntity.getLocator().getValue() + ")");
            requestDto.setUrl(selectorEntity.getUrl());
            requestDto.setMethodName(selectorEntity.getMethodName());
            requestDto.setClassName(selectorEntity.getClassName().contains(Constants.SINGLE_ELEMENT_PROXY_CLASS_PATH)
                    || selectorEntity.getClassName().contains(Constants.MULTIPLE_ELEMENTS_PROXY_CLASS_PATH)
                    ? null
                    : selectorEntity.getClassName());
            requestDto.setEnableHealing(selectorEntity.getEnableHealing());
            requestDto.setCommand("findElement".equals(selectorEntity.getCommand()) ? "single" : "multiple");
            requestDtoResult.add(requestDto);
        }
        return requestDtoResult;
    }

    default List<SelectorDto> toSelectorDto(List<Selector> selector) {
        List<SelectorDto> requestDtoResult = new ArrayList<>();
        for (Selector selectorEntity : selector) {
            SelectorDto selectorDto = new SelectorDto();
            selectorDto.setId(selectorEntity.getUid());
            selectorDto.setLocator(selectorEntity.getLocator().getType() + selectorEntity.getLocator().getValue());
            requestDtoResult.add(selectorDto);
        }
        return requestDtoResult;
    }

    default Selector cloneSelector(Selector source) {
        return new Selector()
                .setClassName(source.getClassName())
                .setMethodName(source.getMethodName())
                .setUrl(source.getUrl())
                .setLocator(source.getLocator())
                .setCreatedDate(source.getCreatedDate())
                .setNodePathWrapper(source.getNodePathWrapper());
    }

}