// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.barrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.RelevanceRegionComponent;
import org.terasology.itemRendering.components.AnimateRotationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;

@RegisterSystem
public class MagicDomeSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(MagicDomeSystem.class);

    private static final int WORLD_RADIUS = 500;
    private final Vector3f lastPos = Vector3f.zero();
    private final FastRandom random = new FastRandom();
    @In
    private EntityManager entityManager;
    private EntityRef magicDomeEntity = EntityRef.NULL;
    private float updateDelta;

    @Override
    public void postBegin() {
        //toggleDome();
    }

    @Command(shortDescription = "Activate/Deactivate dome barrier", helpText = "Activates or deactivates the dome " +
            "barrier around the world", runOnServer = true)
    public String dome() {
        toggleDome();
        return "Toggled dome.";
    }

    public void toggleDome() {

        if (!entityManager.getEntitiesWith(MagicDome.class).iterator().hasNext()) {

            magicDomeEntity = entityManager.create("lightAndShadowResources:magicDome", Vector3f.zero());
            LocationComponent loc = magicDomeEntity.getComponent(LocationComponent.class);
            loc.setWorldScale(2 * WORLD_RADIUS * 1.01f);
            magicDomeEntity.addOrSaveComponent(loc);
            magicDomeEntity.addOrSaveComponent(new RelevanceRegionComponent());
            magicDomeEntity.setAlwaysRelevant(true);

            AnimateRotationComponent rotationComponent = new AnimateRotationComponent();
            rotationComponent.rollSpeed = 0.01f;
            rotationComponent.pitchSpeed = 0.01f;
            rotationComponent.yawSpeed = 0.01f;
            magicDomeEntity.addOrSaveComponent(rotationComponent);
        } else {
            magicDomeEntity.destroy();
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent loc) {
        Vector3f pos = new Vector3f(loc.getWorldPosition());

        float distance = pos.length();

        float deltaDistance = TeraMath.fastAbs(pos.distance(lastPos));

        for (EntityRef domeEntity : entityManager.getEntitiesWith(MagicDome.class, LocationComponent.class)) {
            LocationComponent domeLocationComponent = domeEntity.getComponent(LocationComponent.class);
            Vector3f domeCenter = domeLocationComponent.getWorldPosition();
            Vector3f domeVerticalTop = domeCenter.addY(WORLD_RADIUS);

            MagicDome dome = domeEntity.getComponent(MagicDome.class);

            if (deltaDistance > 0.2f) {
//                logger.info("CharacerMoveInputEvent: position: {} - distance from O: {}, delta: {}", pos, distance,
//                deltaDistance);

                if (lastPos.length() < WORLD_RADIUS && lastPos.length() < pos.length() && distance > WORLD_RADIUS) {
//                    logger.info("Sending player back inside!");
                    Vector3f impulse = pos.normalize().invert();

                    impulse.set(impulse.scale(64).setY(6));
                    player.send(new CharacterImpulseEvent(impulse));

                    player.send(new PlaySoundEvent(magicDomeEntity.getComponent(MagicDome.class).hitSound, 2f));
                }

                if (lastPos.length() > WORLD_RADIUS && lastPos.length() > pos.length() && distance < WORLD_RADIUS) {
//                    logger.info("Sending player back outside");
                    Vector3f impulse = pos.normalize();
                    float verticalDiff =
                            TeraMath.sqrt(TeraMath.fastAbs(TeraMath.sqr(lastPos.getY()) - TeraMath.sqr(domeVerticalTop.getY())));
                    float impulseY = (TeraMath.fastAbs(verticalDiff) / (float) WORLD_RADIUS) * 3f;

                    impulse.set(impulse.scale(64)).addY(impulseY);
                    player.send(new CharacterImpulseEvent(impulse));

                    player.send(new PlaySoundEvent(magicDomeEntity.getComponent(MagicDome.class).hitSound, 2f));

                }
                lastPos.set(pos);
            }
        }
    }

    @Override
    public void update(float delta) {
        updateDelta += delta;
        if (updateDelta > 2.0f) {
            for (EntityRef entity : entityManager.getEntitiesWith(MagicDome.class, AnimateRotationComponent.class)) {
                AnimateRotationComponent rotationComponent = entity.getComponent(AnimateRotationComponent.class);

                rotationComponent.yawSpeed = TeraMath.clamp(rotationComponent.yawSpeed + random.nextFloat(-0.01f,
                        0.01f), -0.01f, 0.01f);
                rotationComponent.pitchSpeed = TeraMath.clamp(rotationComponent.pitchSpeed + random.nextFloat(-0.01f,
                        0.01f), -0.01f, 0.01f);
                rotationComponent.rollSpeed = TeraMath.clamp(rotationComponent.rollSpeed + random.nextFloat(-0.01f,
                        0.01f), -0.01f, 0.01f);

                entity.saveComponent(rotationComponent);
            }

            updateDelta = 0;
        }
    }
}
