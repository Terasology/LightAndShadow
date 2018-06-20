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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.las.UI.ScoreHud;
import org.terasology.ligthandshadow.componentsystem.components.FlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeam;
import org.terasology.ligthandshadow.componentsystem.components.ScoreComponent;
import org.terasology.ligthandshadow.componentsystem.components.WinConditionCheckOnActivateComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.geom.Quat4f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.block.items.BlockItemComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ScoreSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScoreSystem.class);

    @In
    private InventoryManager inventoryManager;

    @In
    private NUIManager nuiManager;

    @In
    private EntityManager entityManager;

    private ControlWidget scoreScreen;
    private EntityRef score;
    private int redScore;
    private int blackScore;
    private UILabel blackScoreArea;
    boolean hasItem;

    @Override
    public void postBegin() {
        // Sets score screen bindings
        scoreScreen = nuiManager.getHUD().getHUDElement("LightAndShadow:ScoreHud");
        blackScoreArea = scoreScreen.find("blackScoreArea", UILabel.class);
        blackScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(blackScore);
            }
        });
    }

    @Override
    public void initialise() {
        // Displays score UI on game start
        nuiManager.getHUD().addHUDElement("ScoreHud");
    }

    @ReceiveEvent(components = {WinConditionCheckOnActivateComponent.class})

    public void onActivate(ActivateEvent event, EntityRef entity) {
        LASTeam baseTeamComponent = entity.getComponent(LASTeam.class);
        EntityRef player = event.getInstigator();
        CharacterHeldItemComponent characterHeldItemComponent = player.getComponent(CharacterHeldItemComponent.class);
        EntityRef heldItem = characterHeldItemComponent.selectedItem;
        hasItem = heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equalsIgnoreCase("LightAndShadowResources:RedFlag");

        //check to see if player has other team's flag
        if (baseTeamComponent.team.equals(baseTeamComponent.RED) && heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString()
                .equalsIgnoreCase("LightAndShadowResources:BlackFlag")) {
            redScore++;
        }

        if (baseTeamComponent.team.equals(baseTeamComponent.BLACK) && heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString()
                .equalsIgnoreCase("LightAndShadowResources:RedFlag")) {
            blackScore++;
        }
    }
}
