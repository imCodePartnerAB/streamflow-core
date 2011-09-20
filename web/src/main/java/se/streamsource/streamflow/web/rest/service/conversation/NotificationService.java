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

package se.streamsource.streamflow.web.rest.service.conversation;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.streamflow.api.workspace.cases.contact.ContactEmailDTO;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.domain.replay.DomainEventPlayer;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.EventRouter;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.TransactionTracker;
import se.streamsource.streamflow.web.application.mail.EmailValue;
import se.streamsource.streamflow.web.application.mail.MailSender;
import se.streamsource.streamflow.web.domain.interaction.gtd.CaseId;
import se.streamsource.streamflow.web.domain.interaction.profile.MessageRecipient;
import se.streamsource.streamflow.web.domain.structure.caze.History;
import se.streamsource.streamflow.web.domain.structure.conversation.Conversation;
import se.streamsource.streamflow.web.domain.structure.conversation.ConversationOwner;
import se.streamsource.streamflow.web.domain.structure.conversation.Message;
import se.streamsource.streamflow.web.domain.structure.conversation.MessageReceiver;
import se.streamsource.streamflow.web.domain.structure.conversation.Messages;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.EmailTemplates;
import se.streamsource.streamflow.web.domain.structure.user.Contactable;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Send and receive notifications. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a notification to the provided recipient.
 */
@Mixins(NotificationService.Mixin.class)
public interface NotificationService
      extends Configuration, Activatable, MailSender, ServiceComposite
{
   class Mixin
         implements Activatable
   {
      final Logger logger = LoggerFactory.getLogger( NotificationService.class.getName() );

      @Service
      private EventSource eventSource;

      @Service
      private EventStream stream;

      @Structure
      private Module module;

      @This
      private Configuration<NotificationConfiguration> config;

      @This
      private MailSender mailSender;

      private TransactionTracker tracker;

      @Service
      DomainEventPlayer player;

      private SendEmails sendEmails = new SendEmails();

      Map<String, String> templateDefaults = new HashMap<String, String>();
      private EmailAccessPoints eap;

      public void activate() throws Exception
      {
         // Get defaults for emails
         ResourceBundle bundle = ResourceBundle.getBundle(EmailTemplates.class.getName());
         for (String key : bundle.keySet())
         {
            templateDefaults.put(key, bundle.getString(key));
         }

         // Store reference to EAP
         UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork(UsecaseBuilder.newUsecase("Update templates"));
         eap = module.queryBuilderFactory().newQueryBuilder(EmailAccessPoints.class).newQuery(uow).find();
         uow.discard();

         EventRouter router = new EventRouter();
         router.route( Events.withNames( SendEmails.class ), Events.playEvents( player, sendEmails, module.unitOfWorkFactory(), UsecaseBuilder.newUsecase("Send email to participant" )) );

         tracker = new TransactionTracker( stream, eventSource, config, Events.adapter( router ) );
         tracker.start();
      }

      public void passivate() throws Exception
      {
         tracker.stop();
      }

      public class SendEmails
            implements MessageReceiver.Data
      {
         public void receivedMessage( DomainEvent event, Message message )
         {
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

            try
            {
               Message.Data messageData = (Message.Data) message;

               Conversation conversation = messageData.conversation().get();

               ConversationOwner owner = conversation.conversationOwner().get();

               String sender = ((Contactable.Data) messageData.sender().get()).contact().get().name().get();
               String caseId = "n/a";

               if (owner != null)
                  caseId = ((CaseId.Data) owner).caseId().get() != null ? ((CaseId.Data) owner).caseId().get() : "n/a";

               MessageReceiver user = uow.get( MessageReceiver.class, event.entity().get() );

               MessageRecipient.Data recipientSettings = (MessageRecipient.Data) user;

               if (recipientSettings.delivery().get().equals( MessageRecipient.MessageDeliveryTypes.email ))
               {
                  String subject;
                  String formattedMsg;

                  History history = (History) owner;
                  EmailAccessPoint emailAccessPoint = history.getOriginalEmailAccessPoint();

                  if (emailAccessPoint != null)
                  {
                     formattedMsg = message.translateBody(emailAccessPoint.emailTemplates().get());
                     subject = MessageFormat.format(emailAccessPoint.subject().get(), caseId, conversation.getDescription());
                  } else
                  {
                     formattedMsg = message.translateBody(templateDefaults);
                     subject = "[" + caseId + "] " + conversation.getDescription(); // Default subject format
                  }

                  if (formattedMsg.trim().equals(""))
                     return; // Don't try to send empty messages

                  ContactEmailDTO recipientEmail = ((Contactable.Data)user).contact().get().defaultEmail();
                  if (recipientEmail != null)
                  {
                     ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder(EmailValue.class);
                     builder.prototype().fromName().set( sender );

                     if (emailAccessPoint != null)
                        builder.prototype().from().set(emailAccessPoint.getDescription() );

      //               builder.prototype().replyTo();
                     builder.prototype().to().set( recipientEmail.emailAddress().get() );
                     builder.prototype().subject().set( subject );
                     builder.prototype().content().set( formattedMsg );
                     builder.prototype().contentType().set( "text/plain" );

                     // Threading headers
                     builder.prototype().messageId().set( "<"+conversation.toString()+"/"+ URLEncoder.encode(user.toString(), "UTF-8")+"@Streamflow>" );
                     ManyAssociation<Message> messages = ((Messages.Data)conversation).messages();
                     StringBuilder references = new StringBuilder();
                     String inReplyTo = null;
                     for (Message previousMessage : messages)
                     {
                        if (references.length() > 0)
                           references.append( " " );

                        inReplyTo = "<"+previousMessage.toString()+"/"+URLEncoder.encode(user.toString(), "UTF-8")+"@Streamflow>";
                        references.append( inReplyTo );
                     }
                     builder.prototype().headers().get().put( "References", references.toString() );
                     if (inReplyTo != null)
                        builder.prototype().headers().get().put( "In-Reply-To", inReplyTo );

                     EmailValue emailValue = builder.newInstance();

                     mailSender.sentEmail( null, emailValue );
                  }
               }
            } catch (Throwable e)
            {
               logger.error("Could not send notification", e);
            }
         }
      }
   }
}