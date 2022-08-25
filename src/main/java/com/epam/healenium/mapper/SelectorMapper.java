package com.epam.healenium.mapper;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.epam.healenium.util.Utils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SelectorMapper {

    default Selector dtoToDocument(SelectorRequestDto dto, boolean urlForKey, boolean pathForKey) {
        String addressForKey = Utils.getAddressForKey(dto.getUrl(), urlForKey, pathForKey);
        Selector element = new Selector();
        element.setUid(Utils.buildKey(dto.getClassName(), dto.getLocator(), addressForKey));
        element.setClassName(dto.getClassName());
        element.setMethodName(dto.getMethodName());
        element.setUrl(addressForKey);
        element.setLocator(new Locator(dto.getLocator(), dto.getType()));
        element.setNodePathWrapper(new NodePathWrapper(dto.getNodePath()));
        element.setCreatedDate(LocalDateTime.now());
        return element;
    }

    default List<RequestDto> toSelectorDto(List<Selector> selector) {
        List<RequestDto> requestDtoResult = new ArrayList<>();
        for (Selector selectorEntity : selector) {
            RequestDto requestDto = new RequestDto();
            requestDto.setLocator(selectorEntity.getLocator().getType() + "(" + selectorEntity.getLocator().getValue() + ")");
            requestDto.setUrl(selectorEntity.getUrl());
            requestDto.setClassName(selectorEntity.getClassName());
            requestDtoResult.add(requestDto);
        }
        return requestDtoResult;
    }

}