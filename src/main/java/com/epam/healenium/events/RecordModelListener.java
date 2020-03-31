package com.epam.healenium.events;

import java.util.UUID;

import com.epam.healenium.model.domain.Report;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RecordModelListener extends AbstractMongoEventListener<Report> {

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Report> event) {
        if (StringUtils.isEmpty(event.getSource().getUid())) {
            event.getSource().setUid(UUID.randomUUID().toString());
        }
    }
}