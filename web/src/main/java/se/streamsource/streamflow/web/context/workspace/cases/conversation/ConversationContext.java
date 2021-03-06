/**
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.workspace.cases.conversation;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.api.workspace.cases.conversation.ConversationDTO;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.conversation.ConversationEntity;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipants;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;

/**
 * JAVADOC
 */
public class ConversationContext
      implements IndexContext<ConversationDTO>
{
   @Structure
   Module module;

   public ConversationDTO index()
   {
      ValueBuilder<ConversationDTO> builder = module.valueBuilderFactory().newValueBuilder( ConversationDTO.class );
      ConversationEntity conversation = RoleMap.role( ConversationEntity.class );

      builder.prototype().id().set( conversation.identity().get() );
      builder.prototype().href().set( conversation.identity().get() );
      builder.prototype().text().set( conversation.getDescription() );
      builder.prototype().creationDate().set( conversation.createdOn().get() );
      builder.prototype().creator().set( ((Describable) conversation.createdBy().get()).getDescription() );
      builder.prototype().messages().set( ((Messages.Data) conversation).messages().count() );
      builder.prototype().participants().set( ((ConversationParticipants.Data) conversation).participants().count() );

      return builder.newInstance();
   }
}