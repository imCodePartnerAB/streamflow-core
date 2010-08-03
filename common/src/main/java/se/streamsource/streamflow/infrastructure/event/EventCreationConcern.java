/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.infrastructure.event;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.This;
import se.streamsource.streamflow.infrastructure.event.factory.DomainEventFactory;

import java.lang.reflect.Method;

/**
 * Generate event for event method
 */
@AppliesTo(EventMethodFilter.class)
public class EventCreationConcern
      extends GenericConcern
{
   @This
   EntityComposite entity;

   @Service
   DomainEventFactory domainEventFactory;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      if (args[0] == DomainEvent.CREATE)
      {
         // Create event
         DomainEvent event = domainEventFactory.createEvent( entity, method.getName(), args );
         args[0] = event;
      }

      return next.invoke( proxy, method, args );
   }
}