// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.components;

import org.terasology.gestalt.entitysystem.component.Component;

public class LASTeamStatsComponent implements Component<LASTeamStatsComponent> {
    public int redTeamSize;
    public int blackTeamSize;
    public int whiteTeamSize;

    @Override
    public void copyFrom(LASTeamStatsComponent other) {
        this.blackTeamSize = other.blackTeamSize;
        this.redTeamSize = other.redTeamSize;
        this.whiteTeamSize = other.whiteTeamSize;
    }

}
