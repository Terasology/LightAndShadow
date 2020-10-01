/*
 * Copyright 2017 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.AudioManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.health.event.OnDamagedEvent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.particles.components.ParticleEmitterComponent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.Assets;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.regions.BlockRegionComponent;

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

        Vector3f horizontalDir = new Vector3f(JomlUtil.from(event.getDirection()));
        horizontalDir.y = 0;
        Side facingDir = Side.inDirection(horizontalDir);
        if (!facingDir.isHorizontal()) {
            event.consume();
            return;
        }

        Vector3f offset = new Vector3f(JomlUtil.from(event.getHitPosition()));
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

        worldProvider.setBlock(bottomBlockPos, card.bottomBlockFamily.getBlockForPlacement(bottomBlockPos, facingDir, Side.TOP));
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
