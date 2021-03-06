/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.surface.tasks;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfwriter.COSWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.qi4j.api.common.Optional;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.administration.form.AttachmentFieldValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.api.administration.form.RequiredSignaturesValue;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactBuilder;
import se.streamsource.streamflow.api.workspace.cases.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.api.workspace.cases.general.FormDraftDTO;
import se.streamsource.streamflow.api.workspace.cases.general.FormSignatureDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.util.Visitor;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.HtmlMailGenerator;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.application.pdf.PdfGeneratorService;
import se.streamsource.streamflow.web.context.services.ApplyFilterContext;
import se.streamsource.streamflow.web.domain.entity.attachment.AttachmentEntity;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity;
import se.streamsource.streamflow.web.domain.entity.customer.CustomersEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.SubmittedFieldValue;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.attachment.DefaultPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.attachment.FormPdfTemplate;
import se.streamsource.streamflow.web.domain.structure.caze.Case;
import se.streamsource.streamflow.web.domain.structure.customer.Customer;
import se.streamsource.streamflow.web.domain.structure.customer.Customers;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.FormDraft;
import se.streamsource.streamflow.web.domain.structure.form.MailSelectionMessage;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedFormValue;
import se.streamsource.streamflow.web.domain.structure.form.SubmittedForms;
import se.streamsource.streamflow.web.domain.structure.organization.AccessPoint;
import se.streamsource.streamflow.web.domain.structure.project.filter.Filters;
import se.streamsource.streamflow.web.domain.structure.user.ProxyUser;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;
import se.streamsource.streamflow.web.infrastructure.attachment.OutputstreamInput;
import se.streamsource.streamflow.web.rest.service.mail.MailSenderService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
@Mixins(TaskFormDraftSummaryContext.Mixin.class)
public interface TaskFormDraftSummaryContext extends Context, MailSender, IndexContext<FormDraftDTO>
{

   void submitandsend();

   RequiredSignaturesValue signatures();

   StringValue mailselectionmessage();

   void enablemailmessage();

   void disablemailmessage();

   void changeemailstobenotified(StringValue message);

   abstract class Mixin implements TaskFormDraftSummaryContext
   {
      protected ApplyFilterContext applyFilterContext;
      private HtmlMailGenerator htmlGenerator;

      public Mixin(@Structure Module module, @This MailSender mailSender, @Service AttachmentStore attachmentStore)
      {
         applyFilterContext = new ApplyFilterContext(module, mailSender, attachmentStore);
         htmlGenerator = module.objectBuilderFactory().newObject( HtmlMailGenerator.class );
      }
      
      @Structure
      Module module;

      @Service
      PdfGeneratorService pdfGenerator;

      @Uses
      Locale locale;

      @Optional
      @Service
      MailSenderService mailSender;

      @Structure
      ValueBuilderFactory vbf;

      @Service
      AttachmentStore attachmentStore;

      final Logger logger = LoggerFactory.getLogger( SubmittedForms.class.getName() );

      public FormDraftDTO index()
      {
         return RoleMap.role( FormDraftDTO.class );
      }

