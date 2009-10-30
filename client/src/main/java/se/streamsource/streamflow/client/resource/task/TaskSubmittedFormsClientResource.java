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

package se.streamsource.streamflow.client.resource.task;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.domain.form.SubmitFormDTO;
import se.streamsource.streamflow.resource.task.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;

import java.io.IOException;


/**
 * Mapped to /task/{id}/forms
 */
public class TaskSubmittedFormsClientResource
        extends CommandQueryClientResource
{
    public TaskSubmittedFormsClientResource(@Uses Context context, @Uses Reference reference)
    {
        super(context, reference);
    }

    public SubmittedFormsListDTO taskSubmittedForms() throws ResourceException, IOException
    {
        return query("taskSubmittedForms", SubmittedFormsListDTO.class);
    }

    public void submitForm(SubmitFormDTO submitDTO) throws ResourceException
    {
        postCommand("submitForm", submitDTO);
    }

    public EffectiveFieldsDTO effectiveFields() throws ResourceException
    {
        return query("effectiveFields", EffectiveFieldsDTO.class);
    }

    public TaskSubmittedFormClientResource taskSubmittedForm(int index)
    {
        TaskSubmittedFormClientResource submittedForm = getSubResource("" + index, TaskSubmittedFormClientResource.class);
        submittedForm.setRoot(getRoot());
        return submittedForm;
    }
}