// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.logic;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.family.BlockPlacementData;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.lightandshadowresources.components.CardComponent;
import org.terasology.module.health.events.OnDamagedEvent;

@RegisterSystem
public class CardSystem extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CardComponent.class, ItemComponent.class})
    public void placeCard(ActivateEvent event, EntityRef entity) {
        CardComponent card = entity.getComponent(CardComponent.class);
        BlockComponent targetBlockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComponent == null) {
            event.consume();
            return;
        }

        Vector3f horizontalDir = new Vector3f(event.getDirection());
        horizontalDir.y = 0;
        Side facingDir = Side.inDirection(horizontalDir);
        if (!facingDir.isHorizontal()) {
            event.consume();
            return;
        }

        Vector3i primePos = new Vector3i(targetBlockComponent.getPosition(new Vector3i()));

        Block primeBlock = worldProvider.getBlock(primePos);
        if (primeBlock.isPenetrable()) {
            event.consume();
            return;
        }

        Block bottomBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);
        Block topBlock = worldProvider.getBlock(primePos.x, primePos.y + 2, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos = new Vector3i();
        Vector3i topBlockPos = new Vector3i();
        if (bottomBlock.isReplacementAllowed() && topBlock.isReplacementAllowed()) {
            bottomBlockPos.set(primePos.x, primePos.y + 1, primePos.z);
            topBlockPos.set(primePos.x, primePos.y + 2, primePos.z);
        } else {
            event.consume();
            return;
        }

        final Vector3f viewingDir = new Vector3f(facingDir.direction());
        worldProvider.setBlock(bottomBlockPos,
                card.bottomBlockFamily.getBlockForPlacement(new BlockPlacementData(bottomBlockPos, Side.TOP, viewingDir)));
        worldProvider.setBlock(topBlockPos,
                card.topBlockFamily.getBlockForPlacement(new BlockPlacementData(topBlockPos, Side.TOP, viewingDir)));

        EntityRef cardEntity = entityManager.create(card.cardBlockPrefab);
        entity.removeComponent(MeshComponent.class);
        cardEntity.addComponent(new BlockRegionComponent(new BlockRegion(bottomBlockPos).union(topBlockPos)));

        Vector3f cardCenter = new Vector3f(bottomBlockPos).add(0, 0.5f, 0);
        cardEntity.saveComponent(new LocationComponent(cardCenter));
        CardComponent newCardComponent = cardEntity.getComponent(CardComponent.class);
        cardEntity.saveComponent(newCardComponent);
        cardEntity.removeComponent(ItemComponent.class);

        audioManager.playSound(Assets.getSound("engine:PlaceBlock").get(), 0.5f);
    }

    @ReceiveEvent(components = {CardComponent.class, LocationComponent.class})
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        EntityBuilder builder = entityManager.newBuilder("LightAndShadowResources:cardParticleEffect");
        builder.getComponent(ParticleEmitterComponent.class).particleSpawnsLeft = 12;
        builder.getComponent(ParticleEmitterComponent.class).destroyEntityWhenDead = true;
        builder.saveComponent(entity.getComponent(LocationComponent.class));
        builder.build();
    }
}
