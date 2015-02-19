/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.ligthandshadow.logic;

import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class YinYangPedestalAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final long UPDATE_INTERVAL = 1000;

    @In
    Time time;
    @In
    EntityManager entityManager;

    long nextUpdateTime;

    private EntityRef getWorldEntity() {
        for (EntityRef entityRef : entityManager.getEntitiesWith(WorldComponent.class)) {
            return entityRef;
        }
        return EntityRef.NULL;
    }


    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (currentTime > nextUpdateTime) {
            nextUpdateTime = currentTime + UPDATE_INTERVAL;

            for (EntityRef entityRef : entityManager.getEntitiesWith(TeamPedestalComponent.class, InventoryComponent.class)) {
                TeamPedestalComponent teamPedestalComponent = entityRef.getComponent(TeamPedestalComponent.class);
                InventoryComponent inventoryComponent = entityRef.getComponent(InventoryComponent.class);
                int filledSlots = 0;
                for (EntityRef item : inventoryComponent.itemSlots) {
                    if (item.exists()) {
                        filledSlots++;
                    }
                }

                if (filledSlots == 2) {
                    getWorldEntity().send(new ScoreTeamPointsEvent(teamPedestalComponent.team, 1));
                }
            }
        }
    }
}
