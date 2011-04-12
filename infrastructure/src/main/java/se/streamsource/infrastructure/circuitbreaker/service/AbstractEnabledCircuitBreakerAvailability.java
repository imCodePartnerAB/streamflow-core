/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.infrastructure.circuitbreaker.service;

import org.qi4j.api.configuration.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.service.*;
import se.streamsource.infrastructure.circuitbreaker.*;

/**
 * Abstract composite that determines Availability by
 * checking the Enabled configuration and a CircuitBreaker.
 *
 * To use this, the service must implement ServiceCircuitBreaker, and its ConfigurationComposite
 * must extend Enabled.
 */
@Mixins(AbstractEnabledCircuitBreakerAvailability.Mixin.class)
public interface AbstractEnabledCircuitBreakerAvailability
   extends Availability
{
   class Mixin
      implements Availability
   {
      @This
      Configuration<Enabled> config;

      @This
      ServiceCircuitBreaker circuitBreaker;

      public boolean isAvailable()
      {
         return config.configuration().enabled().get() && circuitBreaker.getCircuitBreaker().getStatus() == CircuitBreaker.Status.on;
      }
   }
}
