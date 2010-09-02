/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.PageSubmissionValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.SummaryContext;
import se.streamsource.streamflow.web.domain.structure.form.FormSubmission;

/**
 * JAVADOC
 */
@Mixins(FormDraftContext.Mixin.class)
public interface FormDraftContext
   extends Context, IndexContext<FormSubmissionValue>
{
   // queries
   LinksValue pages();



   // commands
   @HasNextPage
   void nextpage( IntegerDTO page );

   @HasPreviousPage
   void previouspage(IntegerDTO page);

   void updatefield( FieldDTO field );

   @SubContext
   @HasNextPage(false)
   SummaryContext summary();

   // parameter only needed to make the command work
   void discard( IntegerDTO dummy );

   abstract class Mixin
      extends ContextMixin
      implements FormDraftContext
   {
      public FormSubmissionValue index()
      {

         return roleMap.get( FormSubmissionValue.class );
      }

      public LinksValue pages()
      {
         FormSubmissionValue value = roleMap.get( FormSubmissionValue.class );

         LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );

         int pageIndex = 0;
         for (PageSubmissionValue pageValue : value.pages().get())
         {
            builder.path( "../" );
            builder.command( "gotopage" );
            builder.addLink( pageValue.title().get(), ""+pageIndex );
            pageIndex++;
         }

         return builder.newLinks();
      }

      public void nextpage( IntegerDTO page )
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( 1 );

         if ( builder != null )
            updateFormSubmission( builder );
      }

      public void previouspage(IntegerDTO page)
      {
         ValueBuilder<FormSubmissionValue> builder = incrementPage( -1 );

         if ( builder != null )
            updateFormSubmission( builder );
      }

      private ValueBuilder<FormSubmissionValue> incrementPage( int increment )
      {
         ValueBuilder<FormSubmissionValue> builder = roleMap.get( FormSubmission.Data.class ).formSubmissionValue().get().buildWith();
         int page = builder.prototype().currentPage().get() + increment;
         int pages = builder.prototype().pages().get().size();

         if ( page < pages && page >= 0 )
         {
            builder.prototype().currentPage().set( page );
            return builder;
         }
         return null;
      }

      public void updatefield( FieldDTO field )
      {
         FormSubmission formSubmission = roleMap.get( FormSubmission.class );

         formSubmission.changeFieldValue( EntityReference.parseEntityReference( field.field().get() ), field.value().get() );
      }

      private void updateFormSubmission( ValueBuilder<FormSubmissionValue> builder )
      {
         FormSubmissionValue newFormValue = builder.newInstance();
         FormSubmission formSubmission = roleMap.get( FormSubmission.class );
         formSubmission.changeFormSubmission( newFormValue );

         roleMap.set( newFormValue );
      }

      public SummaryContext summary()
      {
         roleMap.set( this );
         return subContext( SummaryContext.class );
      }

      public void discard( IntegerDTO dummy )
      {
         // todo
      }
   }
}