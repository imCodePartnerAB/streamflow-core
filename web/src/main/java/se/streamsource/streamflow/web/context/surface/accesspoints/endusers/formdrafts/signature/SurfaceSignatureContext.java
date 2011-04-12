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

package se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.signature;

import org.qi4j.api.concern.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.api.value.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.domain.form.*;
import se.streamsource.streamflow.web.context.surface.accesspoints.endusers.formdrafts.summary.*;
import se.streamsource.streamflow.web.domain.structure.caze.*;
import se.streamsource.streamflow.web.domain.structure.form.*;
import se.streamsource.streamflow.web.domain.structure.user.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Concerns(UpdateCaseCountFormSummaryConcern.class)
@Mixins(SurfaceSignatureContext.Mixin.class)
public interface SurfaceSignatureContext
   extends Context, IndexContext<FormDraftValue>
{
   void submit();
   
   void submitandsend();

   LinksValue providers();

   abstract class Mixin
      implements SurfaceSignatureContext
   {
      @Structure
      Module module;

      public FormDraftValue index()
      {
         return role( FormDraftValue.class );
      }

      public void submit()
      {
         EndUserCases userCases = role( EndUserCases.class );
         EndUser user = role( EndUser.class );
         FormDraft formSubmission = role( FormDraft.class );
         Case aCase = role( Case.class );

         userCases.submitForm( aCase, formSubmission , user );
      }

      public void submitandsend()
      {
         EndUserCases userCases = role( EndUserCases.class );
         EndUser user = role( EndUser.class );
         FormDraft formSubmission = role( FormDraft.class );
         Case aCase = role( Case.class );

         userCases.submitFormAndSendCase( aCase, formSubmission, user );
      }

      public RequiredSignaturesValue signatures()
      {
         FormDraftValue form = role( FormDraftValue.class );

         RequiredSignatures.Data data = module.unitOfWorkFactory().currentUnitOfWork().get( RequiredSignatures.Data.class, form.form().get().identity() );

         ValueBuilder<RequiredSignaturesValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( RequiredSignaturesValue.class );
         valueBuilder.prototype().signatures().get();
         ValueBuilder<RequiredSignatureValue> builder = module.valueBuilderFactory().newValueBuilder( RequiredSignatureValue.class );

         for (RequiredSignatureValue signature :  data.requiredSignatures().get())
         {
            builder.prototype().name().set( signature.name().get() );
            builder.prototype().description().set( signature.description().get() );

            valueBuilder.prototype().signatures().get().add( builder.newInstance() );
         }
         return valueBuilder.newInstance();
      }

      public LinksValue providers()
      {
         //var url = "https://175.145.48.194:8443/eid/sign/";
         return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
   }
}