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

import org.terasology.dialogs.ShowDialogEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.journal.BrowserJournalChapterHandler;
import org.terasology.journal.DiscoveredNewJournalEntry;
import org.terasology.journal.JournalManager;

import java.util.Arrays;

@RegisterSystem(RegisterMode.AUTHORITY)
public class LASJournalIntegration extends BaseComponentSystem {
    @In
    private JournalManager journalManager;
    private String lasChapterId = "LightAndShadow";

    @Override
    public void preBegin() {
        BrowserJournalChapterHandler chapterHandler = new BrowserJournalChapterHandler();
        chapterHandler.registerJournalEntry("Instructions", Arrays.asList());
        journalManager.registerJournalChapter(lasChapterId,
                Assets.getTextureRegion("LightAndShadow:MagicFoolIcon").get(),
                "LightAndShadow", chapterHandler);
    }

    @ReceiveEvent
    public void onDialogActivated(ShowDialogEvent event, EntityRef player) {
        player.send(new DiscoveredNewJournalEntry(lasChapterId, "Instructions"));
    }
}
