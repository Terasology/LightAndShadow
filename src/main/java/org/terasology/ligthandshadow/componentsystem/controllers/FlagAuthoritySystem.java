// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.lifespan.LifespanComponent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.events.FlagDropRequestEvent;
import org.terasology.ligthandshadow.componentsystem.events.GiveFlagEvent;
import org.terasology.ligthandshadow.componentsystem.events.MoveFlagToBaseEvent;
import org.terasology.module.inventory.events.DropItemRequest;
import org.terasology.module.inventory.systems.InventoryManager;

/**
 * Handles events related to flag drops and pickups.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = FlagAuthoritySystem.class)
public class FlagAuthoritySystem extends BaseComponentSystem {

    @In
    DelayManager delayManager;

    @In
    WorldProvider worldProvider;

    @In
    BlockManager blockManager;

    @In
    InventoryManager inventoryManager;

    @In
    EntityManager entityManager;

    private EntityRef flagSlot;

    /**
     * Add a delayed action using delay manager to flags when they are dropped.
     * Priority is kept low because we want this handler to be triggered after the default handler of DropItem event to
     * be triggered first and let the LifespanComponent to be copied first.
     *
     * @see org.terasology.engine.logic.inventory.ItemPickupAuthoritySystem
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

    @ReceiveEvent
    public void dropFlagRequest(FlagDropRequestEvent event, EntityRef targetPlayer) {
        int inventorySize = inventoryManager.getNumSlots(targetPlayer);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef inventorySlot = inventoryManager.getItemInSlot(targetPlayer, slotNumber);
            if (inventorySlot.hasComponent(BlockItemComponent.class)) {
                if (inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(event.flagTeam)) {
                    flagSlot = inventorySlot;
                }
            }
        }
        Vector3fc startPosition = targetPlayer.getComponent(LocationComponent.class).getLocalPosition();
        Vector3f impulse = event.attackingPlayer.getComponent(LocationComponent.class).getLocalPosition()
                .add(startPosition, new Vector3f()).div(2f);
        targetPlayer.send(new DropItemRequest(flagSlot, targetPlayer, impulse, startPosition));
    }

    @ReceiveEvent
    public void moveFlagToBase(MoveFlagToBaseEvent event, EntityRef playerEntity) {
        worldProvider.setBlock(LASUtils.getFlagLocation(event.flagTeam), blockManager.getBlock(LASUtils.getFlagURI(event.flagTeam)));
        inventoryManager.removeItem(playerEntity, EntityRef.NULL, event.heldFlag, true, 1);
    }

    @ReceiveEvent
    public void giveFlagToPlayer(GiveFlagEvent event, EntityRef player) {
        BlockComponent blockComponent = event.flag.getComponent(BlockComponent.class);
        LASTeamComponent flagTeamComponent = event.flag.getComponent(LASTeamComponent.class);
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily(LASUtils.getFlagURI(flagTeamComponent.team))));
        worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
        event.flag.destroy();
    }

}
