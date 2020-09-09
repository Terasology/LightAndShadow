// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterHeldItemComponent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.inventory.logic.InventoryManager;
import org.terasology.inventory.logic.events.DropItemRequest;
import org.terasology.inventory.logic.events.InventorySlotChangedEvent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.BlackFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.FlagDropOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.RedFlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.FlagDropEvent;
import org.terasology.ligthandshadow.componentsystem.events.FlagPickupEvent;
import org.terasology.math.geom.Vector3f;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AttackSystem extends BaseComponentSystem {
    @In
    InventoryManager inventoryManager;
    @In
    EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;

    private EntityRef flagSlot;
    private EntityRef item;

    @ReceiveEvent(components = {FlagDropOnActivateComponent.class, PlayerCharacterComponent.class,
            HasFlagComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        dropFlagOnPlayerAttack(event, entity);
    }

    /**
     * When player activates another with the magic staff, checks to see if the attacked player has a flag If so, makes
     * the player drop the flag
     *
     * @param targetPlayer The player being attacked
     */
    private void dropFlagOnPlayerAttack(ActivateEvent event, EntityRef targetPlayer) {
        EntityRef attackingPlayer = event.getInstigator(); // The player using the staff to attack
        if (canPlayerAttack(attackingPlayer)) {
            if (targetPlayer.hasComponent(PlayerCharacterComponent.class) && targetPlayer.hasComponent(HasFlagComponent.class)) {
                // If the target player has the black flag
                if (targetPlayer.getComponent(HasFlagComponent.class).flag.equals(LASUtils.BLACK_TEAM)) {
                    dropFlag(targetPlayer, attackingPlayer, LASUtils.BLACK_FLAG_URI);
                    return;
                }
                if (targetPlayer.getComponent(HasFlagComponent.class).flag.equals(LASUtils.RED_TEAM)) {
                    dropFlag(targetPlayer, attackingPlayer, LASUtils.RED_FLAG_URI);
                    return;
                }
            }
        }
    }

    private void dropFlag(EntityRef targetPlayer, EntityRef attackingPlayer, String flagTeam) {
        int inventorySize = inventoryManager.getNumSlots(targetPlayer);
        for (int slotNumber = 0; slotNumber <= inventorySize; slotNumber++) {
            EntityRef inventorySlot = inventoryManager.getItemInSlot(targetPlayer, slotNumber);
            if (inventorySlot.hasComponent(BlockItemComponent.class)) {
                if (inventorySlot.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equals(flagTeam)) {
                    flagSlot = inventorySlot;
                }
            }
        }
        Vector3f startPosition = new Vector3f(targetPlayer.getComponent(LocationComponent.class).getLocalPosition());
        Vector3f endPosition = new Vector3f(attackingPlayer.getComponent(LocationComponent.class).getLocalPosition());
        Vector3f newPosition = new Vector3f((startPosition.x + endPosition.x) / 2,
                (startPosition.y + endPosition.y) / 2,
                (startPosition.z + endPosition.z) / 2);
        targetPlayer.send(new DropItemRequest(flagSlot, targetPlayer, newPosition, startPosition));
    }

    private boolean canPlayerAttack(EntityRef attackingPlayer) {
        if (!attackingPlayer.hasComponent(CharacterHeldItemComponent.class)) {
            return false;
        }
        EntityRef heldItem = attackingPlayer.getComponent(CharacterHeldItemComponent.class).selectedItem;
        return heldItem.hasComponent(RaycastOnActivateComponent.class);
    }

    /**
     * Checks if player picks up flag of the same team. If so, moves flag back to base, otherwise adds particle emitter
     * and HasFlagComponent to player
     * <p>
     * Otherwise checks if player puts down flag. If so, removes particle emitter and HasFlagComponent from player
     */
    @ReceiveEvent(components = {LASTeamComponent.class})
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity) {
        EntityRef player = entity;

        // Check if player picks up flag
        item = event.getNewItem();
        if (itemIsFlag(item)) {
            String flagTeam = checkWhichFlagPicked(event);
            if (flagTeam.equals(player.getComponent(LASTeamComponent.class).team)) {
                moveFlagToBase(player, flagTeam);
                return;
            } else {
                handleFlagPickup(player, flagTeam);
                return;
            }
        }

        // Checks if player puts down flag
        item = event.getOldItem();
        if (itemIsFlag(item)) {
            handleFlagDrop(player);
        }
    }

    private boolean itemIsFlag(EntityRef checkedItem) {
        return (checkedItem.hasComponent(BlackFlagComponent.class) || checkedItem.hasComponent(RedFlagComponent.class));
    }

    private void handleFlagPickup(EntityRef player, String flagTeam) {
        sendEventToClients(new FlagPickupEvent(player, flagTeam));
        if (!player.hasComponent(HasFlagComponent.class)) {
            player.addComponent(new HasFlagComponent());
            player.getComponent(HasFlagComponent.class).flag = flagTeam;
        }
    }

    private void handleFlagDrop(EntityRef player) {
        if (player.hasComponent(HasFlagComponent.class)) {
            player.removeComponent(HasFlagComponent.class);
        }
        sendEventToClients(new FlagDropEvent(player));
    }

    private void moveFlagToBase(EntityRef playerEntity, String flagTeam) {
        worldProvider.setBlock(LASUtils.getFlagLocation(flagTeam),
                blockManager.getBlock(LASUtils.getFlagURI(flagTeam)));
        inventoryManager.removeItem(playerEntity, EntityRef.NULL, item, true, 1);
    }

    private String checkWhichFlagPicked(InventorySlotChangedEvent event) {
        item = event.getNewItem();
        if (item.hasComponent(BlackFlagComponent.class)) {
            return LASUtils.BLACK_TEAM;
        }
        if (item.hasComponent(RedFlagComponent.class)) {
            return LASUtils.RED_TEAM;
        }
        return null;
    }

    private void sendEventToClients(Event event) {
        if (entityManager.getCountOfEntitiesWith(ClientComponent.class) != 0) {
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            for (EntityRef client : clients) {
                client.send(event);
            }
        }
    }
}
