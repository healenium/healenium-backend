package com.epam.healenium.util;

import lombok.experimental.UtilityClass;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

@UtilityClass
public class JpaTools {

    private static final JoinType DEFAULT_JOIN_TYPE = JoinType.INNER;

    /**
     * Check if query is for counting
     *
     * @param query criteria query to check
     * @return true if for count
     */
    public boolean isCountQuery(CriteriaQuery<?> query){
        return query.getResultType() == Long.class || query.getResultType() == long.class;
    }

    /**
     *
     * @param root
     * @param attribute
     * @param joinType
     * @param <T>
     */
    public <T> void fetchOnce(Root<T> root, SetAttribute<? super T, ?> attribute, JoinType joinType){
        for (Fetch<T, ?> fetch : root.getFetches()) {
            if(fetch.getAttribute().equals(attribute)){
                return;
            }
        }
        root.fetch(attribute, joinType);
    }

    /**
     *
     * @param root
     * @param attribute
     * @param joinType
     * @param <T>
     */
    public <T> void fetchOnce(Root<T> root, SingularAttribute<? super T, ?> attribute, JoinType joinType){
        for (Fetch<T, ?> fetch : root.getFetches()) {
            if(fetch.getAttribute().equals(attribute)){
                return;
            }
        }
        root.fetch(attribute, joinType);
    }

    /**
     *
     * @param root
     * @param attribute
     * @param <T>
     */
    public <T> void fetchOnce(Root<T> root, SingularAttribute<? super T, ?> attribute){
        fetchOnce(root, attribute, JoinType.LEFT);
    }

    /**
     *
     * @param root
     * @param attribute
     * @param <T>
     */
    public <T> void fetchOnce(Root<T> root, SetAttribute<? super T, ?> attribute){
        fetchOnce(root, attribute, JoinType.LEFT);
    }

    public <ROOT, JOIN> Join<ROOT, JOIN> getJoin(From<?, ROOT> root, SetAttribute<ROOT, JOIN> attribute){
        return getJoinWithType(root, attribute, DEFAULT_JOIN_TYPE);
    }

    public <ROOT, JOIN> Join<ROOT, JOIN> getLeftJoin(From<?, ROOT> root, SetAttribute<ROOT, JOIN> attribute){
        return getJoinWithType(root, attribute, JoinType.LEFT);
    }

    public <ROOT, JOIN> Join<ROOT, JOIN> getJoin(From<?, ROOT> root, SingularAttribute<ROOT, JOIN> attribute){
        return getJoinWithType(root, attribute, DEFAULT_JOIN_TYPE);
    }

    public <ROOT, JOIN> Join<ROOT, JOIN> getLeftJoin(From<?, ROOT> root, SingularAttribute<ROOT, JOIN> attribute){
        return getJoinWithType(root, attribute, JoinType.LEFT);
    }

    @SuppressWarnings("unchecked")
    private <ROOT, JOIN> Join<ROOT, JOIN> getJoinWithType(From<?, ROOT> root, SetAttribute<ROOT, JOIN> attribute, JoinType joinType){
        for (Join<ROOT, ?> join : root.getJoins()) {
            if(join.getAttribute().equals(attribute)){
                return (Join<ROOT, JOIN>) join;
            }
        }
        return root.join(attribute, joinType);
    }

    @SuppressWarnings("unchecked")
    private <ROOT, JOIN> Join<ROOT, JOIN> getJoinWithType(From<?, ROOT> root, SingularAttribute<ROOT, JOIN> attribute, JoinType joinType){
        for (Join<ROOT, ?> join : root.getJoins()) {
            if(join.getAttribute().equals(attribute)){
                return (Join<ROOT, JOIN>) join;
            }
        }
        return root.join(attribute, joinType);
    }

}
