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
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.SetTeamOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.events.AddPlayerSkinToPlayerEvent;
import org.terasology.logic.characters.VisualCharacterComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.utilities.Assets;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientSkinSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private AssetManager assetManager;

    private static final Logger logger = LoggerFactory.getLogger(ClientSkinSystem.class);

    @ReceiveEvent(components = {SetTeamOnActivateComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef player = event.getInstigator();
        String team = LASUtils.BLACK_TEAM;
        handlePlayerTeleport(player, team);
    }

    private void handlePlayerTeleport(EntityRef player, String team) {
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

    //@ReceiveEvent
    //public void onAddPlayerSkinToPlayer(AddPlayerSkinToPlayerEvent event, EntityRef entity) {
//        String team = event.team;
//        if (entity.hasComponent(VisualCharacterComponent.class)) {
//            VisualCharacterComponent visualCharacterComponent = entity.getComponent(VisualCharacterComponent.class);
//            if (visualCharacterComponent.visualCharacter != EntityRef.NULL && visualCharacterComponent.visualCharacter.hasComponent(SkeletalMeshComponent.class)) {
//                SkeletalMeshComponent skeletalMeshComponent = visualCharacterComponent.visualCharacter.getComponent(SkeletalMeshComponent.class);
//                if (team.equals(LASUtils.BLACK_TEAM)) {
//                    skeletalMeshComponent.material = Assets.getMaterial(LASUtils.BLACK_PAWN_SKIN).get();
//                } else if (team.equals(LASUtils.RED_TEAM)) {
//                    skeletalMeshComponent.material = Assets.getMaterial(LASUtils.RED_PAWN_SKIN).get();
//                }
//            }
//        }
//    }
}
