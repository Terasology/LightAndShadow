// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.flag.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.lightandshadow.LASUtils;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.lightandshadow.gamestate.components.LASTeamComponent;
import org.terasology.lightandshadow.flag.components.TakeBlockOnActivateComponent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TakeBlockOnActivationSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(TakeBlockOnActivationSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    @In
    private InventoryManager inventoryManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = {TakeBlockOnActivateComponent.class, BlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        EntityRef flagTaker = event.getInstigator();

        // If the flag being taken is a red flag and the player is on the black team, let them take the flag
        if (!playerTeamMatchesFlagTeam(entity, flagTaker)) {
            giveFlagToPlayer(entity, flagTaker);
        }
    }

    private boolean playerTeamMatchesFlagTeam(EntityRef flag, EntityRef player) {
        LASTeamComponent flagTeamComponent = flag.getComponent(LASTeamComponent.class);
        LASTeamComponent playerTeamComponent = player.getComponent(LASTeamComponent.class);
        return (flagTeamComponent.team.equals(playerTeamComponent.team));
    }

    private void giveFlagToPlayer(EntityRef flag, EntityRef player) {
        BlockComponent blockComponent = flag.getComponent(BlockComponent.class);
        LASTeamComponent flagTeamComponent = flag.getComponent(LASTeamComponent.class);
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
        inventoryManager.giveItem(player, EntityRef.NULL, blockFactory.newInstance(blockManager.getBlockFamily(LASUtils.getFlagURI(flagTeamComponent.team))));
        worldProvider.setBlock(blockComponent.getPosition(), blockManager.getBlock(BlockManager.AIR_ID));
        flag.destroy();
    }
}
