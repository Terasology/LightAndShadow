// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.controllers;

import java.util.Collection;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.AttackEvent;
import org.terasology.engine.logic.health.EngineDamageTypes;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.module.health.events.DoDamageEvent;
import org.terasology.engine.registry.In;
import org.terasology.multiBlock2.component.MultiBlockMainComponent;
import org.terasology.multiBlock2.component.MultiBlockMemberComponent;

/**
 * Damages all the blocks in a multiBlock Structure when any block in the structure is damaged.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MultiBlockSystem extends BaseComponentSystem {
    @In
    private BlockEntityRegistry blockEntityRegistry;

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onDamageDone(AttackEvent event, EntityRef entity) {
        int damage = 1;
        Prefab damageType = EngineDamageTypes.PHYSICAL.get();
        ItemComponent item = event.getDirectCause().getComponent(ItemComponent.class);
        if (item != null) {
            damage = item.baseDamage;
            if (item.damageType != null) {
                damageType = item.damageType;
            }
        }
        Collection<Vector3i> blocks = getBlocks(entity);
        if (blocks != null) {
            for (Vector3i pos : blocks) {
                EntityRef block = blockEntityRegistry.getBlockEntityAt(pos);
                block.send(new DoDamageEvent(damage, damageType, event.getInstigator(), event.getDirectCause()));
                event.consume();
            }
        }
    }

    public Collection<Vector3i> getBlocks(EntityRef entity) {
        Collection<Vector3i> blocks = null;
        EntityRef mainBlock = null;
        if (entity.hasComponent(MultiBlockMainComponent.class)) {
            mainBlock = entity;
        } else if (entity.hasComponent(MultiBlockMemberComponent.class)) {
            mainBlock = blockEntityRegistry.getBlockEntityAt(entity.getComponent(MultiBlockMemberComponent.class).getMainBlockLocation());
        }
        if (mainBlock != null) {
            blocks = mainBlock.getComponent(MultiBlockMainComponent.class).getMultiBlockMembers();
        }
        return blocks;
    }
}
