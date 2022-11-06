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
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.SetDirectionEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.events.BeforeDisconnectEvent;
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
import org.terasology.module.lightandshadow.events.PlayerExitedArenaEvent;
import org.terasology.module.lightandshadow.events.TimerEvent;
import org.terasology.module.lightandshadow.phases.Phase;
import org.terasology.module.lightandshadow.phases.SwitchToPhaseEvent;
import org.terasology.module.lightandshadow.phases.authority.PhaseSystem;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Teleports players to play arena once they chose their team. It also sends events to change players skins and hud based on team they have
 * chosen.
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
    @In
    TeamSystem teamSystem;

    private static final Logger logger = LoggerFactory.getLogger(TeleporterSystem.class);

    Optional<Prefab> prefab = Assets.getPrefab("inventory");
    StartingInventoryComponent startingInventory = prefab.get().getComponent(StartingInventoryComponent.class);

    private final Random random = new Random();

    /**
     * Depending on which teleporter the player chooses, they are set to that team and teleported to that base Assumption: there are two
     * teleporters, one for the red, one for the black team
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
            logger.debug("Player {} attempted to join white team via teleporter", player.getId());
            event.consume();
        } else {
            if (teamSystem.setPlayerTeamToTeam(player, targetTeam)) {
                logger.debug("Player {} successfully added to team {}", player, targetTeam);
                handlePlayerTeleport(player, targetTeam);
            } else {
                EntityRef gameEntity = gameEntitySystem.getGameEntity();
                LASConfigComponent config = gameEntity.getComponent(LASConfigComponent.class);
                player.getOwner().send(new ChatMessageEvent(
                        "The " + targetTeam + " team has " + config.maxTeamSizeDifference + " player(s) more, " +
                                "please join the " + LASUtils.getOppositionTeam(targetTeam) + " team.", EntityRef.NULL));
            }
        }
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

        // check game start condition
        if (teamSystem.isMinSizeTeams() && phaseSystem.getCurrentPhase() == Phase.PRE_GAME) {
            sendEventToClients(TimerEvent::new);
            gameEntitySystem.getGameEntity().send(new SwitchToPhaseEvent(Phase.COUNTDOWN));
        }
    }

    private void sendEventToClients(Supplier<Event> eventSupplier) {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        for (EntityRef client : clients) {
            client.send(eventSupplier.get());
        }
    }

    @Command(shortDescription = "Teleport player back to platform", helpText = "Platform Teleport", runOnServer = true,
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String teleportToPlatform(@Sender EntityRef sender) {
        ClientComponent clientComp = sender.getComponent(ClientComponent.class);
        LASTeamComponent senderTeam = clientComp.character.getComponent(LASTeamComponent.class);
        if (senderTeam != null && (senderTeam.team.equals(LASUtils.RED_TEAM) || senderTeam.team.equals(LASUtils.BLACK_TEAM))) {
            // spectators (white team) are not relevant for arena exit actions
            clientComp.character.send(new PlayerExitedArenaEvent(sender));
        }
        clientComp.character.send(new CharacterTeleportEvent(new Vector3f(LASUtils.FLOATING_PLATFORM_POSITION).add(0, 1, 0)));
        return "Teleporting you to the platform.";
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onPlayerDisconnect(BeforeDisconnectEvent event, EntityRef entity) {
        ClientComponent clientComp = entity.getComponent(ClientComponent.class);
        LASTeamComponent senderTeam = clientComp.character.getComponent(LASTeamComponent.class);
        if (senderTeam != null && (senderTeam.team.equals(LASUtils.RED_TEAM) || senderTeam.team.equals(LASUtils.BLACK_TEAM))) {
            // spectators (white team) are not relevant for arena exit actions
            teleportToPlatform(entity);
        }
    }
}
