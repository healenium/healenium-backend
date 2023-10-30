package com.epam.healenium.specification;

import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.Healing_;
import com.epam.healenium.model.domain.Selector;
import com.epam.healenium.model.domain.Selector_;
import com.epam.healenium.model.dto.RequestDto;
import com.epam.healenium.util.JpaTools;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;



@UtilityClass
public class HealingSpecBuilder {

    public Specification<Healing> buildSpec(RequestDto filter){
        Specification<Healing> spec = Specification.where(null);

        if(!StringUtils.isEmpty(filter.getLocator())){
            spec = spec.and(HealingSpecifications.hasLocator(filter.getLocator()));
        }

        if(!StringUtils.isEmpty(filter.getMethodName())){
            spec = spec.and(HealingSpecifications.hasMethod(filter.getMethodName()));
        }

        if(!StringUtils.isEmpty(filter.getClassName())){
            spec = spec.and(HealingSpecifications.hasClass(filter.getClassName()));
        }
        return spec;
    }

    private class HealingSpecifications {

        public static Specification<Healing> hasClass(final String className){
            return (root, query, cb) -> {
                query.distinct(true);
                Join<Healing, Selector> selector = JpaTools.getJoin(root, Healing_.selector);
                return cb.equal(selector.get(Selector_.className), className);
            };
        }

        public static Specification<Healing> hasMethod(final String methodName){
            return (root, query, cb) -> {
                query.distinct(true);
                Join<Healing, Selector> selector = JpaTools.getJoin(root, Healing_.selector);
                return cb.equal(selector.get(Selector_.methodName), methodName);
            };
        }

        public static Specification<Healing> hasLocator(final String locator){
            return (root, query, cb) -> {
                query.distinct(true);
                Join<Healing, Selector> selector = JpaTools.getJoin(root, Healing_.selector);
                return cb.like(
                        cb.function("json_extract_path_text", String.class, selector.get(Selector_.locator), cb.literal("value")),
                        "%"+ locator + "%"
                );
            };
        }

    }

}
