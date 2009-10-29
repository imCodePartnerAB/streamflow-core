/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.task;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Mixins(Subtasks.Mixin.class)
public interface Subtasks
{
    void addSubtask(Subtask subtask);

    void removeSubtask(Subtask subtask);

    interface Data
    {
//        ManyAssociation<Subtask> subtasks();
    }

    class Mixin
            implements Subtasks
    {
        @This
        Data state;
        @This
        Subtasks subtasks;

        public void addSubtask(Subtask subtask)
        {
//            state.subtasks().add(state.subtasks().count(), subtask);
            subtask.changeParentTask(subtasks);
        }

        public void removeSubtask(Subtask subtask)
        {
//            state.subtasks().remove(subtask);
            subtask.changeParentTask(subtasks);
        }
    }
}
