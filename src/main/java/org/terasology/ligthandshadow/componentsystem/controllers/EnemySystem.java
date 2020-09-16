// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.behaviors.minion.work.WorkTargetComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.ligthandshadow.componentsystem.components.LASTeamComponent;
import org.terasology.math.geom.Vector3f;

import java.util.Map;
import java.util.Set;

/**
 * Created by synopia on 01.02.14.
 */
//@RegisterSystem
public class EnemySystem extends BaseComponentSystem {
    public static final Logger logger = LoggerFactory.getLogger(EnemySystem.class);
    private final Map<String, Set<EntityRef>> teams = Maps.newHashMap();
    private final Set<EntityRef> entities = Sets.newHashSet();
    private final Map<EntityRef, Set<Distance>> distances = Maps.newHashMap();

    public EnemySystem() {
        CoreRegistry.put(EnemySystem.class, this);
    }

    @Override
    public void initialise() {
    }

    @ReceiveEvent
    public void onActivated(OnActivatedComponent event, EntityRef entityRef, LASTeamComponent team,
                            LocationComponent locationComponent) {
        Set<EntityRef> teamEntities = teams.get(team.team);
        if (teamEntities == null) {
            teamEntities = Sets.newHashSet();
            teams.put(team.team, teamEntities);
        }
        teamEntities.add(entityRef);
        entities.add(entityRef);
//        recalculateDistances();
        WorkTargetComponent component = new WorkTargetComponent();
        component.workUri = "LightAndShadow:attackMinion" + team.team;
        entityRef.addComponent(component);
    }

    @ReceiveEvent
    public void onRemoved(BeforeRemoveComponent event, EntityRef entityRef, LASTeamComponent team,
                          LocationComponent locationComponent, WorkTargetComponent jobTargetComponent) {
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
                    Vector3f dist = one.getComponent(LocationComponent.class).getWorldPosition();
                    dist.sub(two.getComponent(LocationComponent.class).getWorldPosition());
                    float length = dist.length();
                    addDistance(one, two, length);
                    addDistance(two, one, length);
                }
            }
        }
    }

    private void addDistance(EntityRef one, EntityRef two, float dist) {
        Set<Distance> map = distances.get(one);
        if (map == null) {
            map = Sets.newTreeSet();
            distances.put(one, map);
        }
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
