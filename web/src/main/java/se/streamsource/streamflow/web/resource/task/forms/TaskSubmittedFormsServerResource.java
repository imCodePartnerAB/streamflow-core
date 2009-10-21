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

package se.streamsource.streamflow.web.resource.task.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;
import se.streamsource.streamflow.domain.contact.ContactValue;
import se.streamsource.streamflow.web.domain.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.resource.CommandQueryServerResource;
import se.streamsource.streamflow.resource.task.SubmittedFormsListDTO;

/**
 * Mapped to:
 * /tasks/{task}/forms/
 */
public class TaskSubmittedFormsServerResource
        extends CommandQueryServerResource
{
    @Structure
    ValueBuilderFactory vbf;

    public TaskSubmittedFormsServerResource()
    {
        setNegotiated(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    public SubmittedFormsListDTO taskSubmittedForms()
    {
        String formsQueryId = getRequest().getAttributes().get("task").toString();
        SubmittedFormsQueries forms = uowf.currentUnitOfWork().get(SubmittedFormsQueries.class, formsQueryId);

        return forms.getSubmittedForms();
    }

    public void add(ContactValue newContact)
    {
    }

    @Override
    protected String getConditionalIdentityAttribute()
    {
        return "task";
    }
}