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

package se.streamsource.streamflow.web.domain.entity.caze;

import org.qi4j.api.io.*;
import se.streamsource.streamflow.domain.contact.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.web.domain.structure.attachment.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.conversation.*;
import se.streamsource.streamflow.web.domain.structure.form.*;

/**
 * JAVADOC
 */
public class CaseDescriptor
{
   private final Case caze;

   public CaseDescriptor(Case aCase)
   {
      caze = aCase;
   }

   public Case getCase()
   {
      return caze;
   }

   public Input<ContactValue, RuntimeException> contacts()
   {
      return Inputs.iterable(((Contacts.Data)caze).contacts().get());
   }

   public Input<SubmittedFormValue, RuntimeException> submittedForms()
   {
      return Inputs.iterable(((SubmittedForms.Data) caze).submittedForms().get());
   }

   public Input<Conversation, RuntimeException> conversations()
   {
      return Inputs.iterable(((Conversations.Data)caze).conversations());

   }

   public Input<Attachment, RuntimeException> attachments()
   {
      return Inputs.iterable(((Attachments.Data)caze).attachments());
   }
}
