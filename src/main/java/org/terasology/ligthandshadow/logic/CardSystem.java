// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.math.Side;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.logic.MeshComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.health.logic.event.OnDamagedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

@RegisterSystem
public class CardSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(CardSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;
    @In
    private BlockManager blockManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private PrefabManager prefabManager;

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

        Vector3f offset = new Vector3f(event.getHitPosition());
        offset.sub(targetBlockComponent.getPosition().toVector3f());
        Side offsetDir = Side.inDirection(offset);

        Vector3i primePos = new Vector3i(targetBlockComponent.getPosition());
        primePos.add(offsetDir.getVector3i());

        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.consume();
            return;
        }

        Block belowBlock = worldProvider.getBlock(primePos.x, primePos.y - 1, primePos.z);
        Block aboveBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos;
        Block bottomBlock;
        Vector3i topBlockPos;
        Block topBlock;
        if (belowBlock.isReplacementAllowed()) {
            bottomBlockPos = new Vector3i(primePos.x, primePos.y - 1, primePos.z);
            bottomBlock = belowBlock;
            topBlockPos = primePos;
            topBlock = primeBlock;
        } else if (aboveBlock.isReplacementAllowed()) {
            bottomBlockPos = primePos;
            bottomBlock = primeBlock;
            topBlockPos = new Vector3i(primePos.x, primePos.y + 1, primePos.z);
            topBlock = aboveBlock;
        } else {
            event.consume();
            return;
        }

        worldProvider.setBlock(bottomBlockPos, card.bottomBlockFamily.getBlockForPlacement(bottomBlockPos, facingDir,
                Side.TOP));
        worldProvider.setBlock(topBlockPos, card.topBlockFamily.getBlockForPlacement(topBlockPos, facingDir, Side.TOP));

        EntityRef cardEntity = entityManager.create(card.cardBlockPrefab);
        entity.removeComponent(MeshComponent.class);
        cardEntity.addComponent(new BlockRegionComponent(Region3i.createBounded(bottomBlockPos, topBlockPos)));
        Vector3f cardCenter = bottomBlockPos.toVector3f();
        cardCenter.y += 0.5f;
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
