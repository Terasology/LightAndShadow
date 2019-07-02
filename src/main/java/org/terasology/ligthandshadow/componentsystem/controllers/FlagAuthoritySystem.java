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
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;

/**
 * Handles events related to flag drops and pickups.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class FlagAuthoritySystem extends BaseComponentSystem {

    @In
    DelayManager delayManager;

    @In
    WorldProvider worldProvider;

    @In
    BlockManager blockManager;

    /**
     * Add a delayed action using delay manager to flags when they are dropped.
     * Priority is kept low because we want this handler to be triggered after the default handler of DropItem event to
     * be triggered first and let the LifespanComponent to be copied first.
     *
     * @see org.terasology.logic.inventory.ItemPickupAuthoritySystem
     * @see DelayManager
     *
     * @param event
     * @param itemEntity
     * @param itemComponent
     * @param blockItemComponent
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onDropItemEvent(DropItemEvent event, EntityRef itemEntity, ItemComponent itemComponent,
                                BlockItemComponent blockItemComponent) {
        String blockFamilyURI = blockItemComponent.blockFamily.getURI().toString();
        if (blockFamilyURI.equals(LASUtils.BLACK_FLAG_URI) || blockFamilyURI.equals(LASUtils.RED_FLAG_URI)) {
            itemEntity.removeComponent(LifespanComponent.class);
            delayManager.addDelayedAction(itemEntity, LASUtils.DROPPED_FLAG, LASUtils.FLAG_TELEPORT_DELAY);
        }
    }
    /**
     * Destroy dropped items except flag after the delay set using delay manager.
     * Flag is teleported back to the base.
     * @see DelayManager
     * @see DelayedActionTriggeredEvent
     *
     * @param event     The event which is triggered once the delay is over
     * @param entity    The item on which the delay was set
     */
    @ReceiveEvent
    public void destroyDroppedItems(DelayedActionTriggeredEvent event, EntityRef entity,
                                    BlockItemComponent blockItemComponent) {
        if (event.getActionId().equals(LASUtils.DROPPED_FLAG)) {
            String blockFamilyURI = blockItemComponent.blockFamily.getURI().toString();
            entity.destroy();
            if (blockFamilyURI.equals(LASUtils.BLACK_FLAG_URI)) {
                worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.BLACK_TEAM),
                        blockManager.getBlock(LASUtils.getFlagURI(LASUtils.BLACK_TEAM)));
            }
            if (blockFamilyURI.equals(LASUtils.RED_FLAG_URI)) {
                worldProvider.setBlock(LASUtils.getFlagLocation(LASUtils.RED_TEAM),
                        blockManager.getBlock(LASUtils.getFlagURI(LASUtils.RED_TEAM)));
            }
        }
    }

    /**
     * Remove delay action on flag if it is picked up.
     * Priority is kept low because we want this handler to be triggered after the default handler of GiveItem event to
     * be triggered first and check if the event was handled or not.
     *
     * @param event
     * @param item
     * @param itemComponent
     * @param blockItemComponent
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onGiveItemToCharacterHoldItem(GiveItemEvent event, EntityRef item, ItemComponent itemComponent,
                                              BlockItemComponent blockItemComponent) {
        if (event.isHandled() && delayManager.hasDelayedAction(item, LASUtils.DROPPED_FLAG)) {
                delayManager.cancelDelayedAction(item, LASUtils.DROPPED_FLAG);
        }
    }
}
