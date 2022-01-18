package com.epam.healenium.mapper;

import com.epam.healenium.model.Locator;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.dto.SelectorRequestDto;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.epam.healenium.util.Utils;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SelectorMapper {

    default Selector dtoToDocument(SelectorRequestDto dto) {
        Selector element = new Selector();
        element.setUid(Utils.buildKey(dto.getClassName(), dto.getLocator(), dto.getUrl()));
        element.setClassName(dto.getClassName());
        element.setMethodName(dto.getMethodName());
        element.setUrl(dto.getUrl());
        element.setLocator(new Locator(dto.getLocator(), dto.getType()));
        element.setNodePathWrapper(new NodePathWrapper(dto.getNodePath()));
        element.setCreatedDate(LocalDateTime.now());
        return element;
    }

}