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

import org.terasology.assets.management.AssetManager;
import org.terasology.engine.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.ScoreComponent;
import org.terasology.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.HealthHud;
import org.terasology.rendering.nui.widgets.UIIconBar;
import org.terasology.logic.characters.VisualCharacterComponent;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.widgets.UILabel;
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
    @In
    private AssetManager assetManager;
    @In
    private LASGlobalSystem lasGlobalSystem;

    /**
     * Change the Health HUD when the local player is spawned based on their Light and Shadow Team.
     * @see LASTeamComponent
     *
     * @param event            The event that is triggered when local player has been spawned
     * @param characterEntity  The character entity belonging to local player
     * @param lasTeamComponent The Light and Shadow team component of local player
     */
    @ReceiveEvent
    public void onAwaitedLocalCharacterSpawnEvent(AwaitedLocalCharacterSpawnEvent event, EntityRef characterEntity,
                                                  LASTeamComponent lasTeamComponent) {
        setHealthHUD(lasTeamComponent.team);
        setScoreHUD();
    }

    /**
     * Updates the skeletal mesh of a player when its visual character is being created.
     * Default event handler for this event has Trivial priority. Hence, this method catches the event first
     * and consumes it.
     * @see CreateVisualCharacterEvent
     * @see org.terasology.logic.characters.VisualCharacterSystem
     *
     * @param event
     * @param characterEntity
     * @param lasTeamComponent
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
     * @see LASTeamComponent
     *
     * @param event
     * @param characterEntity
     * @param lasTeamComponent
     */
    @ReceiveEvent
    public void onLASTeamChange(OnChangedComponent event, EntityRef characterEntity, LASTeamComponent lasTeamComponent) {
        if (characterEntity.hasComponent(VisualCharacterComponent.class)) {
            VisualCharacterComponent visualCharacterComponent = characterEntity.getComponent(VisualCharacterComponent.class);
            EntityRef visualCharacter = visualCharacterComponent.visualCharacter;
            if (visualCharacter != EntityRef.NULL && visualCharacter.hasComponent(SkeletalMeshComponent.class)) {
                SkeletalMeshComponent skeletalMeshComponent = visualCharacter.getComponent(SkeletalMeshComponent.class);
                skeletalMeshComponent.material = Assets.getMaterial(LASUtils.getPlayerSkin(lasTeamComponent.team)).get();
                visualCharacter.saveComponent(skeletalMeshComponent);
            }
        }
        if (characterEntity.getOwner().equals(localPlayer.getClientEntity())) {
            setHealthHUD(lasTeamComponent.team);
        }
    }

    private void setHealthHUD(String team) {
        HealthHud healthHud = nuiManager.getHUD().getHUDElement("core:healthHud", HealthHud.class);
        healthHud.find("healthBar", UIIconBar.class).setIcon(Assets.getTextureRegion(LASUtils.getHealthIcon(team)).get());
        healthHud.setSkin(Assets.getSkin(LASUtils.getHealthSkin(team)).get());
    }

    private void setScoreHUD() {
        EntityRef globalEntity = lasGlobalSystem.getOrCreateGlobalEntity();
        ScoreComponent scoreComponent = globalEntity.getComponent(ScoreComponent.class);
        ControlWidget scoreScreen =  nuiManager.getHUD().addHUDElement("ScoreHud");
        UILabel blackScoreArea = scoreScreen.find("blackScoreArea", UILabel.class);
        blackScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(scoreComponent.blackScore);
            }
        });
        UILabel redScoreArea = scoreScreen.find("redScoreArea", UILabel.class);
        redScoreArea.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return String.valueOf(scoreComponent.redScore);
            }
        });
    }
}
