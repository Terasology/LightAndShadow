// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.economy.components.CurrencyStorageComponent;
import org.terasology.economy.events.WalletUpdatedEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.events.RemoveItemAction;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.events.PlayerExitedArenaEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.SwitchToPhaseEvent;
import org.terasology.module.lightandshadow.phases.authority.PhaseSystem;

@RegisterSystem
public class LASSystem extends BaseComponentSystem {
    @In
    private InventoryManager inventoryManager;
    @In
    private CelestialSystem celestialSystem;
    @In
    private GameEntitySystem gameEntitySystem;
    @In
    private PhaseSystem phaseSystem;
    @In
    private TeamSystem teamSystem;

    private static final Logger logger = LoggerFactory.getLogger(LASSystem.class);

    /**
     * Gives an empty inventory to a player in the lobby to prevent fight's in the lobby and gives the player some funds.
     */
    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
    public void onPlayerSpawn(OnPlayerSpawnedEvent event, EntityRef player, InventoryComponent inventory) {
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef itemInSlot = inventoryManager.getItemInSlot(player, i);
            player.send(new RemoveItemAction(player, itemInSlot, true));
        }
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        player.getComponent(CurrencyStorageComponent.class).amount = gameEntity.getComponent(CurrencyStorageComponent.class).amount;
        player.send(new WalletUpdatedEvent(gameEntity.getComponent(CurrencyStorageComponent.class).amount));
        player.send(new CharacterTeleportEvent(new Vector3f(LASUtils.FLOATING_PLATFORM_POSITION).add(0, 1, 0)));
    }

    /**
     * Follow-up actions necessary when a player leaves the arena, either by porting back to the platform
     * or by disconnecting from the server.
     * These action are required to verify the current game state and update the phase accordingly if necessary.
     */
    @ReceiveEvent
    public void onArenaExit(PlayerExitedArenaEvent event, EntityRef player) {
        // if in game phase: verify that game start condition still met
        Phase currentPhase = phaseSystem.getCurrentPhase();
        if ((currentPhase == Phase.IN_GAME || currentPhase == Phase.COUNTDOWN) && !teamSystem.isMinSizeTeams()) {
            logger.debug("Starting condition no longer met, switching from phase {} to PRE_GAME", currentPhase);
            gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.PRE_GAME));
        }

        // if the player teleporting was the last one still in the arena
        // switch back to idle phase
        if (teamSystem.getTeamSize(LASUtils.BLACK_TEAM) == 0 && teamSystem.getTeamSize(LASUtils.RED_TEAM) == 0) {
            logger.debug("No players left in arena, switching to IDLE phase");
            gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.IDLE));
        }
    }

    @Override
    public void initialise() {
        if (!celestialSystem.isSunHalted()) {
            celestialSystem.toggleSunHalting(0.5f);
        }
    }

    @Override
    public void shutdown() {
    }

}
