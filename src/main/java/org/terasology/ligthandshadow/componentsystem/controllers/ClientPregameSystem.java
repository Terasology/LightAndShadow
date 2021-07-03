// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.notify.ui.DialogNotificationOverlay;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientPregameSystem extends BaseComponentSystem {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("LightAndShadow:Timer");

    @In
    private NUIManager nuiManager;

    private DialogNotificationOverlay window;

    @Override
    public void initialise() {
        window = nuiManager.addOverlay(ASSET_URI, DialogNotificationOverlay.class);
        window.addNotification("The game starts in 10 seconds. \n The inventory will be reset.");
    }

    @Override
    public void shutdown() {
        nuiManager.closeScreen(window);
    }
}
