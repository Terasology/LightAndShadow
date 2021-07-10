// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.layers.ingame.DeathScreen;


public class StatisticsScreen extends DeathScreen {

    @Override
    public void initialise() {
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return true;
    }
}
