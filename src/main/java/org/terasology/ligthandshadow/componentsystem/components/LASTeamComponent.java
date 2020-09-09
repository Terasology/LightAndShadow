// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;

public final class LASTeamComponent implements Component {
    @Replicate(FieldReplicateType.SERVER_TO_CLIENT)
    public String team;

    public LASTeamComponent() {
    }

    public LASTeamComponent(String team) {
        this.team = team;
    }
}
