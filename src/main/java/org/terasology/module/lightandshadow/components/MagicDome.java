// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.components;

import org.terasology.engine.audio.StaticSound;
import org.terasology.gestalt.entitysystem.component.Component;

public class MagicDome implements Component<MagicDome> {
    public StaticSound hitSound;
    public String team;

    @Override
    public void copyFrom(MagicDome other) {
        this.hitSound = other.hitSound;
        this.team = other.team;
    }
}
