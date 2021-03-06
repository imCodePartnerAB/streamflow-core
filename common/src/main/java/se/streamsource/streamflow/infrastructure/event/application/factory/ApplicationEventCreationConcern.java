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
package se.streamsource.streamflow.infrastructure.event.application.factory;

import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.concern.GenericConcern;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;

import java.lang.reflect.Method;

/**
 * Generate application event for event method
 */
@AppliesTo(ApplicationEventMethodFilter.class)
public class ApplicationEventCreationConcern
      extends GenericConcern
{
   @Service
   ApplicationEventFactory eventFactory;

   public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
   {
      if (args[0] == null)
      {
         // Create application event
         ApplicationEvent event = eventFactory.createEvent( method.getName(), args );
         args[0] = event;
      }

      return next.invoke( proxy, method, args );
   }
}