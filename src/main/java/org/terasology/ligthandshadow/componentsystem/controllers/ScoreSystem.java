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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.WinConditionCheckOnActivateComponent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;
import org.terasology.world.generation.WorldRasterizer;

@RegisterSystem(RegisterMode.AUTHORITY)
public class ScoreSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScoreSystem.class);

    @In
    private InventoryManager inventoryManager;
    @In
    private NUIManager nuiManager;
    @In
    private EntityManager entityManager;
    @In
    private BlockManager blockManager;
    @In
    private WorldProvider worldProvider;

    private EntityRef score;
    private int redScore = 0;
    private int blackScore = 0;

    @Override
    public void postBegin() {
        // Sets score screen bindings
        ControlWidget scoreScreen = nuiManager.getHUD().getHUDElement("LightAndShadow:ScoreHud");
        UILabel blackScoreArea = scoreScreen.find("blackScoreArea", UILabel.class);
        blackScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(blackScore);
            }
        });
        UILabel redScoreArea = scoreScreen.find("redScoreArea", UILabel.class);
        redScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(redScore);
            }
        });
    }

    @Override
    public void initialise() {
        // Displays score UI on game start
        nuiManager.getHUD().addHUDElement("ScoreHud");
    }

    @ReceiveEvent(components = {WinConditionCheckOnActivateComponent.class, LASTeamComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        LASTeamComponent baseTeamComponent = entity.getComponent(LASTeamComponent.class);
        EntityRef player = event.getInstigator();
        CharacterHeldItemComponent characterHeldItemComponent = player.getComponent(CharacterHeldItemComponent.class);
        if (characterHeldItemComponent != null) {
            EntityRef heldItem = characterHeldItemComponent.selectedItem;

            // Check to see if player has other team's flag
            if (baseTeamComponent.team.equals(LASUtils.RED_TEAM)
                    && heldItem.hasComponent(BlockItemComponent.class)
                    && heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.BLACK_FLAG_URI)) {
                redScore++;
                inventoryManager.removeItem(player, player, heldItem, true);
                worldProvider.setBlock(new Vector3i(LASUtils.CENTER_BLACK_BASE_POSITION.x, LASUtils.CENTER_BLACK_BASE_POSITION.y + 1, LASUtils.CENTER_BLACK_BASE_POSITION.z), blockManager.getBlock(LASUtils.BLACK_FLAG_URI));
            }

            if (baseTeamComponent.team.equals(LASUtils.BLACK_TEAM)
                    && heldItem.hasComponent(BlockItemComponent.class)
                    && heldItem.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(LASUtils.RED_FLAG_URI)) {
                blackScore++;
                inventoryManager.removeItem(player, player, heldItem, true);
                worldProvider.setBlock(new Vector3i(LASUtils.CENTER_RED_BASE_POSITION.x, LASUtils.CENTER_RED_BASE_POSITION.y + 1, LASUtils.CENTER_RED_BASE_POSITION.x), blockManager.getBlock(LASUtils.RED_FLAG_URI));
            }
        }
    }
}
