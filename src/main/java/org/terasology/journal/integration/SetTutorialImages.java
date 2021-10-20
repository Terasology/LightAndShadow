// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.journal.integration;

import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.journal.ui.JournalNUIWindow;
import org.terasology.nui.widgets.UIImage;

public class SetTutorialImages extends JournalNUIWindow {

    @In
    AssetManager assetManager;
    private UIImage fetchFlag;
    private UIImage deliverFlag;
    private UIImage inventory;
    private UIImage statistics;

    @Override
    public void initialise() {
        fetchFlag = find("fetchFlag", UIImage.class);
        deliverFlag = find("deliverFlag", UIImage.class);
        inventory = find("openInventory", UIImage.class);
        statistics = find("openStatistics", UIImage.class);
        super.initialise();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void setImages() {
        Texture texture = assetManager.getAsset("LightAndShadow:FetchFlag", Texture.class).get();
        fetchFlag.setImage(texture);
        texture = assetManager.getAsset("LightAndShadow:DeliverFlag", Texture.class).get();
        deliverFlag.setImage(texture);
        texture = assetManager.getAsset("LightAndShadow:OpenStatistics", Texture.class).get();
        statistics.setImage(texture);
        texture = assetManager.getAsset("LightAndShadow:OpenInventory", Texture.class).get();
        inventory.setImage(texture);
    }
}
