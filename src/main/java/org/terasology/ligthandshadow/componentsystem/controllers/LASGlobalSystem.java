/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.components.LASGlobalComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

@RegisterSystem
@Share(value = LASGlobalSystem.class)
public class LASGlobalSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;

    private EntityRef globalEntity = EntityRef.NULL;

    public EntityRef getGlobalEntity() {
        return globalEntity;
    }

    public EntityRef getOrCreateGlobalEntity() {
        if (this.globalEntity.equals(EntityRef.NULL)) {
            if (entityManager.getCountOfEntitiesWith(LASGlobalComponent.class) != 0) {
                this.globalEntity = entityManager.getEntitiesWith(LASGlobalComponent.class).iterator().next();
            } else {
                this.globalEntity = entityManager.create("LightAndShadow:LASGlobalEntity");
            }
        }
        return globalEntity;
    }

}
