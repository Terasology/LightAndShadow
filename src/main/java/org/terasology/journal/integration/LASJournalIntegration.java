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
package org.terasology.journal.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dialogs.ShowDialogEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.utilities.Assets;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;
import org.terasology.journal.ui.ImageParagraph;
import org.terasology.nui.HorizontalAlign;

import java.util.Arrays;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LASJournalIntegration extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(LASJournalIntegration.class);
    @In
    private JournalManager journalManager;
    @In
    private PrefabManager prefabManager;

    private String lasChapterId = "LightAndShadow";

    @Override
    public void preBegin() {

        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();
        Prefab screenshot1 = prefabManager.getPrefab("LightAndShadow:LASTutorial Screenshot 1");
        Prefab screenshot2 = prefabManager.getPrefab("LightAndShadow:LASTutorial Screenshot 2");
        Prefab screenshot3 = prefabManager.getPrefab("LightAndShadow:LASTutorial Screenshot 3");
        Prefab screenshot4 = prefabManager.getPrefab("LightAndShadow:LASTutorial Screenshot 4");
        chapterHandler.registerJournalEntry("Instructions",
                Arrays.asList(
                        new ImageParagraph(new Prefab[]{screenshot1, screenshot2, screenshot3, screenshot4}, null)
                ));
        journalManager.registerJournalChapter(lasChapterId,
                Assets.getTextureRegion("LightAndShadow:MagicFoolIcon").get(),
                "LightAndShadow", chapterHandler);
        logger.info("registered journal chapter");
    }

    @ReceiveEvent
    public void onDialogActivated(ShowDialogEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(lasChapterId, "Instructions"));
    }
}
