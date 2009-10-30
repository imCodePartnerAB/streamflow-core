/*
 * Copyright (c) 2009, Mads Enevoldsen. All Rights Reserved.
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
import org.jbehave.scenario.annotations.When;
import org.jbehave.scenario.steps.Steps;
import org.qi4j.api.injection.scope.Uses;
import se.streamsource.streamflow.client.application.shared.steps.setup.GenericSteps;
import se.streamsource.streamflow.web.domain.form.Field;
import se.streamsource.streamflow.web.domain.form.FormEntity;
import se.streamsource.streamflow.web.domain.form.Forms;

/**
 * JAVADOC
 */
public class FormsSteps
        extends Steps
{
    @Uses
    GenericSteps genericSteps;

    @Uses
    ValueDefinitionsSteps valueSteps;

    @Uses
    OrganizationsSteps orgsSteps;

    @Uses
    FormTemplatesSteps formTemplatesSteps;

    @Uses
    ProjectsSteps projectSetupSteps;

    public FormEntity givenForm;

    @Given("form named $form")
    public void givenForm(String form)
    {
        givenForm = projectSetupSteps.givenProject.getFormByName( form );
    }

    @When("a form named $name is created")
    public void createForm(String name) throws Exception
    {
        try
        {
            Forms projectForms = projectSetupSteps.givenProject;

            givenForm = (FormEntity) projectForms.createForm( );
            givenForm.changeDescription(name);
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("a form is removed from project")
    public void removeForm() throws Exception
    {
        try
        {
            Forms projectForms = projectSetupSteps.givenProject;

            projectForms.removeForm( givenForm );
        } catch(Exception e)
        {
            genericSteps.setThrowable(e);
        }
    }

    @When("a field named $name is added to form")
    public void createField( String someField )
    {
        givenForm.createField( someField, valueSteps.givenValue );
    }

    @When("a field named $name is removed from form")
    public void removeField(String name)
    {
        Field field = givenForm.getFieldByName( name );
        givenForm.removeField(field);
    }
}