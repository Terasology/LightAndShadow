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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.ligthandshadow.componentsystem.components.LASTeam;
import org.terasology.ligthandshadow.componentsystem.components.TargetComponent;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.minion.path.MinionPathComponent;
import org.terasology.pathfinding.componentSystem.PathfinderSystem;
import org.terasology.pathfinding.model.WalkableBlock;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.List;

/**
 * @author synopia
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
        private Random random = new FastRandom();

        public SetTargetTargetBlockTask(Node node) {
            super(node);
        }

        @Override
        public Status update(float dt) {
            String team = actor().component(LASTeam.class).team;
            List<Vector3i> candidates = Lists.newArrayList();

            for (EntityRef entityRef : entityManager.getEntitiesWith(TargetComponent.class, LocationComponent.class, LASTeam.class)) {
                if (team.equals(entityRef.getComponent(LASTeam.class).team)) {
                    WalkableBlock block = pathfinderSystem.getBlock(entityRef);
                    if (block != null) {
                        candidates.add(block.getBlockPosition());
                    }
                }
            }

            if (candidates.size() > 0) {
                MinionPathComponent pathComponent = actor().component(MinionPathComponent.class);
                pathComponent.targetBlock = candidates.get(random.nextInt(candidates.size()));
                pathComponent.pathState = MinionPathComponent.PathState.NEW_TARGET;
                actor().save(pathComponent);
                return Status.SUCCESS;
            } else {
                return Status.FAILURE;
            }
        }
    }
}
