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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.BlackFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.FlagDropOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.FlagParticleComponent;
import org.terasology.ligthandshadow.componentsystem.components.HasFlagComponent;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.components.RaycastOnActivateComponent;
import org.terasology.ligthandshadow.componentsystem.components.RedFlagComponent;
import org.terasology.ligthandshadow.componentsystem.events.FlagDropEvent;
import org.terasology.ligthandshadow.componentsystem.events.FlagPickupEvent;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemComponent;

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

    @ReceiveEvent(components = {FlagDropOnActivateComponent.class, PlayerCharacterComponent.class, HasFlagComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        dropFlagOnPlayerAttack(event, entity);
    }

    /**
     * When player activates another with the magic staff, checks to see if the attacked player has a flag
     * If so, makes the player drop the flag
     * targetPlayer is player being attacked
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

    private void removeParticleEmitterFromPlayer(EntityRef player) {
        if (player.hasComponent(FlagParticleComponent.class)) {
            EntityRef particleEntity = player.getComponent(FlagParticleComponent.class).particleEntity;
            if (particleEntity != EntityRef.NULL) {
                particleEntity.destroy();
            }
            player.removeComponent(FlagParticleComponent.class);
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
        Vector3f position = new Vector3f(targetPlayer.getComponent(LocationComponent.class).getLocalPosition());
        Vector3f impulseVector = new Vector3f(attackingPlayer.getComponent(LocationComponent.class).getLocalPosition());
        targetPlayer.send(new DropItemRequest(flagSlot, targetPlayer, impulseVector, position));
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
     *
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
        player.addComponent(new HasFlagComponent(flagTeam));
        attachParticleEmitterToPlayer(player, flagTeam);
        sendEventToClients(new FlagPickupEvent(player, flagTeam));
    }

    private void handleFlagDrop(EntityRef player) {
        sendEventToClients(new FlagDropEvent(player));
        removeParticleEmitterFromPlayer(player);
        player.removeComponent(HasFlagComponent.class);
    }

    private void attachParticleEmitterToPlayer(EntityRef target, String flagTeam) {
        if (target.exists()) {
            EntityRef particleEntity = entityManager.create(LASUtils.getFlagParticle(flagTeam));
            FlagParticleComponent particleComponent = new FlagParticleComponent(particleEntity);

            LocationComponent targetLoc = target.getComponent(LocationComponent.class);
            LocationComponent childLoc = particleEntity.getComponent(LocationComponent.class);
            childLoc.setWorldPosition(targetLoc.getWorldPosition());
            Location.attachChild(target, particleEntity);
            particleEntity.setOwner(target);

            target.addOrSaveComponent(particleComponent);
        }
    }

    private void moveFlagToBase(EntityRef playerEntity, String flagTeam) {
        worldProvider.setBlock(LASUtils.getFlagLocation(flagTeam), blockManager.getBlock(LASUtils.getFlagURI(flagTeam)));
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
