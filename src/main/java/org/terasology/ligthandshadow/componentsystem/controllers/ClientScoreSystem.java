/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.las.UI.ScoreHud;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.ScoreUpdateFromServerEvent;
import org.terasology.math.geom.Rect2f;
import org.terasology.network.ClientComponent;
import org.terasology.network.events.PingFromClientEvent;
import org.terasology.network.events.PingFromServerEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientScoreSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;

    @Override
    public void postBegin() {
        // Sets score screen bindings
        ControlWidget scoreScreen = nuiManager.getHUD().getHUDElement("LightAndShadow:ScoreHud");
        UILabel blackScoreArea = scoreScreen.find("blackScoreArea", UILabel.class);
        blackScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(LASUtils.GOAL_SCORE);
            }
        });
        UILabel redScoreArea = scoreScreen.find("redScoreArea", UILabel.class);
        redScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(LASUtils.GOAL_SCORE);
            }
        });
    }

    @Override
    public void initialise() {
        // Displays score UI to client on game start
        nuiManager.getHUD().addHUDElement("ScoreHud");
    }
//    @ReceiveEvent(components = ClientComponent.class)
//    public void onScoreUpdateFromServer(ScoreUpdateFromServerEvent event, EntityRef entity) {
//        ClientComponent client = entity.getComponent(ClientComponent.class);
//        if (client.local) {
//
//        }
//    }
}
