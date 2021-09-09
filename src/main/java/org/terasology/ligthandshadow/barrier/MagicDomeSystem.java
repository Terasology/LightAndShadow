// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.barrier;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterImpulseEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;
import org.terasology.ligthandshadow.componentsystem.components.InvulnerableComponent;
import org.terasology.ligthandshadow.componentsystem.events.ActivateBarrierEvent;
import org.terasology.ligthandshadow.componentsystem.events.DelayedDeactivateBarrierEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MagicDomeSystem extends BaseComponentSystem {
    private static final String DEACTIVATE_BARRIERS_ACTION = "LightAndShadow:deactivateBarriers";
    private static final int PREGAME_ZONE_RADIUS = 20;
    @In
    private EntityManager entityManager;
    @In
    DelayManager delayManager;

    private Vector3f position = new Vector3f();
    private EntityRef redBarrier = EntityRef.NULL;
    private EntityRef blackBarrier = EntityRef.NULL;


    /**
     * Activates the barriers for the pregame regions corresponding to both the teams only in the beginning when
     * the barriers haven't been created yet.
     *
     * @param event
     * @param entity
     */
    @ReceiveEvent
    public void activateBarriers(ActivateBarrierEvent event, EntityRef entity) {
        if (redBarrier == EntityRef.NULL && blackBarrier == EntityRef.NULL) {
            redBarrier = createBarrier("lightAndShadowResources:magicDome", LASUtils.CENTER_RED_BASE_POSITION, "red");
            blackBarrier = createBarrier("lightAndShadowResources:magicDome", LASUtils.CENTER_BLACK_BASE_POSITION, "black");
        }
    }

    @ReceiveEvent
    public void delayedDeactivateBarriers(DelayedDeactivateBarrierEvent event, EntityRef entity) {
        delayManager.addDelayedAction(entity, DEACTIVATE_BARRIERS_ACTION, event.getDelay());
    }

    @ReceiveEvent
    public void deactivateBarriers(DelayedActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(DEACTIVATE_BARRIERS_ACTION)) {
            redBarrier.destroy();
            blackBarrier.destroy();
            removePlayerInvulnerableComponents();
        }
    }


    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent loc) {
        Vector3f pos = new Vector3f(loc.getWorldPosition(new Vector3f()));

        for (EntityRef domeEntity : entityManager.getEntitiesWith(MagicDome.class, LocationComponent.class)) {
            LocationComponent domeLocationComponent = domeEntity.getComponent(LocationComponent.class);
            Vector3f domeCenter = domeLocationComponent.getWorldPosition(new Vector3f());
            pos.sub(domeCenter, position);
            float distance = position.length();
            if (distance > PREGAME_ZONE_RADIUS && isAllowedInsideBarrier(player, domeEntity)) {
                Vector3f impulse = position.normalize().negate();
                impulse.mul(8);

                player.send(new CharacterImpulseEvent(impulse));
                player.send(new PlaySoundEvent(domeEntity.getComponent(MagicDome.class).hitSound, 2f));
            }
            if (distance < PREGAME_ZONE_RADIUS && !isAllowedInsideBarrier(player, domeEntity)) {
                Vector3f impulse = position.normalize();
                impulse.mul(8);
                player.send(new CharacterImpulseEvent(impulse));
                player.send(new PlaySoundEvent(domeEntity.getComponent(MagicDome.class).hitSound, 2f));

            }
        }
    }

    public EntityRef createBarrier(String prefab, Vector3ic location, String team) {
        EntityRef barrier = entityManager.create(prefab, new Vector3f(location));
        LocationComponent loc = barrier.getComponent(LocationComponent.class);
        loc.setWorldScale(2 * PREGAME_ZONE_RADIUS * 1.01f);
        MagicDome magicDome = barrier.getComponent(MagicDome.class);
        magicDome.team = team;
        barrier.addOrSaveComponent(loc);
        barrier.addOrSaveComponent(magicDome);
        return barrier;
    }

    public boolean isAllowedInsideBarrier(EntityRef player, EntityRef barrier) {
        String barrierTeam = barrier.getComponent(MagicDome.class).team;
        String playerTeam = player.getComponent(LASTeamComponent.class).team;
        return barrierTeam.equals(playerTeam);
    }

    private void removePlayerInvulnerableComponents() {
        Iterable<EntityRef> players = entityManager.getEntitiesWith(InvulnerableComponent.class);
        for (EntityRef player : players) {
            player.removeComponent(InvulnerableComponent.class);
        }
    }
}
