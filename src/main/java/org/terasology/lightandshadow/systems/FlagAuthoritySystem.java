// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.systems;

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
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.lightandshadowresources.components.FlagComponent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadow.LASUtils;
import org.terasology.lightandshadow.events.DropFlagEvent;
import org.terasology.lightandshadow.events.GiveFlagEvent;
import org.terasology.lightandshadow.events.ReturnFlagEvent;
import org.terasology.module.inventory.events.DropItemRequest;
import org.terasology.module.inventory.systems.InventoryManager;

import java.util.Optional;

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
    /**
     * Iterates through the inventory to locate the flag and then sends a {@link DropItemRequest} to remove the flag from
     * the player's inventory.
     *
     * @param event The FlagDrop event sent to the player.
     * @param targetPlayer The entity which should drop the flag.
     */
    @ReceiveEvent
    public void dropFlagRequest(DropFlagEvent event, EntityRef targetPlayer) {
        int inventorySize = inventoryManager.getNumSlots(targetPlayer);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef inventorySlot = inventoryManager.getItemInSlot(targetPlayer, slotNumber);
            Optional<FlagComponent> flagComponent = Optional.ofNullable(inventoryManager.getItemInSlot(targetPlayer,
                    slotNumber).getComponent(FlagComponent.class));
            flagComponent.ifPresent(flag -> {
                if (flag.team.equals(LASUtils.getOppositionTeam(targetPlayer.getComponent(LASTeamComponent.class).team))) {
                    flagSlot = inventorySlot;
                }
            });
        }
        Vector3fc startPosition = targetPlayer.getComponent(LocationComponent.class).getLocalPosition();
        Vector3f impulse = event.getAttackingPlayer().getComponent(LocationComponent.class).getLocalPosition()
                .add(startPosition, new Vector3f()).div(2f);
        targetPlayer.send(new DropItemRequest(flagSlot, targetPlayer, impulse, startPosition));
    }

    /**
     * Places the flag at the base and removes the flag from the players inventory.
     *
     * @param event The ReturnFlag event sent to the player.
     * @param playerEntity The entity from which the flag should be removed.
     */
    @ReceiveEvent
    public void moveFlagToBase(ReturnFlagEvent event, EntityRef playerEntity) {
        String flagTeam = event.getFlag().getComponent(FlagComponent.class).team;
        worldProvider.setBlock(LASUtils.getFlagLocation(flagTeam), blockManager.getBlock(LASUtils.getFlagURI(flagTeam)));
        inventoryManager.removeItem(playerEntity, EntityRef.NULL, event.getFlag(), true, 1);
    }

    /**
     * Gives the flag to the player triggering the event by adding it to their inventory and then removes the flag from the base.
     *
     * @param event The GiveFlag event sent to the player.
     * @param player The entity that is taking the flag.
     */
    @ReceiveEvent
    public void giveFlagToPlayer(GiveFlagEvent event, EntityRef player) {
        BlockComponent blockComponent = event.getFlag().getComponent(BlockComponent.class);
        FlagComponent flagComponent = event.getFlag().getComponent(FlagComponent.class);
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(
                blockManager.getBlockFamily(LASUtils.getFlagURI(flagComponent.team))));
        worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
        event.getFlag().destroy();
    }

}
