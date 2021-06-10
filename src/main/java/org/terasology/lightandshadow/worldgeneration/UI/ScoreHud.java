// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.worldgeneration.UI;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.lightandshadow.gamestate.components.ScoreComponent;
import org.terasology.nui.widgets.UIText;

public class ScoreHud extends CoreHudWidget {

    @In
    private EntityManager entityManager;

    private UIText scoreArea;
    private EntityRef scoreEntity;
    private ScoreComponent score;

    @Override
    public void initialise() {

    }
}
