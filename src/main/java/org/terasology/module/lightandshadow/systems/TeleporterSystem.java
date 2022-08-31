// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.joml.Random;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.PlayerCharacterComponent;
import org.terasology.engine.logic.players.SetDirectionEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.lightandshadowresources.components.SetTeamOnActivateComponent;
import org.terasology.module.inventory.components.StartingInventoryComponent;
import org.terasology.module.inventory.events.RequestInventoryEvent;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.module.lightandshadow.components.LASTeamStatsComponent;
import org.terasology.module.lightandshadow.events.DelayedDeactivateBarrierEvent;
import org.terasology.module.lightandshadow.events.GameStartMessageEvent;
import org.terasology.module.lightandshadow.events.TimerEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.SwitchToPhaseEvent;
import org.terasology.module.lightandshadow.phases.authority.PhaseSystem;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Teleports players to play arena once they chose their team.
 * It also sends events to change players skins and hud based on team they have chosen.
 *
 * @see ClientSkinSystem
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TeleporterSystem extends BaseComponentSystem {
    @In
    EntityManager entityManager;
    @In
    GameEntitySystem gameEntitySystem;
    @In
    PhaseSystem phaseSystem;

    private static final Logger logger = LoggerFactory.getLogger(TeleporterSystem.class);

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    private final Random random = new Random();

    @Command(shortDescription = "Set the maximum team size difference", helpText = "Set maxTeamSizeDifference", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String setMaxTeamSizeDifference(@Sender EntityRef client, @CommandParam("difference") int difference) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASConfigComponent lasconfig = gameEntity.getComponent(LASConfigComponent.class);
        lasconfig.maxTeamSizeDifference = difference;
        gameEntity.saveComponent(lasconfig);
        return "The max team size difference is set to " + difference;
    }

    /**
     * Depending on which teleporter the player chooses, they are set to that team
     * and teleported to that base
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent(components = SetTeamOnActivateComponent.class)
    public void onTeleportActivation(ActivateEvent event, EntityRef entity) {
        EntityRef player = event.getInstigator();
        String targetTeam = entity.getComponent(LASTeamComponent.class).team;
        if (targetTeam.equals(LASUtils.WHITE_TEAM)) {
            // teleporters are (currently) expected to only target black or red team
            logger.debug("Player {} attempted to join white team", player.getId());
            event.consume();
        } else {
            gameEntitySystem.updateTeamStats();

            if (isBalancedTeams(targetTeam)) {
                String team = setPlayerTeamToTeleporterTeam(player, entity);
                handlePlayerTeleport(player, team);

                // check game start condition
                if (isMinSizeTeams() && phaseSystem.getCurrentPhase() == Phase.PRE_GAME) {
                    sendEventToClients(TimerEvent::new);
                    player.send(new DelayedDeactivateBarrierEvent(30000));
                    gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.COUNTDOWN));
                }
            } else {
                EntityRef gameEntity = gameEntitySystem.getGameEntity();
                LASConfigComponent config = gameEntity.getComponent(LASConfigComponent.class);
                player.getOwner().send(new ChatMessageEvent(
                        "The " + targetTeam + " team has " + config.maxTeamSizeDifference + " player(s) more, " +
                                "please join the " + LASUtils.getOppositionTeam(targetTeam) + " team.", EntityRef.NULL));
            }
        }
    }

    private boolean isBalancedTeams(String targetTeam) {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASTeamStatsComponent teamStats = gameEntity.getComponent(LASTeamStatsComponent.class);
        int maxTeamSizeDifference = gameEntity.getComponent(LASConfigComponent.class).maxTeamSizeDifference;
        int currentTeamSizeDiff = teamStats.blackTeamSize - teamStats.redTeamSize;
        int postTeleportTeamSizeDiff = targetTeam.equals(LASUtils.BLACK_TEAM) ? currentTeamSizeDiff + 1 : currentTeamSizeDiff - 1;

        return Math.abs(postTeleportTeamSizeDiff) <= maxTeamSizeDifference;
    }

    private boolean isMinSizeTeams() {
        EntityRef gameEntity = gameEntitySystem.getGameEntity();
        LASTeamStatsComponent teamStats = gameEntity.getComponent(LASTeamStatsComponent.class);
        int minTeamSize = gameEntity.getComponent(LASConfigComponent.class).minTeamSize;

        return teamStats.redTeamSize >= minTeamSize && teamStats.blackTeamSize >= minTeamSize;
    }

    private String setPlayerTeamToTeleporterTeam(EntityRef player, EntityRef teleporter) {
        LASTeamComponent teleporterTeamComponent = teleporter.getComponent(LASTeamComponent.class);
        LASTeamComponent playerTeamComponent = player.getComponent(LASTeamComponent.class);
        playerTeamComponent.team = teleporterTeamComponent.team;
        player.saveComponent(playerTeamComponent);
        return playerTeamComponent.team;
    }

    private void handlePlayerTeleport(EntityRef player, String team) {
        // when the first player joins the game, switch to pre-game phase
        if (phaseSystem.getCurrentPhase() == Phase.IDLE) {
            gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.PRE_GAME));
        }
        Vector3f randomVector = new Vector3f(-1 + random.nextInt(3), 0, -1 + random.nextInt(3));
        player.send(new CharacterTeleportEvent(randomVector.add(LASUtils.getTeleportDestination(team))));
        player.send(new SetDirectionEvent(LASUtils.getYaw(LASUtils.getTeleportDestination(team).
                sub(LASUtils.getTeleportDestination(LASUtils.getOppositionTeam(team)), new Vector3f())), 0));
        player.addOrSaveComponent(startingInventory);
        player.send(new RequestInventoryEvent(startingInventory.items));
        sendEventToClients(GameStartMessageEvent::new);
    }

    private void sendEventToClients(Supplier<Event> eventSupplier) {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        for (EntityRef client : clients) {
            client.send(eventSupplier.get());
        }
    }
}
