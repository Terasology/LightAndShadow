// Copyright 2021 The Terasology Foundation
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
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.lightandshadowresources.components.FlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.OnFlagDropEvent;
import org.terasology.ligthandshadow.componentsystem.events.DropFlagEvent;
import org.terasology.ligthandshadow.componentsystem.events.OnFlagPickupEvent;
import org.terasology.ligthandshadow.componentsystem.events.MoveFlagToBaseEvent;
import org.terasology.module.inventory.events.InventorySlotChangedEvent;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.FlagDropOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.lightandshadowresources.components.RaycastOnActivateComponent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AttackSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;

    private EntityRef item;

    @ReceiveEvent(components = {FlagDropOnActivateComponent.class, PlayerCharacterComponent.class, HasFlagComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        dropFlagOnPlayerAttack(event, entity);
    }

    /**
     * When player activates another with the magic staff, checks to see if the attacked player has a flag
     * If so, makes the player drop the flag
     * @param targetPlayer The player being attacked
     */
    private void dropFlagOnPlayerAttack(ActivateEvent event, EntityRef targetPlayer) {
        EntityRef attackingPlayer = event.getInstigator(); // The player using the staff to attack
        if (canPlayerAttack(attackingPlayer)) {
            if (targetPlayer.hasComponent(PlayerCharacterComponent.class) && targetPlayer.hasComponent(HasFlagComponent.class)) {
                // If the target player has the black flag
                if (targetPlayer.getComponent(HasFlagComponent.class).flag.equals(LASUtils.BLACK_TEAM)) {
                    targetPlayer.send(new DropFlagEvent(attackingPlayer, LASUtils.BLACK_FLAG_URI));
                    return;
                }
                if (targetPlayer.getComponent(HasFlagComponent.class).flag.equals(LASUtils.RED_TEAM)) {
                    targetPlayer.send(new DropFlagEvent(attackingPlayer, LASUtils.RED_FLAG_URI));
                    return;
                }
            }
        }
    }

    private boolean canPlayerAttack(EntityRef attackingPlayer) {
        if (!attackingPlayer.hasComponent(CharacterHeldItemComponent.class)) {
            return false;
        }
        EntityRef heldItem = attackingPlayer.getComponent(CharacterHeldItemComponent.class).selectedItem;
        return heldItem.hasComponent(RaycastOnActivateComponent.class);
    }

    /**
     * Checks if player picks up flag of the same team.
     * If so, moves flag back to base, otherwise adds particle emitter and HasFlagComponent to player
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
                player.send(new MoveFlagToBaseEvent(item, flagTeam));
            } else {
                handleFlagPickup(player, flagTeam);
            }
            return;
        }

        // Checks if player puts down flag
        item = event.getOldItem();
        if (itemIsFlag(item)) {
            handleFlagDrop(player);
        }
    }

    private boolean itemIsFlag(EntityRef checkedItem) {
        return checkedItem.hasComponent(FlagComponent.class);
    }

    private String checkWhichFlagPicked(InventorySlotChangedEvent event) {
        item = event.getNewItem();
        if (item.hasComponent(FlagComponent.class)) {
            return item.getComponent(FlagComponent.class).team;
        }
        return null;
    }

    private void handleFlagPickup(EntityRef player, String flagTeam) {
        sendEventToClients(new OnFlagPickupEvent(player, flagTeam));
        if (!player.hasComponent(HasFlagComponent.class)) {
            player.addComponent(new HasFlagComponent());
            player.getComponent(HasFlagComponent.class).flag = flagTeam;
        }
    }

    private void handleFlagDrop(EntityRef player) {
        if (player.hasComponent(HasFlagComponent.class)) {
            player.removeComponent(HasFlagComponent.class);
        }
        sendEventToClients(new OnFlagDropEvent(player));
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
