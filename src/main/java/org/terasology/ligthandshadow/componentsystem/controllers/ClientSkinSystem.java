// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.core.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.VisualCharacterComponent;
import org.terasology.engine.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.health.rendering.nui.layers.hud.HealthHud;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.nui.widgets.UIIconBar;

/**
 * Handles changing players' health HUD and skin based on their teams.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ClientSkinSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private AssetManager assetManager;

    /**
     * Change the Health HUD when the local player is spawned based on their Light and Shadow Team.
     *
     * @param event The event that is triggered when local player has been spawned
     * @param characterEntity The character entity belonging to local player
     * @param lasTeamComponent The Light and Shadow team component of local player
     * @see LASTeamComponent
     */
    @ReceiveEvent
    public void onAwaitedLocalCharacterSpawnEvent(AwaitedLocalCharacterSpawnEvent event, EntityRef characterEntity,
                                                  LASTeamComponent lasTeamComponent) {
        setHealthHUD(lasTeamComponent.team);
    }

    /**
     * Updates the skeletal mesh of a player when its visual character is being created. Default event handler for this
     * event has Trivial priority. Hence, this method catches the event first and consumes it.
     *
     * @param event
     * @param characterEntity
     * @param lasTeamComponent
     * @see CreateVisualCharacterEvent
     * @see org.terasology.logic.characters.VisualCharacterSystem
     */
    @ReceiveEvent
    public void onCreateDefaultVisualCharacter(CreateVisualCharacterEvent event, EntityRef characterEntity,
                                               LASTeamComponent lasTeamComponent) {
        Prefab prefab = assetManager.getAsset("engine:defaultVisualCharacter", Prefab.class).get();
        EntityBuilder entityBuilder = event.getVisualCharacterBuilder();
        entityBuilder.addPrefab(prefab);
        SkeletalMeshComponent skeletalMeshComponent = entityBuilder.getComponent(SkeletalMeshComponent.class);
        skeletalMeshComponent.material = Assets.getMaterial(LASUtils.getPlayerSkin(lasTeamComponent.team)).get();
        entityBuilder.saveComponent(skeletalMeshComponent);
        event.consume();
    }

    /**
     * Updates the skeletal mesh of a player when its team changes.
     *
     * @param event
     * @param characterEntity
     * @param lasTeamComponent
     * @see LASTeamComponent
     */
    @ReceiveEvent
    public void onLASTeamChange(OnChangedComponent event, EntityRef characterEntity,
                                LASTeamComponent lasTeamComponent) {
        if (characterEntity.hasComponent(VisualCharacterComponent.class)) {
            VisualCharacterComponent visualCharacterComponent =
                    characterEntity.getComponent(VisualCharacterComponent.class);
            EntityRef visualCharacter = visualCharacterComponent.visualCharacter;
            if (visualCharacter != EntityRef.NULL && visualCharacter.hasComponent(SkeletalMeshComponent.class)) {
                SkeletalMeshComponent skeletalMeshComponent = visualCharacter.getComponent(SkeletalMeshComponent.class);
                skeletalMeshComponent.material =
                        Assets.getMaterial(LASUtils.getPlayerSkin(lasTeamComponent.team)).get();
                visualCharacter.saveComponent(skeletalMeshComponent);
            }
        }
        if (characterEntity.getOwner().equals(localPlayer.getClientEntity())) {
            setHealthHUD(lasTeamComponent.team);
        }
    }

    private void setHealthHUD(String team) {
        HealthHud healthHud = nuiManager.getHUD().getHUDElement("Health:healthHud", HealthHud.class);
        healthHud.find("healthBar", UIIconBar.class).setIcon(Assets.getTextureRegion(LASUtils.getHealthIcon(team)).get());
        healthHud.setSkin(Assets.getSkin(LASUtils.getHealthSkin(team)).get());
    }
}
