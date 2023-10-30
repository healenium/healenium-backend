package com.epam.healenium.generator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;

import java.io.Serializable;

public class ReportUUIDGenerator extends UUIDGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Serializable id = (Serializable) session.getEntityPersister(null, object)
                .getClassMetadata().getIdentifier(object, session);
        return id != null ? id : (Serializable) super.generate(session, object);
    }
}
