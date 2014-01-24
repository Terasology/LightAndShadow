/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.nodes;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.ligthandshadow.componentsystem.components.TargetComponent;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.In;

/**
 * Created by synopia on 24.01.14.
 */
public class SetTargetTargetBlockNode extends Node {
    @Override
    public Task createTask() {
        return new SetTargetTargetBlockTask(this);
    }

    public static class SetTargetTargetBlockTask extends Task {
        @In
        private EntityManager entityManager;
        @In
        private PathfinderSystem pathfinderSystem;

        public SetTargetTargetBlockTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            for (EntityRef entityRef : entityManager.getEntitiesWith(TargetComponent.class, LocationComponent.class)) {
                MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
                WalkableBlock block = pathfinderSystem.getBlock(entityRef);
                if (block != null) {
                    pathComponent.targetBlock = block.getBlockPosition();
                    pathComponent.pathState = MinionPathComponent.PathState.NEW_TARGET;
                    actor().save(pathComponent);
                    return Status.SUCCESS;
                }
            }
            return Status.RUNNING;
        }
    }
}
