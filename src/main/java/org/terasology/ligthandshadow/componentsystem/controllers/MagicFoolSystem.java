// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.ligthandshadow.componentsystem.controllers;

import org.terasology.dialogs.action.CloseDialogAction;
import org.terasology.dialogs.action.NewDialogAction;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dialogs.components.DialogResponse;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.ligthandshadow.componentsystem.components.MagicFoolComponent;

import java.util.ArrayList;

@RegisterSystem(RegisterMode.AUTHORITY)
public class MagicFoolSystem extends BaseComponentSystem {

    @ReceiveEvent(components = MagicFoolComponent.class)
    public void onActivateMagicFool(ActivateEvent event, EntityRef entity) {

        DialogComponent dialogComponent = entity.getComponent(DialogComponent.class);
        dialogComponent.pages = new ArrayList<>();
        dialogComponent.firstPage = "main";

        //the first page(main page) of MagicFool which shows Welcome text
        DialogPage mainPage = new DialogPage();
        mainPage.id = "main";
        mainPage.paragraphText = new ArrayList<>();
        mainPage.responses = new ArrayList<>();
        mainPage.title = "Welcome title";
        mainPage.paragraphText.add("Welcome to Light! Shadow. It is time for you to choose a side in this battle. " +
                "Approach a teleporter and activate it to join that side in a fight for victory."
        );

        //this is the second page or the instruction page of the MagicFool dialogue
        DialogPage instructionsPage = new DialogPage();
        instructionsPage.id = "instructions";
        instructionsPage.paragraphText = new ArrayList<>();
        instructionsPage.responses = new ArrayList<>();
        instructionsPage.title = "Instructions";
        instructionsPage.paragraphText.add("As LightAndShadow is a purely multiplayer gameplay now, you'll need a second client to " +
                "actually start the game." +
                "However, you can already take a look at the gameplay arena by interacting (E) with any of the two dice on the jester's " +
                "platform." +
                "When interacting with one of them, you'll join the related red or black team and spawn in the gameplay arena on your " +
                "team's home base." +
                "On your home base, you'll notice a single block, that is the flag. The goal of the game is to fetch the other team's " +
                "flag from their base and bring it to your home base." +
                "You'll also notice there is a magic dome around you that you cannot escape, same goes for the other team's base on the " +
                "other side of the gameplay arena." +
                "As long as there's no player in the other team, these magic domes will not resolve and the game will tell you that it's " +
                "waiting for more players." +
                "As soon as somebody joins the other team (you can try this out locally by starting a second client and joining a locally" +
                " hosted game), a countdown will start and once that's down to 0, the magic domes will resolve and the game will start." +
                "Now it's time to speed to the other team's base to fetch their flag (E) and back to your home base to deliver it (again " +
                "E). " +
                "But take care, the players of the other team could try to hinder you. All players have a basic set of weapons to fight " +
                "their opponents with." +
                "The team that first scores 5 points, aka successfully delivers 5 enemy flags to their home base, wins."
        );

        //this is the first button as a response in dialogue which closes the dialogue
        DialogResponse close = new DialogResponse();
        close.text = "Very Well...";
        close.responseImage = "LightAndShadowResources:answerArrow";
        close.action = new ArrayList<>();
        close.action.add(new CloseDialogAction());

        //this is the second button as a response in dialogue which shows instructions
        DialogResponse instruction = new DialogResponse();
        instruction.text = "How does it work?";
        instruction.responseImage = "LightAndShadowResources:answerArrow";
        instruction.action = new ArrayList<>();
        instruction.action.add(new NewDialogAction("instructions"));

        mainPage.responses.add(close);
        mainPage.responses.add(instruction);
        dialogComponent.pages.add(mainPage);
        dialogComponent.pages.add(instructionsPage);

    }
}