      public void submitandsend()
      {

         FormDraft formSubmission = RoleMap.role( FormDraft.class );
         Case aCase = RoleMap.role( Case.class );

         // Get the signature
         FormSignatureDTO signature = formSubmission.getFormDraftValue().signatures().get().get( 0 );

         StringBuilder queryBuilder = new StringBuilder();
         queryBuilder.append( " type:se.streamsource.streamflow.web.domain.entity.customer.CustomerEntity" );
         queryBuilder.append( " contactId:" + signature.signerId().get() );
         Query<Customer> query = module.queryBuilderFactory()
               .newNamedQuery( Customer.class, module.unitOfWorkFactory().currentUnitOfWork(), "solrquery" )
               .setVariable( "query", queryBuilder.toString() );
         Customer customer = null;
         if (query.iterator().hasNext())
         {
            customer = query.iterator().next();
         } else
         {
            Customers customers = module.unitOfWorkFactory().currentUnitOfWork().get( Customers.class, CustomersEntity.CUSTOMERS_ID );
            customer = customers.createCustomerById( signature.signerId().get(), signature.name().get());
         }

         RoleMap.current().set( customer , CustomerEntity.class );

         // Add contact info for signatories
         ContactBuilder builder = new ContactBuilder( module.valueBuilderFactory() );
         builder.name( signature.signerName().get() ).contactId( signature.signerId().get() );
         aCase.addContact( builder.newInstance() );

         aCase.submitForm( formSubmission, customer );

         FormDraftDTO form = role( FormDraftDTO.class );

         if (form.mailSelectionEnablement().get() != null && form.mailSelectionEnablement().get())
         {
            try
            {
               SubmittedForms.Data data = RoleMap.role( SubmittedForms.Data.class );

               SubmittedFormValue submittedFormValue = null;
               for (SubmittedFormValue value : data.submittedForms().get())
               {

                  if (value.form().get().identity().equals( form.form().get().identity() ))
                  {
                     submittedFormValue = value;
                  }
               }
               if (submittedFormValue != null)
               {
                  // find all form attachments and attach them to the email as
                  // well
                  List<AttachedFileValue> formAttachments = new ArrayList<AttachedFileValue>();
                  for (SubmittedFieldValue value : submittedFormValue.fields())
                  {
                     FieldValueDefinition.Data field = module.unitOfWorkFactory().currentUnitOfWork()
                           .get( FieldValueDefinition.Data.class, value.field().get().identity() );
                     if (field.fieldValue().get() instanceof AttachmentFieldValue)
                     {
                        if (!Strings.empty( value.value().get() ))
                        {
                           AttachmentFieldSubmission currentFormDraftAttachmentField = module.valueBuilderFactory()
                                 .newValueFromJSON( AttachmentFieldSubmission.class, value.value().get() );
                           AttachmentEntity attachment = module
                                 .unitOfWorkFactory()
                                 .currentUnitOfWork()
                                 .get( AttachmentEntity.class,
                                       currentFormDraftAttachmentField.attachment().get().identity() );

                           ValueBuilder<AttachedFileValue> formAttachment = module.valueBuilderFactory()
                                 .newValueBuilder( AttachedFileValue.class );
                           formAttachment
                                 .prototype()
                                 .mimeType()
                                 .set( URLConnection.guessContentTypeFromName( currentFormDraftAttachmentField.name()
                                       .get() ) );
                           formAttachment.prototype().uri().set( attachment.uri().get() );
                           formAttachment.prototype().modificationDate().set( attachment.modificationDate().get() );
                           formAttachment.prototype().name().set( currentFormDraftAttachmentField.name().get() );
                           formAttachment.prototype().size()
                                 .set( attachmentStore.getAttachmentSize( attachment.uri().get() ) );
                           formAttachments.add( formAttachment.newInstance() );
                        }
                     }
                  }
                  notifyByMail( submittedFormValue, form.enteredEmails().get(), formAttachments );
               }
            } catch (Throwable throwable)
            {
               logger.error( "Could not send mail", throwable );
            }
         }

         // Apply filter if they exist.
         Owner owner = ((CaseEntity)aCase).owner().get();
         if (owner instanceof Filters.Data)
         {
            applyFilterContext.rebind((Filters.Data) owner, (CaseEntity)aCase).applyFilters();
         }
      }

      public RequiredSignaturesValue signatures()
      {
         RequiredSignatures.Data data = role( RequiredSignatures.Data.class );

         ValueBuilder<RequiredSignaturesValue> valueBuilder = module.valueBuilderFactory().newValueBuilder(
               RequiredSignaturesValue.class );

         valueBuilder.prototype().signatures().get()
               .add( data.requiredSignatures().get().get( 1 ).<RequiredSignatureValue> buildWith().newInstance() );

         return valueBuilder.newInstance();
      }

      public StringValue mailselectionmessage()
      {
         String message = RoleMap.current().get( MailSelectionMessage.Data.class ).mailSelectionMessage().get();
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         if (message == null)
         {
            message = "";
         }
         builder.prototype().string().set( message );
         return builder.newInstance();
      }

      public void enablemailmessage()
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.enableEmailMessage();
      }

