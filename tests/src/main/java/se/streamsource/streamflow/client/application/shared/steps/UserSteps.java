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

package se.streamsource.streamflow.client.application.shared.steps;

import org.jbehave.scenario.annotations.Given;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.web.domain.user.UserEntity;

/**
 * JAVADOC
 */
public class UserSteps
        extends Steps
{
    @Structure
    UnitOfWorkFactory uowf;

    public UserEntity user;

    @Given("a user named $name")
    public void givenUserNamed(String name) throws Exception
    {
        UnitOfWork uow = uowf.newUnitOfWork();

        user = uow.get(UserEntity.class, name);
    }

/*
    @Given("a users named $name")
    public void givenUserNamed(String name) throws Exception
    {
        login.givenAnAccount();
        login.whenLoginWith(name, name);
        login.whenUserRegisters();

        login.account.visitShared(new UserVisitor()
        {
            public boolean visitUser(User users)
            {
                UserSteps.this.users = users;
                return false;
            }
        });
    }
*/
}