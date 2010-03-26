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

package se.streamsource.streamflow.web.domain.structure.form;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(SelectedForms.Mixin.class)
public interface SelectedForms
{
   void addSelectedForm( Form form );

   void removeSelectedForm( Form form );

   boolean hasSelectedForm( Form form );

   interface Data
   {
      ManyAssociation<Form> selectedForms();

      void addedSelectedForm( DomainEvent event, Form form );

      void removedSelectedForm( DomainEvent event, Form form );

      public void possibleForms( LinksBuilder builder, Iterable<Forms.Data> forms );
   }

   abstract class Mixin
         implements SelectedForms, Data
   {
      public void addSelectedForm( Form form )
      {
         addedSelectedForm( DomainEvent.CREATE, form );
      }

      public void removeSelectedForm( Form form )
      {
         removedSelectedForm( DomainEvent.CREATE, form );
      }

      public boolean hasSelectedForm( Form form )
      {
         return selectedForms().contains( form );
      }

      public void addedSelectedForm( DomainEvent event, Form form )
      {
         selectedForms().add( form );
      }

      public void removedSelectedForm( DomainEvent event, Form form )
      {
         selectedForms().remove( form );
      }

      public void possibleForms( LinksBuilder builder, Iterable<Forms.Data> forms )
      {
         for (Forms.Data form : forms)
         {
            for (Form definedForm : form.forms())
            {
               if (!selectedForms().contains( definedForm ))
               {
                  builder.addDescribable( definedForm, (Describable) form );
               }
            }
         }
      }
   }
}