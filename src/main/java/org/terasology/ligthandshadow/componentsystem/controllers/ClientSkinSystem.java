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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.AddPlayerSkinToPlayerEvent;
import org.terasology.logic.characters.VisualCharacterComponent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientSkinSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;

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
}
