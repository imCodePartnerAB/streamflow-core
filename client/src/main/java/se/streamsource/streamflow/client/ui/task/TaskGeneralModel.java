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

package se.streamsource.streamflow.client.ui.task;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.resource.task.TaskGeneralClientResource;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.infrastructure.event.source.EventHandler;
import se.streamsource.streamflow.infrastructure.event.source.EventHandlerFilter;
import se.streamsource.streamflow.resource.task.TaskGeneralDTO;

import java.util.Date;
import java.util.Observable;

/**
 * Model for the general info about a task.
 */
public class TaskGeneralModel extends Observable implements Refreshable, EventListener,
		EventHandler

{
	EventHandlerFilter eventFilter = new EventHandlerFilter(this, "addedLabel",
			"removedLabel");

	private TaskGeneralClientResource generalClientResource;

	TaskGeneralDTO general;

    @Uses LabelsModel labelsModel;

    @Uses LabelSelectionModel selectionModel;

    public TaskGeneralModel(@Uses TaskGeneralClientResource generalClientResource)
    {
        this.generalClientResource = generalClientResource;
        eventFilter = new EventHandlerFilter(generalClientResource.getRequest().getResourceRef().getParentRef().getLastSegment(), this, "addedLabel",
                "removedLabel");
    }

    public TaskGeneralDTO getGeneral()
	{
		if (general == null)
			refresh();

		return general;
	}

	public void describe(String newDescription)
	{
		try
		{
			generalClientResource.changeDescription(newDescription);
		} catch (ResourceException e)
		{
			throw new OperationException(
					TaskResources.could_not_change_description, e);
		}
	}

	public void changeNote(String newNote)
	{
		try
		{
			generalClientResource.changeNote(newNote);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_note, e);
		}
	}

	public void changeDueOn(Date newDueOn)
	{
		try
		{
			generalClientResource.changeDueOn(newDueOn);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_change_due_on,
					e);
		}
	}

	public void addLabel(String labelId)
	{
		try
		{
			generalClientResource.addLabel(labelId);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_add_label,
					e);
		}
	}

	public void removeLabel(String labelId)
	{
		try
		{
			generalClientResource.removeLabel(labelId);
		} catch (ResourceException e)
		{
			throw new OperationException(TaskResources.could_not_remove_label,
					e);
		}
	}
	
    public LabelsModel labelsModel()
    {
        return labelsModel;
    }


    public LabelSelectionModel selectionModel()
    {
        return selectionModel;
    }

	public void refresh()
	{
		try
		{
			general = (TaskGeneralDTO) generalClientResource.general()
					.buildWith().prototype();

            labelsModel.setLabels(general.labels().get());

            selectionModel.refresh();
            
		} catch (Exception e)
		{
			throw new OperationException(TaskResources.could_not_refresh, e);
		}
	}

	public void notifyEvent(DomainEvent event)
	{
        eventFilter.handleEvent(event);

        labelsModel.notifyEvent(event);
	}

	public boolean handleEvent(DomainEvent event)
	{
        refresh();
		return true;
	}
}