package com.epam.healenium.mapper;

import java.util.Collection;
import java.util.Set;

import com.epam.healenium.model.Locator;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.openqa.selenium.By;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocatorMapper {

    default Locator byToLocator(By by) {
        String[] locatorParts = by.toString().split(":", 2);
        String type = locatorParts[0].trim();
        return new Locator(locatorParts[1].trim(), type.substring(type.indexOf('.') + 1));
    }

    @IterableMapping(elementTargetType = Locator.class)
    Set<Locator> byToLocator(Collection<By> by);

}