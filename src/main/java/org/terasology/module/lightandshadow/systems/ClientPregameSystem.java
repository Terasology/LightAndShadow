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
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.logic.players.event.LocalPlayerInitializedEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.unicode.EnclosedAlphanumerics;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.module.inventory.input.InventoryButton;
import org.terasology.module.lightandshadow.events.GameStartMessageEvent;
import org.terasology.module.lightandshadow.events.TimerEvent;
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
    private static final String NOTIFICATION_ID = "LightAndShadow:firstTimeShop";

    public static final ResourceUrn ASSET_URI = new ResourceUrn("LightAndShadow:Timer");

    private static final String PREGAME_MESSAGE = "The game start's as soon as there is \n at least one player in each team.";

    private static Timer timer;

    @In
    InputSystem inputSystem;

    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;

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
    public void onPregameStart(GameStartMessageEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            window.addNotification(PREGAME_MESSAGE);
            entity.upsertComponent(AllowShopScreenComponent.class, c -> c.orElse(new AllowShopScreenComponent()));
        }
    }

    @ReceiveEvent
    public void updateTimer(TimerEvent event, EntityRef entity) {
        if (localPlayer.getClientEntity().equals(entity)) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                int timePeriod = 30;
                boolean addNotification;
                public void run() {
                    if (addNotification) {
                        window.removeNotification(PREGAME_MESSAGE);
                        window.addNotification("The game starts in " + timePeriod-- + " seconds.");
                        addNotification = false;
                    } else {
                        window.removeNotification(PREGAME_MESSAGE);
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
    @ReceiveEvent(components = {ClientComponent.class, AllowShopScreenComponent.class}, priority = EventPriority.PRIORITY_CRITICAL)
    public void onInGameShopButton(InventoryButton event, EntityRef entity) {
        if (event.getState() == ButtonState.DOWN) {
            entity.send(new ExpireNotificationEvent(NOTIFICATION_ID));
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
        Notification notification = new Notification(NOTIFICATION_ID,
                "Shut Up and Take My Money!",
                "Press " + getActivationKey(new SimpleUri("Inventory:inventory")) + " to buy items",
                "Economy:GoldCoin");
        localPlayer.getClientEntity().send(new ShowNotificationEvent(notification));
    }
}
