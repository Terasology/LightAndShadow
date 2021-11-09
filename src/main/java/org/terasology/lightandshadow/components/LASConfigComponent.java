// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.components;

import org.terasology.gestalt.entitysystem.component.Component;

public class LASConfigComponent implements Component<LASConfigComponent> {
    public int maxTeamSizeDifference;

    @Override
    public void copyFrom(LASConfigComponent other) {
        this.maxTeamSizeDifference = other.maxTeamSizeDifference;
    }
}
