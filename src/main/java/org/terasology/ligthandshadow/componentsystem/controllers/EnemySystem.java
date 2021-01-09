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
package org.terasology.ligthandshadow.componentsystem.controllers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minion.work.WorkTargetComponent;

import java.util.Map;
import java.util.Set;

/**
 * Created by synopia on 01.02.14.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class EnemySystem extends BaseComponentSystem {
    public static final Logger logger = LoggerFactory.getLogger(EnemySystem.class);
    private Map<String, Set<EntityRef>> teams = Maps.newHashMap();
    private Set<EntityRef> entities = Sets.newHashSet();
    private Map<EntityRef, Set<Distance>> distances = Maps.newHashMap();

    public EnemySystem() {
    }

    @Override
    public void initialise() {
    }

    @ReceiveEvent
    public void onActivated(OnActivatedComponent event, EntityRef entityRef, LASTeamComponent team, LocationComponent locationComponent) {
        Set<EntityRef> teamEntities = teams.computeIfAbsent(team.team, k -> Sets.newHashSet());
        teamEntities.add(entityRef);
        entities.add(entityRef);
//        recalculateDistances();
        WorkTargetComponent component = new WorkTargetComponent();
        component.workUri = "LightAndShadow:attackMinion" + team.team;
        entityRef.addComponent(component);
    }

    @ReceiveEvent
    public void onRemoved(BeforeRemoveComponent event, EntityRef entityRef, LASTeamComponent team, LocationComponent locationComponent, WorkTargetComponent jobTargetComponent) {
        entities.remove(entityRef);
        Set<EntityRef> map = teams.get(team.team);
        map.remove(entityRef);
    }

    @ReceiveEvent
    public void onBlockChange(OnEnterBlockEvent event, EntityRef entityRef, LASTeamComponent team) {
        if (entities.contains(entityRef)) {
//            recalculateDistances();
        }
    }

    private void recalculateDistances() {
        distances.clear();
        Set<EntityRef> others = Sets.newHashSet(entities);
        for (Map.Entry<String, Set<EntityRef>> entry : teams.entrySet()) {
            others.removeAll(entry.getValue());
            for (EntityRef one : entry.getValue()) {
                for (EntityRef two : others) {
                    Vector3f dist = one.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
                    dist.sub(two.getComponent(LocationComponent.class).getWorldPosition(new Vector3f()));
                    float length = dist.length();
                    addDistance(one, two, length);
                    addDistance(two, one, length);
                }
            }
        }
    }

    private void addDistance(EntityRef one, EntityRef two, float dist) {
        Set<Distance> map = distances.computeIfAbsent(one, k -> Sets.newTreeSet());
        map.add(new Distance(two, dist));
    }

    @Override
    public void shutdown() {

    }

    private static final class Distance implements Comparable<Distance> {
        public float distance;
        public EntityRef target;

        private Distance(EntityRef target, float distance) {
            this.target = target;
            this.distance = distance;
        }

        @Override
        public int compareTo(Distance o) {
            return Float.compare(distance, o.distance);
        }
    }
}
