// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.components;

import org.terasology.gestalt.entitysystem.component.Component;

public class LASConfigComponent implements Component<LASConfigComponent> {
    public int maxTeamSizeDifference;
    public int minTeamSize;

    @Override
    public void copyFrom(LASConfigComponent other) {
        this.maxTeamSizeDifference = other.maxTeamSizeDifference;
        this.minTeamSize = other.minTeamSize;
    }
}
