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

package se.streamsource.streamflow.infrastructure.event;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import se.streamsource.streamflow.infrastructure.event.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.source.TransactionHandler;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.logging.Logger;

/**
 * DomainEvent player
 */
@Mixins(DomainEventPlayerService.DomainEventPlayerMixin.class)
public interface DomainEventPlayerService
        extends DomainEventPlayer, ServiceComposite
{
    class DomainEventPlayerMixin
            implements DomainEventPlayer
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        ValueBuilderFactory vbf;

        @Service
        IdentityGenerator idGenerator;

        @Service
        EventStore eventStore;

        @Structure
        Module module;

        String
        version;

        public void replayEvents(Date afterDate) throws EventReplayException
        {
            final EventReplayException[] ex = new EventReplayException[1];
            eventStore.transactions( afterDate, new TransactionHandler()
            {
                public boolean handleTransaction( TransactionEvents transaction )
                {
                    UnitOfWork uow = uowf.newUnitOfWork();
                    DomainEvent currentEvent = null;
                    try
                    {
                        for (DomainEvent domainEvent : transaction.events().get())
                        {
                            currentEvent = domainEvent;
                            // Get the entity
                            Class entityType = module.classLoader().loadClass(domainEvent.entityType().get() );
                            String id = domainEvent.entity().get();
                            Object entity = uow.get(entityType, id);

                            // Get method
                            Method eventMethod = getEventMethod(entityType, domainEvent.name().get());

                            if (eventMethod == null)
                            {
                                Logger.getLogger( DomainEventPlayer.class.getName() ).warning( "Could not find event method "+domainEvent.name().get()+" in entity of type "+entityType.getName() );
                                continue;
                            }

                            // Build parameters
                            String jsonParameters = domainEvent.parameters().get();
                            JSONObject parameters = (JSONObject) new JSONTokener(jsonParameters).nextValue();
                            Object[] args = new Object[eventMethod.getParameterTypes().length];
                            for (int i = 1; i < eventMethod.getParameterTypes().length; i++)
                            {
                                Class<?> parameterType = eventMethod.getParameterTypes()[i];

                                String paramName = "param"+i;

                                Object value = parameters.get( paramName );

                                args[i] = getParameterArgument(parameterType, value, uow);
                            }

                            args[0] = domainEvent;

                            // Invoke method
                            Logger.getLogger( DomainEventPlayer.class.getName() ).info( "Replay:"+domainEvent );

                            eventMethod.invoke( entity, args );
                        }
                        uow.complete();
                        return true;
                    } catch (Exception e)
                    {
                        uow.discard();
                        ex[0] = new EventReplayException(currentEvent, e);
                        return false;
                    }
                }
            });

            if (ex[0] != null)
                throw ex[0];
        }

        private Object getParameterArgument( Class<?> parameterType, Object value, UnitOfWork uow )
        {
            if (value.equals(JSONObject.NULL))
                return null;

            if (parameterType.equals( String.class ))
            {
                return (String) value;
            } else if (parameterType.equals(Boolean.class) || parameterType.equals( Boolean.TYPE ))
            {
                return (Boolean) value;
            } else if (parameterType.equals(Long.class) || parameterType.equals( Long.TYPE ))
            {
                return (Long) value;
            } else if (parameterType.equals(Integer.class) || parameterType.equals( Integer.TYPE ))
            {
                return (Integer) value;
            } else if (ValueComposite.class.isAssignableFrom( parameterType ))
            {
                return module.valueBuilderFactory().newValueFromJSON( parameterType, (String) value );
            } else if (parameterType.isInterface())
            {
                return uow.get( parameterType, (String)value );
            } else if (parameterType.isEnum())
            {
                return Enum.valueOf( (Class<? extends Enum>) parameterType, value.toString() );
            } else
            {
                throw new IllegalArgumentException( "Unknown parameter type:"+parameterType.getName());
            }
        }

        private Method getEventMethod( Class<? extends Object> aClass, String eventName )
        {
            for (Method method : aClass.getMethods())
            {
                if (method.getName().equals(eventName))
                {
                    Class[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length > 0 && parameterTypes[0].equals(DomainEvent.class))
                        return method;
                }
            }
            return null;
        }
    }
}