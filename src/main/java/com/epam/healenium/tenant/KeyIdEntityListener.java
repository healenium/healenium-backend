package com.epam.healenium.tenant;

import com.epam.healenium.model.domain.Healing;
import com.epam.healenium.model.domain.HealingResult;
import com.epam.healenium.model.domain.Report;
import jakarta.persistence.PrePersist;

import java.util.UUID;

/**
 * Sets {@code key_id} on insert for reports, healings and healing results.
 * Uses {@link KeyContext#getKeyId()}; a null key id leaves the column null (unknown origin).
 */
public class KeyIdEntityListener {

    @PrePersist
    public void prePersist(Object entity) {
        UUID keyId = KeyContext.getKeyId();
        if (keyId == null) {
            return;
        }
        if (entity instanceof Report report && report.getKeyId() == null) {
            report.setKeyId(keyId);
        } else if (entity instanceof Healing healing && healing.getKeyId() == null) {
            healing.setKeyId(keyId);
        } else if (entity instanceof HealingResult result && result.getKeyId() == null) {
            result.setKeyId(keyId);
        }
    }
}
