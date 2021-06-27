// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.barrier;

import org.terasology.combatSystem.weaponFeatures.components.ArrowComponent;
import org.terasology.combatSystem.weaponFeatures.components.ExplodeComponent;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.characters.CharacterImpulseEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.lightandshadowresources.components.LASTeamComponent;
import org.terasology.ligthandshadow.componentsystem.LASUtils;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MagicDomeSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final int WORLD_RADIUS = 20;

    @In
    private EntityManager entityManager;

    private float updateDelta;
    private Vector3f df = new Vector3f();
    private Vector3f temp = new Vector3f();
    private EntityRef redBarrier = EntityRef.NULL;
    private EntityRef blackBarrier = EntityRef.NULL;

    @Override
    public void postBegin() {
        //toggleDome();
    }

    @Command(shortDescription = "Activate/Deactivate dome barrier", helpText = "Activates or deactivates the dome barrier around the world", runOnServer = true)
    public String dome() {
        toggleDome();
        return "Toggled dome.";
    }

    public void toggleDome() {

        if (!entityManager.getEntitiesWith(MagicDome.class).iterator().hasNext()) {
            redBarrier = createBarrier("lightAndShadowResources:magicDome", LASUtils.CENTER_RED_BASE_POSITION, "red");
            blackBarrier = createBarrier("lightAndShadowResources:magicDome", LASUtils.CENTER_BLACK_BASE_POSITION, "black");
        } else {
            redBarrier.destroy();
            blackBarrier.destroy();
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent loc) {
        Vector3f pos = new Vector3f(loc.getWorldPosition(new Vector3f()));

        for (EntityRef domeEntity : entityManager.getEntitiesWith(MagicDome.class, LocationComponent.class)) {
            LocationComponent domeLocationComponent = domeEntity.getComponent(LocationComponent.class);
            Vector3f domeCenter = domeLocationComponent.getWorldPosition(new Vector3f());
            pos.sub(domeCenter, df);
            float distance = df.length();
//          logger.info("CharacerMoveInputEvent: position: {} - distance from O: {}, delta: {}", pos, distance, deltaDistance);
            if (distance > WORLD_RADIUS && isAllowedInsideBarrier(player, domeEntity)) {
//              logger.info("Sending player back inside!");
                Vector3f impulse = df.normalize().negate();
                impulse.mul(8);

                player.send(new CharacterImpulseEvent(impulse));
                player.send(new PlaySoundEvent(domeEntity.getComponent(MagicDome.class).hitSound, 2f));
            }
            if (distance < WORLD_RADIUS && !isAllowedInsideBarrier(player, domeEntity)) {
//              logger.info("Sending player back outside");
                Vector3f impulse = df.normalize();
                impulse.mul(8);
                player.send(new CharacterImpulseEvent(impulse));

                player.send(new PlaySoundEvent(domeEntity.getComponent(MagicDome.class).hitSound, 2f));

            }
        }
    }

    public EntityRef createBarrier(String prefab, Vector3ic location, String team) {
        EntityRef barrier = entityManager.create(prefab, new Vector3f(location));
        LocationComponent loc = barrier.getComponent(LocationComponent.class);
        loc.setWorldScale(2 * WORLD_RADIUS * 1.01f);
        MagicDome magicDome = barrier.getComponent(MagicDome.class);
        magicDome.team = team;
        barrier.addOrSaveComponent(loc);
        barrier.addOrSaveComponent(magicDome);
        return barrier;
    }

    public boolean isAllowedInsideBarrier(EntityRef player, EntityRef barrier) {
        return barrier.getComponent(MagicDome.class).team.equals(player.getComponent(LASTeamComponent.class).team);
    }

    public boolean isProjectileAllowedInsideBarrier(EntityRef projectile, EntityRef barrier) {

        if (projectile.hasComponent(LASTeamComponent.class) && projectile.getComponent(LASTeamComponent.class).team.equals("white")) {
            projectile.getComponent(LocationComponent.class).getWorldPosition(temp);
            if (temp.sub(new Vector3f(LASUtils.CENTER_RED_BASE_POSITION), new Vector3f()).length()
                    < temp.sub(new Vector3f(LASUtils.CENTER_BLACK_BASE_POSITION), new Vector3f()).length()) {
                projectile.getComponent(LASTeamComponent.class).team = "red";
            } else {
                projectile.getComponent(LASTeamComponent.class).team = "black";
            }
        }
        return barrier.getComponent(MagicDome.class).team.equals(projectile.getComponent(LASTeamComponent.class).team);
    }

    public void destroyProjectile(Iterable<EntityRef> items) {
        for (EntityRef item : items) {
            if (item.getComponent(LocationComponent.class) != null) {
                LocationComponent itemLocationComponent = item.getComponent(LocationComponent.class);
                Vector3f pos = itemLocationComponent.getWorldPosition(new Vector3f());
                for (EntityRef domeEntity : entityManager.getEntitiesWith(MagicDome.class, LocationComponent.class)) {
                    LocationComponent domeLocationComponent = domeEntity.getComponent(LocationComponent.class);
                    Vector3f domeCenter = domeLocationComponent.getWorldPosition(new Vector3f());
                    pos.sub(domeCenter, df);
                    float distance = df.length();
                    if (distance > WORLD_RADIUS && isProjectileAllowedInsideBarrier(item, domeEntity)) {
                        item.destroy();
                    }
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        updateDelta += delta;
        if (updateDelta > 0.5f) {
            // required to destroy the fireball sent by the staff
            Iterable<EntityRef> fireballs = entityManager.getEntitiesWith(ExplodeComponent.class, LASTeamComponent.class);
            destroyProjectile(fireballs);
            // required to destroy the arrows and the spear
            Iterable<EntityRef> arrows = entityManager.getEntitiesWith(ArrowComponent.class, LASTeamComponent.class);
            destroyProjectile(arrows);
            updateDelta = 0;
        }
    }
}
