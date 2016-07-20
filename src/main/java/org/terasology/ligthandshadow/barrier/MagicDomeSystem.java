/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.ligthandshadow.barrier;

import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterImpulseEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;

@RegisterSystem
public class MagicDomeSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MagicDomeSystem.class);

    private static final int WORLD_RADIUS = 500;

    @In
    private EntityManager entityManager;

    Vector3f lastPos = Vector3f.zero();
    EntityRef magicDomeEntity = EntityRef.NULL;

    @Override
    public void postBegin() {

        if (!entityManager.getEntitiesWith(MagicDome.class).iterator().hasNext()) {
            logger.info("Spawning magic dome!");
            Prefab magicDome = entityManager.getPrefabManager().getPrefab("lightAndShadowResource:zesphere");

            magicDomeEntity = entityManager.create("lightAndShadowResources:magicDome", Vector3f.zero());
            LocationComponent loc = magicDomeEntity.getComponent(LocationComponent.class);
            loc.setWorldScale(2*WORLD_RADIUS);
            magicDomeEntity.saveComponent(loc);
            magicDomeEntity.setAlwaysRelevant(true);
        }
    }

    @ReceiveEvent(components = {LocationComponent.class})
    public void onCharacterMovement(CharacterMoveInputEvent moveInputEvent, EntityRef player, LocationComponent loc) {
        Vector3f pos = new Vector3f(loc.getWorldPosition());

        float distance = pos.length();

        float deltaDistance = TeraMath.fastAbs(pos.distance(lastPos));
        if (deltaDistance > 0.2f) {
            logger.info("CharacerMoveInputEvent: position: {} - distance from O: {}, delta: {}", pos, distance, deltaDistance);
            lastPos.set(pos);

            if (distance > WORLD_RADIUS) {
                logger.info("Sending player back!");
                Vector3f impulse = pos.normalize().invert();
                impulse.set(impulse.scale(32));
                player.send(new CharacterImpulseEvent(impulse));
            }
        }
    }
}
