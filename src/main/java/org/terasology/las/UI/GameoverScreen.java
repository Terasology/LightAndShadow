/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.las.UI;

import org.terasology.engine.GameEngine;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * This screen is displayed when a round of game gets over.
 */
public class GameoverScreen extends CoreScreenLayer {
    private UILabel gameoverDetails;

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public void initialise() {
        gameoverDetails = find("gameoverDetails", UILabel.class);
        WidgetUtil.trySubscribe(this, "restart", widget -> getManager().closeScreen(GameoverScreen.this)); //TODO send restart event
        WidgetUtil.trySubscribe(this, "settings", widget -> getManager().pushScreen("settingsMenuScreen"));
        WidgetUtil.trySubscribe(this, "exitGame", widget -> CoreRegistry.get(GameEngine.class).shutdown());
    }

    public void setGameoverDetails(String result) {
        gameoverDetails.setText(String.format("You %s !", result));
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
