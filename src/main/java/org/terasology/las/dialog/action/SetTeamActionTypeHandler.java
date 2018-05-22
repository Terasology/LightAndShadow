/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.las.dialog.action;

import com.google.common.collect.ImmutableMap;
import org.terasology.persistence.typeHandling.*;

import java.util.Map;

@RegisterTypeHandler
public class SetTeamActionTypeHandler extends SimpleTypeHandler<SetTeamAction> {

    public SetTeamActionTypeHandler() {

    }

    @Override
    public PersistedData serialize(SetTeamAction action, SerializationContext context) {
        Map<String, PersistedData> data = ImmutableMap.of(
                "type", context.create(action.getClass().getSimpleName()),
                "team", context.create(action.getTeam())
        );

        return context.create(data);
    }

    @Override
    public SetTeamAction deserialize(PersistedData data, DeserializationContext context) {
        PersistedDataMap root = data.getAsValueMap();
        String team = root.get("team").getAsString();
        return new SetTeamAction(team);
    }

}
