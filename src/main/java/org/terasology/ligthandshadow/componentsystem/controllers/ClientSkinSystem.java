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
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.AddPlayerSkinToPlayerEvent;
import org.terasology.ligthandshadow.componentsystem.events.SetPlayerHealthHUDEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.hud.HealthHud;
import org.terasology.rendering.nui.widgets.UIIconBar;
import org.terasology.logic.characters.VisualCharacterComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;

/**
 * Handles changing players' health HUD and skin based on their teams.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientSkinSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;


    @Override
    public void initialise() {
        HealthHud healthHud = nuiManager.getHUD().getHUDElement("core:healthHud", HealthHud.class);
        healthHud.find("healthBar", UIIconBar.class).setIcon(Assets.getTextureRegion(LASUtils.getHealthIcon(LASUtils.WHITE_TEAM)).get());
        healthHud.setSkin(Assets.getSkin(LASUtils.getHealthSkin(LASUtils.WHITE_TEAM)).get());
    }

    /**
     * Changes player skin on receiving the corresponding event.
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void onAddPlayerSkinToPlayer(AddPlayerSkinToPlayerEvent event, EntityRef entity) {
        EntityRef player = event.player;
        String team = event.team;
        if (player.hasComponent(VisualCharacterComponent.class)) {
            VisualCharacterComponent visualCharacterComponent = player.getComponent(VisualCharacterComponent.class);
            if (visualCharacterComponent.visualCharacter != EntityRef.NULL && visualCharacterComponent.visualCharacter.hasComponent(SkeletalMeshComponent.class)) {
                SkeletalMeshComponent skeletalMeshComponent = visualCharacterComponent.visualCharacter.getComponent(SkeletalMeshComponent.class);
                if (team.equals(LASUtils.BLACK_TEAM)) {
                    skeletalMeshComponent.material = Assets.getMaterial(LASUtils.BLACK_PAWN_SKIN).get();
                } else if (team.equals(LASUtils.RED_TEAM)) {
                    skeletalMeshComponent.material = Assets.getMaterial(LASUtils.RED_PAWN_SKIN).get();
                }
            }
        }
    }

    /**
     * Changes player health hud on receiving the corresponding event.
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void onSetPlayerHealthHUDEvent(SetPlayerHealthHUDEvent event, EntityRef entity) {
        EntityRef player = event.player;
        String team = event.team;

        if (player.getId() == localPlayer.getCharacterEntity().getId()) {
            HealthHud healthHud = nuiManager.getHUD().getHUDElement("core:healthHud", HealthHud.class);
            healthHud.find("healthBar", UIIconBar.class).setIcon(Assets.getTextureRegion(LASUtils.getHealthIcon(team)).get());
            healthHud.setSkin(Assets.getSkin(LASUtils.getHealthSkin(team)).get());
        }
    }
}
