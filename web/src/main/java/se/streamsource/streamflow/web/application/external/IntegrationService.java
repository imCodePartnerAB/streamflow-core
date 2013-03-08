/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.application.external;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;

@Mixins( IntegrationService.Mixin.class )
public interface IntegrationService
      extends Configuration, Activatable, ServiceComposite
{
   abstract class Mixin
      implements IntegrationService
   {
      @This
      Configuration<IntegrationConfiguration> config;

      public void activate() throws Exception
      {
         // force instantiation of config
         config.configuration();
      }

      public void passivate() throws Exception
      {

      }
   }
}