// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.economy.components.AllowShopScreenComponent;
import org.terasology.economy.ui.MarketUiClientSystem;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;
import org.terasology.module.inventory.input.InventoryButton;
import org.terasology.module.lightandshadow.LASUtils;
import org.terasology.module.lightandshadow.events.PlayerExitedArenaEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseStartedEvent;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;

import java.util.Timer;
import java.util.TimerTask;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientShopSystem extends BaseComponentSystem {
    private static final String SHOP_NOTIFICATION_ID = "LightAndShadow:firstTimeShop";

    @In
    InputSystem inputSystem;

    @In
    private LocalPlayer localPlayer;

    @ReceiveEvent
    public void onPregameStart(OnPreGamePhaseStartedEvent event, EntityRef entity) {
        entity.upsertComponent(AllowShopScreenComponent.class, c -> c.orElse(new AllowShopScreenComponent()));
    }

    @ReceiveEvent
    public void onArenaExit(PlayerExitedArenaEvent event, EntityRef entity) {
        entity.removeComponent(AllowShopScreenComponent.class);
    }

    /**
     * Handles the button event if in-game shop is enabled.
     * Needs to have a higher priority than {@link MarketUiClientSystem#onToggleInventory(InventoryButton, EntityRef)}
     * to receive the {@link InventoryButton} event before it is consumed.
     *
     * @param event the help button event.
     * @param entity the entity to display the help screen to.
     */
    @Priority(EventPriority.PRIORITY_CRITICAL)
    @ReceiveEvent(components = {ClientComponent.class, AllowShopScreenComponent.class})
    public void onInGameShopButton(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            entity.send(new ExpireNotificationEvent(SHOP_NOTIFICATION_ID));
        }
    }

    @ReceiveEvent(components = AllowShopScreenComponent.class)
    public void onShopComponentAdded(OnAddedComponent event, EntityRef entity) {
        Notification notification = new Notification(SHOP_NOTIFICATION_ID,
                "Shut Up and Take My Money!",
                "Press " + LASUtils.getActivationKey(inputSystem, new SimpleUri("Inventory:inventory")) + " to buy items",
                "Economy:GoldCoin");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }
}