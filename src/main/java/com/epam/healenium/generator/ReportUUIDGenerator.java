package com.epam.healenium.generator;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.persister.entity.EntityPersister;

import java.io.Serializable;

public class ReportUUIDGenerator extends UUIDGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        EntityPersister persister = session.getEntityPersister(null, object);
        Serializable id = (Serializable) persister.getIdentifier(object, session);
        return id != null ? id : (Serializable) super.generate(session, object);
    }
}
