/*
 * Copyright (c) 2009, Henrik Bernstr&ouml;m. All Rights Reserved.
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

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.streamflow.infrastructure.application.ListValueBuilder;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import se.streamsource.streamflow.web.domain.label.Label;
import se.streamsource.streamflow.web.domain.label.LabelEntity;
import se.streamsource.streamflow.web.domain.label.Labelable;
import se.streamsource.streamflow.web.domain.label.Labels;

@Mixins(TaskLabelsQueries.Mixin.class)
public interface TaskLabelsQueries
{
	ListValue possibleLabels();

    class Mixin
	implements TaskLabelsQueries 
	{
		@This 
		Ownable.Data ownable;
		
		@This 
		Labelable.Data labelable;
		
		@Structure 
		QueryBuilderFactory qbf;
		
		@Structure 
		ValueBuilderFactory vbf;
		
		@Structure 
		UnitOfWorkFactory uowf;
		
		public ListValue possibleLabels()
        {
            ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder(EntityReferenceDTO.class);
            ListValueBuilder listBuilder = new ListValueBuilder(vbf);


            if (ownable.owner().get() instanceof Labels)
            {
                for (Label label : ((Labels.Data) ownable.owner().get()).labels())
                {
                    if (!labelable.labels().contains((LabelEntity) label))
                    {
                        listBuilder.addDescribable(label);
                    }
                }
            }

            return listBuilder.newList();
        }
    }
}