      public void disablemailmessage()
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.disableEmailMessage();
      }

      public void changeemailstobenotified(StringValue message)
      {
         FormDraft formDraft = role( FormDraft.class );
         formDraft.changeEmailsToBeNotified( message );
      }

      private PDDocument generatePdf(SubmittedFormValue submittedFormValue) throws Throwable
      {
         FormDraftDTO form = role( FormDraftDTO.class );

         FormPdfTemplate.Data selectedTemplate = role( FormPdfTemplate.Data.class );
         AttachedFile.Data template = (AttachedFile.Data) selectedTemplate.formPdfTemplate().get();

         if (template == null)
         {
            ProxyUser proxyUser = role( ProxyUser.class );
            template = (AttachedFile.Data) ((FormPdfTemplate.Data) proxyUser.organization().get()).formPdfTemplate()
                  .get();

            if (template == null)
            {
               template = (AttachedFile.Data) ((DefaultPdfTemplate.Data) proxyUser.organization().get())
                     .defaultPdfTemplate().get();
            }
         }
         String uri = null;
         if (template != null)
         {
            uri = template.uri().get();
         }

         CaseId.Data idData = role( CaseId.Data.class );

         return pdfGenerator.generateSubmittedFormPdf( submittedFormValue, idData, uri, locale );
      }

      private void notifyByMail(SubmittedFormValue form, String emails, List<AttachedFileValue> formAttachments)
            throws Throwable
      {
         String[] mails = emails.split( "," );
         PDDocument document = generatePdf( form );

         // TODO handle case attachments: also attach them to the mail
         AccessPoint role = role( AccessPoint.class );
         Date submittedOn = form.submissionDate().get();

         mailFormPDF( role.getDescription(), submittedOn, document, formAttachments, mails );
      }

      private void mailFormPDF(String accessPointName, Date submittedOn, PDDocument document,
            List<AttachedFileValue> formAttachments, String... recipients)
      {
         ResourceBundle bundle = ResourceBundle.getBundle( TaskFormDraftSummaryContext.class.getName(), locale );

         try
         {
            String id = addToAttachmentStore( document );
            for (String recipient : recipients)
            {
               ValueBuilder<EmailValue> builder = vbf.newValueBuilder( EmailValue.class );

               // leave from address and fromName empty to allow mail sender to
               // pick up
               // default values from mail sender configuration
               builder.prototype().subject().set( accessPointName );
               builder.prototype().content().set( htmlGenerator.createMailContent( bundle.getString( "mail_notification_body" ), "" ) );
               builder.prototype().contentType().set( Translator.HTML );
               builder.prototype().to().set( recipient );

               List<AttachedFileValue> attachments = builder.prototype().attachments().get();
               ValueBuilder<AttachedFileValue> attachment = vbf.newValueBuilder( AttachedFileValue.class );
               attachment.prototype().mimeType().set( "application/pdf" );
               attachment.prototype().uri().set( "store:" + id );
               attachment.prototype().modificationDate().set( submittedOn );
               attachment.prototype().name().set( accessPointName + ".pdf" );
               attachment.prototype().size().set( attachmentStore.getAttachmentSize( id ) );
               attachments.add( attachment.newInstance() );

               if (formAttachments.size() > 0)
               {
                  for (AttachedFileValue formAttachment : formAttachments)
                  {
                     attachments.add( formAttachment );
                  }
               }
               mailSender.sentEmail( builder.newInstance() );
            }
         } catch (Throwable throwable)
         {
            logger.error( "Could not send mail", throwable );
         }
      }

      private String addToAttachmentStore(final PDDocument pdf) throws Throwable
      {

         // Store case as PDF for attachment purposes
         ValueBuilder<CaseOutputConfigDTO> config = vbf.newValueBuilder( CaseOutputConfigDTO.class );
         config.prototype().attachments().set( true );
         config.prototype().contacts().set( true );
         config.prototype().conversations().set( true );
         config.prototype().submittedForms().set( true );
         config.prototype().caselog().set( true );
         RoleMap.current().set( new Locale( "SV", "se" ) );

         String id = attachmentStore.storeAttachment( new OutputstreamInput( new Visitor<OutputStream, IOException>()
         {
            public boolean visit(OutputStream out) throws IOException
            {
               COSWriter writer = new COSWriter( out );

               try
               {
                  writer.write( pdf );
               } catch (COSVisitorException e)
               {
                  throw new IOException( e );
               } finally
               {
                  writer.close();
               }

               return true;
            }
         }, 4096 ) );
         pdf.close();

         return id;
      }

   }
}