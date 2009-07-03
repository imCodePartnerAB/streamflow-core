/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package se.streamsource.streamflow.client.ui;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import static org.qi4j.api.usecase.UsecaseBuilder.*;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.structure.Application;
import org.restlet.Restlet;
import se.streamsource.streamflow.client.domain.individual.*;
import se.streamsource.streamflow.client.resource.StreamFlowClientResource;
import se.streamsource.streamflow.client.resource.users.UserClientResource;
import se.streamsource.streamflow.resource.inbox.TasksQuery;

import java.util.logging.Logger;

/**
 * JAVADOC
 */
@Mixins(DummyDataService.ActivatableMixin.class)
public interface DummyDataService
        extends ServiceComposite, Activatable
{
    class ActivatableMixin
            implements Activatable
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Structure
        TransientBuilderFactory tbf;

        @Structure
        ValueBuilderFactory vbf;

        @Service
        IndividualRepository individualRepository;

        @Service
        Restlet client;

        @Structure
        Application app;

        public void activate() throws Exception
        {
            if (!app.mode().equals(Application.Mode.development))
                return;

            try
            {
                UnitOfWork uow = uowf.newUnitOfWork(newUsecase("Create account"));

                Individual individual = individualRepository.individual();

                ValueBuilder<AccountSettingsValue> builder = vbf.newValueBuilder(AccountSettingsValue.class);
                builder.prototype().name().set("Test server");
                builder.prototype().server().set("http://localhost:8040/streamflow");
                builder.prototype().userName().set("administrator");
                builder.prototype().password().set("administrator");

                final Account account = individual.newAccount();
                account.updateSettings(builder.newInstance());

//                account.register(client);

                StreamFlowClientResource server = account.server(client);
                String response = server.version();
                System.out.println(response);
                UserClientResource user = account.server(client).users().user("administrator");
                TasksQuery query = vbf.newValue(TasksQuery.class);
                System.out.println(user.shared().user().inbox().tasks(query).tasks().get().size());

                uow.complete();

            } catch (Exception e)
            {
                Logger.getLogger(getClass().getName()).warning("Could not create dummy account");
                e.printStackTrace();
            }
        }

        public void passivate() throws Exception
        {
        }

    }

}