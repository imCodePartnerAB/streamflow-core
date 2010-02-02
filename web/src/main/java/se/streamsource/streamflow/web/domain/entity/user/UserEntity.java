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

package se.streamsource.streamflow.web.domain.entity.user;

import org.qi4j.api.entity.Identity;
import org.qi4j.api.entity.Lifecycle;
import org.qi4j.api.entity.LifecycleException;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.contact.Contactable;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.entity.gtd.AssignmentsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.DelegationsQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.Inbox;
import se.streamsource.streamflow.web.domain.entity.gtd.InboxQueries;
import se.streamsource.streamflow.web.domain.entity.gtd.WaitingForQueries;
import se.streamsource.streamflow.web.domain.interaction.authentication.Authentication;
import se.streamsource.streamflow.web.domain.interaction.comment.Commenter;
import se.streamsource.streamflow.web.domain.interaction.gtd.Actor;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationParticipant;
import se.streamsource.streamflow.web.domain.structure.group.Participation;
import se.streamsource.streamflow.web.domain.structure.label.Labels;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationParticipations;
import se.streamsource.streamflow.web.domain.structure.user.User;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.domain.structure.form.Submitter;

/**
 * JAVADOC
 */
@Mixins({UserEntity.LifecycleMixin.class, UserEntity.AuthenticationMixin.class})
public interface UserEntity
      extends DomainEntity,

      // Interactions
      Actor,
      Inbox,
      Authentication,

      // Structure
      User,
      Commenter,
      Contactable,
      ConversationParticipant,
      OrganizationParticipations,
      Labels,
      Submitter,

      // Queries
      AssignmentsQueries,
      DelegationsQueries,
      OverviewQueries,
      InboxQueries,
      WaitingForQueries,
      ProjectQueries,

      // Data
      Inbox.Data,
      Contactable.Data,
      OrganizationParticipations.Data,
      Describable.Data,
      Labels.Data,
      Participation.Data,
      UserAuthentication.Data
{
   public static final String ADMINISTRATOR_USERNAME = "administrator";

   abstract class LifecycleMixin
         extends Describable.Mixin
         implements Lifecycle
   {
      @This
      Identity identity;

      public void create() throws LifecycleException
      {
         description().set( identity.identity().get() );
      }

      public void remove() throws LifecycleException
      {
      }
   }

   class AuthenticationMixin
      implements Authentication
   {
      @This UserAuthentication.Data data;

      public boolean login( String password )
      {
         if (data.disabled().get())
            return false;

         boolean correct = data.isCorrectPassword( password );

         return correct;
      }
   }
}
