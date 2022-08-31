// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.systems;

import org.terasology.economy.components.AllowShopScreenComponent;
import org.terasology.economy.ui.MarketUiClientSystem;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityManager;
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
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.unicode.EnclosedAlphanumerics;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.module.inventory.input.InventoryButton;
import org.terasology.module.lightandshadow.components.LASConfigComponent;
import org.terasology.module.lightandshadow.events.TimerEvent;
import org.terasology.module.lightandshadow.phases.OnPreGamePhaseStartedEvent;
import org.terasology.notifications.events.ExpireNotificationEvent;
import org.terasology.notifications.events.ShowNotificationEvent;
import org.terasology.notifications.model.Notification;
import org.terasology.notify.ui.DialogNotificationOverlay;
import org.terasology.nui.Color;
import org.terasology.nui.FontColor;

import java.util.Timer;
import java.util.TimerTask;

@RegisterSystem(RegisterMode.CLIENT)
public class ClientPregameSystem extends BaseComponentSystem {
    private static final String SHOP_NOTIFICATION_ID = "LightAndShadow:firstTimeShop";
    private static final String WAIT_NOTIFICATION_ID = "LightAndShadow:waitForPlayers";

    public static final ResourceUrn ASSET_URI = new ResourceUrn("LightAndShadow:Timer");

    private static final int COUNTDOWN_IN_SECONDS = 30;

    private static Timer timer;

    @In
    InputSystem inputSystem;

    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;
    @In
    private GameEntitySystem gameEntitySystem;

    @In
    private LocalPlayer localPlayer;

    private DialogNotificationOverlay window;

    @Override
    public void initialise() {
        window = nuiManager.addOverlay(ASSET_URI, DialogNotificationOverlay.class);
    }

    @Override
    public void shutdown() {
        nuiManager.closeScreen(window);
    }

    @ReceiveEvent
    public void onPregameStart(OnPreGamePhaseStartedEvent event, EntityRef entity) {
        Notification notification = new Notification(SHOP_NOTIFICATION_ID,
                "Too Few Players",
                "Each team needs at least " +
                        gameEntitySystem.getGameEntity().getComponent(LASConfigComponent.class).minTeamSize + " player(s)",
                "engine:icons#halfGreenHeart");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
        entity.upsertComponent(AllowShopScreenComponent.class, c -> c.orElse(new AllowShopScreenComponent()));
    }

    @ReceiveEvent
    public void updateTimer(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int timePeriod = COUNTDOWN_IN_SECONDS;
                boolean addNotification;
                public void run() {
                    if (addNotification) {
                        entity.send(new ExpireNotificationEvent(WAIT_NOTIFICATION_ID));
                        window.addNotification("The game starts in " + timePeriod-- + " seconds.");
                        addNotification = false;
                    } else {
                        entity.send(new ExpireNotificationEvent(WAIT_NOTIFICATION_ID));
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        addNotification = true;
                    }
                    if (timePeriod < 0) {
                        window.removeNotification("The game starts in " + (timePeriod + 1) + " seconds.");
                        timer.cancel();
                    }
                }
            }, 0, 500);
        }
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

    /**
     * Get a formatted representation of the primary {@link Input} associated with the given button binding.
     *
     * If the display name of the primary bound key is a single character this representation will be the encircled
     * character. Otherwise the full display name is used. The bound key will be printed in yellow.
     *
     * If no key binding was found the text "n/a" in red color is returned.
     *
     * @param button the URI of a bindable button
     * @return a formatted text to be used as representation for the player
     */
    //TODO: put this in a common place? Duplicated in Dialogs, EventualSkills, and InGameHelp
    private String getActivationKey(SimpleUri button) {
        return inputSystem.getInputsForBindButton(button).stream()
                .findFirst()
                .map(Input::getDisplayName)
                .map(key -> {
                    if (key.length() == 1) {
                        // print the key in yellow within a circle
                        int off = key.charAt(0) - 'A';
                        char code = (char) (EnclosedAlphanumerics.CIRCLED_LATIN_CAPITAL_LETTER_A + off);
                        return String.valueOf(code);
                    } else {
                        return key;
                    }
                })
                .map(key -> FontColor.getColored(key, Color.yellow))
                .orElse(FontColor.getColored("n/a", Color.red));
    }

    @ReceiveEvent(components = AllowShopScreenComponent.class)
    public void onShopComponentAdded(OnAddedComponent event, EntityRef entity) {
        Notification notification = new Notification(SHOP_NOTIFICATION_ID,
                "Shut Up and Take My Money!",
                "Press " + getActivationKey(new SimpleUri("Inventory:inventory")) + " to buy items",
                "Economy:GoldCoin");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }
}
