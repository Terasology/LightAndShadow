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
import org.terasology.ligthandshadow.componentsystem.components.LASTeam;
import org.terasology.ligthandshadow.componentsystem.components.TakeBlockOnActivateComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryAuthoritySystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;


@RegisterSystem(RegisterMode.AUTHORITY)
public class TakeBlockOnActivationSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;


    private static final Logger LOG = LoggerFactory.getLogger(TakeBlockOnActivationSystem.class);

    @ReceiveEvent(components = {TakeBlockOnActivateComponent.class, BlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        inventoryManager = new InventoryAuthoritySystem();

        BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
        LASTeam flagTeamComponent = entity.getComponent(LASTeam.class);


        EntityRef flagTaker = event.getInstigator();
        LASTeam playerTeamComponent = flagTaker.getComponent(LASTeam.class);
        //if the flag being taken is a red flag and the player is on the black team, let them take the flag
        if (flagTeamComponent.team.equals("red") && playerTeamComponent.team.equals("black")) {
            inventoryManager.giveItem(flagTaker, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("LightAndShadowResources:redFlag")));
            worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
            entity.destroy();
        }
        if (flagTeamComponent.team.equals("black") && playerTeamComponent.team.equals("red")) {
            inventoryManager.giveItem(flagTaker, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily("LightAndShadowResources:blackFlag")));
            worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
            entity.destroy();
        }
    }
}
