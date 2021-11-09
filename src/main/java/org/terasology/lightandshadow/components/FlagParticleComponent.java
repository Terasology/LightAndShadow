// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.components;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

public class FlagParticleComponent implements Component<FlagParticleComponent> {
    public EntityRef particleEntity = EntityRef.NULL;

    @Override
    public void copyFrom(FlagParticleComponent other) {
        this.particleEntity = other.particleEntity;
    }
}
