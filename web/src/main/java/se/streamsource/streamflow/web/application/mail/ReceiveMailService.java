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
package se.streamsource.streamflow.web.application.mail;

import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.Usecase;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.spi.service.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.infrastructure.NamedThreadFactory;
import se.streamsource.infrastructure.circuitbreaker.CircuitBreaker;
import se.streamsource.infrastructure.circuitbreaker.service.AbstractEnabledCircuitBreakerAvailability;
import se.streamsource.infrastructure.circuitbreaker.service.ServiceCircuitBreaker;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.application.defaults.SystemDefaultsService;
import se.streamsource.streamflow.web.application.security.UserPrincipal;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationEntity;
import se.streamsource.streamflow.web.domain.entity.organization.OrganizationsEntity;
import se.streamsource.streamflow.web.domain.entity.user.UserEntity;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFileValue;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoint;
import se.streamsource.streamflow.web.domain.structure.organization.EmailAccessPoints;
import se.streamsource.streamflow.web.domain.structure.organization.OrganizationalUnit;
import se.streamsource.streamflow.web.domain.structure.organization.Organizations;
import se.streamsource.streamflow.web.domain.structure.project.Member;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.streamflow.web.domain.structure.user.UserAuthentication;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.qi4j.api.usecase.UsecaseBuilder.*;

/**
 * Receive mail. This service
 * listens for domain events, and on "receivedMessage" it will send
 * a mail to the provided recipient. Furthermore a mail receiver is started that
 * polls the mail inbox for new replies which can be added to conversations.
 */
@Mixins({ReceiveMailService.Mixin.class, AbstractEnabledCircuitBreakerAvailability.class})
public interface ReceiveMailService
        extends Configuration, Activatable, MailReceiver, ServiceComposite, ServiceCircuitBreaker, AbstractEnabledCircuitBreakerAvailability
{
   abstract class Mixin
           implements Activatable, ReceiveMailService, Runnable, ServiceCircuitBreaker, VetoableChangeListener
   {
      @Structure
      Module module;

      @This
      Configuration<ReceiveMailConfiguration> config;

      @This
      MailReceiver mailReceiver;

      @Uses
      ServiceDescriptor descriptor;

      @Service
      AttachmentStore attachmentStore;

      @Service
      SystemDefaultsService systemDefaults;

      public Logger logger;

      private ScheduledExecutorService receiverExecutor;

      Authenticator authenticator;
      private Properties props;
      private URLName url;

      private CircuitBreaker circuitBreaker;

      public void activate() throws Exception
      {
         circuitBreaker = descriptor.metaInfo(CircuitBreaker.class);

         logger = LoggerFactory.getLogger(ReceiveMailService.class);

         if (config.configuration().enabled().get())
         {
            UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( newUsecase( "Create Streamflow support structure" ) );
            RoleMap.newCurrentRoleMap();
            RoleMap.current().set( uow.get( UserAuthentication.class, UserEntity.ADMINISTRATOR_USERNAME ) );
            RoleMap.current().set( new UserPrincipal( UserEntity.ADMINISTRATOR_USERNAME ) );
            
            
            Organizations.Data orgs = uow.get( OrganizationsEntity.class, OrganizationsEntity.ORGANIZATIONS_ID );
            OrganizationEntity org = (OrganizationEntity)orgs.organization().get();
            // check for the existance of support structure for mails that cannot be parsed
            RoleMap.current().set( org.getAdministratorRole() );
            
            OrganizationalUnit ou = null;
            Project project = null;
            CaseType caseType = null;

            try
            {
               try
               {
                  ou = org.getOrganizationalUnitByName( systemDefaults.config().configuration().supportOrganizationName().get() );
               } catch (IllegalArgumentException iae)
               {
                  ou = org.createOrganizationalUnit( systemDefaults.config().configuration().supportOrganizationName().get() );
               }

               try
               {
                  project = ou.getProjectByName( systemDefaults.config().configuration().supportProjectName().get() );
               } catch (IllegalArgumentException iae)
               {
                  project = ou.createProject( systemDefaults.config().configuration().supportProjectName().get() );
               }

               try
               {
                  caseType = project.getCaseTypeByName( systemDefaults.config().configuration().supportCaseTypeForIncomingEmailName().get() );
               } catch (IllegalArgumentException iae)
               {
                  caseType = ou.createCaseType( systemDefaults.config().configuration().supportCaseTypeForIncomingEmailName().get() );
                  project.addSelectedCaseType( caseType );
                  project.addMember( RoleMap.current().get( Member.class ) );
               }
            } finally
            {
               uow.complete();
               RoleMap.clearCurrentRoleMap();
            }

            // Authenticator
            authenticator = new javax.mail.Authenticator()
            {
               protected PasswordAuthentication getPasswordAuthentication()
               {
                  return new PasswordAuthentication(config.configuration().user().get(),
                          config.configuration().password().get());
               }
            };

            props = new Properties();

            String protocol = config.configuration().protocol().get();
            props.put("mail." + protocol + ".host", config.configuration().host().get());
            props.put("mail.transport.protocol", protocol);
            props.put("mail." + protocol + ".auth", "true");
            props.put("mail." + protocol + ".port", config.configuration().port().get());

            if (config.configuration().useSSL().get())
            {

               props.setProperty("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
               props.setProperty("mail." + protocol + ".socketFactory.fallback", "false");
               props.setProperty("mail." + protocol + ".socketFactory.port", "" + config.configuration().port().get());
            }

            url = new URLName(protocol, config.configuration().host().get(), config.configuration().port().get(), "",
                    config.configuration().user().get(), config.configuration().password().get());


            circuitBreaker.addVetoableChangeListener(this);
            circuitBreaker.turnOn();

            long sleep = config.configuration().sleepPeriod().get();
            logger.info("Starting scheduled mail receiver thread. Checking every: "
                    + (sleep == 0 ? 10 : sleep) + " min");
            receiverExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ReceiveMail"));
            receiverExecutor.scheduleWithFixedDelay(this, 0, (sleep == 0 ? 10 : sleep), TimeUnit.MINUTES);
         }
      }

      public void passivate() throws Exception
      {
         circuitBreaker.removeVetoableChangeListener(this);

         if (receiverExecutor != null)
         {
            receiverExecutor.shutdown();
            receiverExecutor.awaitTermination(30, TimeUnit.SECONDS);
         }

         logger.info("Mail service shutdown");
      }

      public CircuitBreaker getCircuitBreaker()
      {
         return circuitBreaker;
      }

      public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException
      {
         // Test connection to mail server
         if (evt.getNewValue() == CircuitBreaker.Status.on)
         {
            Session session = javax.mail.Session.getInstance(props, authenticator);
            session.setDebug(config.configuration().debug().get());
            try
            {
               Store store = session.getStore(url);
               store.connect();
               store.close();
            } catch (MessagingException e)
            {
               // Failed - don't allow to turn on circuit breaker
               throw new PropertyVetoException(e.getMessage(), evt);
            }
         }
      }

      public void run()
      {
         Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

         if (!circuitBreaker.isOn())
            return; // Don't try - circuit breaker is off

         boolean expunge = config.configuration().deleteMailOnInboxClose().get();
         if( config.configuration().debug().get() )
         {
            logger.info("Checking email");
            logger.info( "Delete mail on close - " + expunge );
         }

         Session session = javax.mail.Session.getInstance(props, authenticator);
         session.setDebug(config.configuration().debug().get());

         Usecase usecase = newUsecase( "Receive Mail" );
         UnitOfWork uow = null;
         Store store = null;
         Folder inbox = null;
         Folder archive = null;
         boolean archiveExists = false;
         List<Message> copyToArchive = new ArrayList<Message>();
         MimeMessage internalMessage = null;

         try
         {
            store = session.getStore(url);
            store.connect();

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            javax.mail.Message[] messages = inbox.getMessages();
            FetchProfile fp = new FetchProfile();
//            fp.add( "In-Reply-To" );
            inbox.fetch(messages, fp);

            // check if the archive folder is configured and exists
            if( !Strings.empty( config.configuration().archiveFolder().get() )
                  && config.configuration().protocol().get().startsWith( "imap" ) )
            {
               archive = store.getFolder( config.configuration().archiveFolder().get() );

               // if not exists - create
               if( !archive.exists() )
               {
                  archive.create( Folder.HOLDS_MESSAGES );
                  archiveExists = true;
               } else
               {
                  archiveExists = true;
               }

               archive.open( Folder.READ_WRITE );
            }

            for (javax.mail.Message message : messages)
            {
               int tries = 0;
               while ( tries < 3 )
               {
                  uow = module.unitOfWorkFactory().newUnitOfWork( usecase );

                  ValueBuilder<EmailValue> builder = module.valueBuilderFactory().newValueBuilder( EmailValue.class );

                  try
                  {
                     // Force a complete fetch of the message by cloning it to a internal MimeMessage
                     // to avoid "javax.mail.MessagingException: Unable to load BODYSTRUCTURE" problems
                     // f.ex. experienced if the message contains a windows .eml file as attachment!

                     // Beware that all flag and folder operations have to be made on the original message
                     // and not on the internal one!!
                     internalMessage = new MimeMessage( (MimeMessage)message );

                     Object content = internalMessage.getContent();

                     // Get email fields
                     builder.prototype().from().set( ((InternetAddress) internalMessage.getFrom()[0]).getAddress() );
                     builder.prototype().fromName().set( ((InternetAddress) internalMessage.getFrom()[0]).getPersonal() );
                     builder.prototype().subject().set( internalMessage.getSubject() == null ? "" : internalMessage.getSubject() );

                     // Get headers
                     for (Header header : Iterables.iterable( (Enumeration<Header>) internalMessage.getAllHeaders() ))
                     {
                        builder.prototype().headers().get().put( header.getName(), header.getValue() );
                     }

                     // Get all recipients in order - TO, CC, BCC
                     // and provide it to the toaddress method to pick the first possible valid adress
                     builder.prototype().to().set( toaddress( internalMessage.getAllRecipients(), builder.prototype().headers().get().get( "References" ) ) );

                     builder.prototype().messageId().set( internalMessage.getHeader( "Message-ID" )[0] );

                     // Get body and attachments
                     String body = "";
                     // set content initially so it never can become null
                     builder.prototype().content().set( body );

                     if (content instanceof String)
                     {
                        body = content.toString();
                        builder.prototype().content().set( body );
                        String contentTypeString = cleanContentType( internalMessage.getContentType() );
                        builder.prototype().contentType().set( contentTypeString );
                        if( Translator.HTML.equalsIgnoreCase( contentTypeString ))
                        {
                           builder.prototype().contentHtml().set( body );
                        }

                     } else if (content instanceof Multipart)
                     {
                        handleMultipart( (Multipart) content, internalMessage, builder );
                     } else if (content instanceof InputStream)
                     {
                        content = new MimeMessage( session, (InputStream) content ).getContent();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        Inputs.byteBuffer( (InputStream) content, 4096 ).transferTo( Outputs.byteBuffer( baos ) );

                        String data = new String( baos.toByteArray(), "UTF-8" );
                        // Unknown content type - abort
                        // and create failure case
                        String subj = "Unkonwn content type: " + internalMessage.getSubject();
                        builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                        builder.prototype().content().set( body );
                        builder.prototype().contentType().set( internalMessage.getContentType() );
                        systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                        copyToArchive.add( message );
                        if (expunge)
                           message.setFlag( Flags.Flag.DELETED, true );

                        uow.discard();
                        tries = 3;
                        continue;
                     } else
                     {
                        // Unknown content type - abort
                        // and create failure case
                        String subj = "Unkonwn content type: " + internalMessage.getSubject();
                        builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                        builder.prototype().content().set( body );
                        builder.prototype().contentType().set( internalMessage.getContentType() );
                        systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                        copyToArchive.add( message );
                        if (expunge)
                           message.setFlag( Flags.Flag.DELETED, true );

                        uow.discard();
                        logger.error( "Could not parse emails: unknown content type " + content.getClass().getName() );
                        tries = 3;
                        continue;
                     }

                     // make sure mail content fit's into statistic database - truncate on 65.500 characters.
                     if (builder.prototype().content().get().length() > 65000)
                     {
                        builder.prototype().content().set( builder.prototype().content().get().substring( 0, 65000 ) );
                     }

                     // try to reveal if it is a smpt error we are looking at
                     // X-Failed-Recipients is returned by Gmail
                     // X-FC-MachineGenerated is returned by FirstClass
                     // Exchange is following RFC 6522 -  The Multipart/Report Media Type for
                     // the Reporting of Mail System Administrative Messages
                     boolean isSmtpErrorReport =
                           !Strings.empty( builder.prototype().headers().get().get( "X-Failed-Recipients" ) ) ||
                                 (!Strings.empty( builder.prototype().headers().get().get( "X-FC-MachineGenerated" ) )
                                       && "true".equals( builder.prototype().headers().get().get( "X-FC-MachineGenerated" ) )) ||
                                 !Strings.empty( new ContentType( builder.prototype().headers().get().get( "Content-Type" ) ).getParameter( "report-type" ) );

                     if (isSmtpErrorReport)
                     {
                        // This is a mail bounce due to SMTP error - create support case.
                        String subj = "Undeliverable mail: " + builder.prototype().subject().get();

                        builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                        systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                        copyToArchive.add( message );
                        if (expunge)
                           message.setFlag( Flags.Flag.DELETED, true );

                        uow.discard();
                        logger.error( "Received a mail bounce reply: " + body );
                        tries = 3;
                        continue;
                     }

                     if (builder.prototype().to().get().equals( "n/a" ))
                     {
                        // This is a mail has no to address - create support case.

                        String subj = "No TO address: " + builder.prototype().subject().get();

                        builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );
                        systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                        copyToArchive.add( message );
                        if (expunge)
                           message.setFlag( Flags.Flag.DELETED, true );

                        uow.discard();
                        logger.error( "Received a mail without TO address: " + body );
                        tries = 3;
                        continue;
                     }

                     mailReceiver.receivedEmail( null, builder.newInstance() );

                     try
                     {
                        logger.debug( "This is try " + tries );
                        uow.complete();
                        tries = 3;
                     } catch (ConcurrentEntityModificationException ceme)
                     {
                        if (tries < 2 )
                        {
                           logger.debug( "Encountered ConcurrentEntityModificationException - try again " );
                           // discard uow and try again
                           uow.discard();
                           tries++;
                           continue;

                        } else
                        {
                           logger.debug( "Rethrowing ConcurrentEntityModification.Exception" );
                           tries++;
                           throw ceme;
                        }

                     }

                     copyToArchive.add( message );
                     // remove mail on success if expunge is true
                     if (expunge)
                        message.setFlag( Flags.Flag.DELETED, true );

                  } catch (Throwable e)
                  {
                     String subj = "Unknown error: " + internalMessage.getSubject();
                     builder.prototype().subject().set( subj.length() > 50 ? subj.substring( 0, 50 ) : subj );

                     StringBuilder content = new StringBuilder();
                     content.append( "Error Message: " + e.getMessage() );
                     content.append( "\n\rStackTrace:\n\r" );
                     for (StackTraceElement trace : Arrays.asList( e.getStackTrace() ))
                     {
                        content.append( trace.toString() + "\n\r" );
                     }

                     builder.prototype().content().set( content.toString() );
                     // since we create the content of the message our self it's ok to set content type always to text/plain
                     builder.prototype().contentType().set( "text/plain" );

                     // Make sure to address has some value before vi create a case!!
                     if( builder.prototype().to().get() == null )
                     {
                        builder.prototype().to().set( "n/a" );
                     }
                     systemDefaults.createCaseOnEmailFailure( builder.newInstance() );
                     copyToArchive.add( message );
                     if (expunge)
                        message.setFlag( Flags.Flag.DELETED, true );

                     uow.discard();
                     logger.error( "Could not parse emails", e );
                     tries = 3;
                  }

               }
            }

            // copy message to archive if archive exists
            if( archiveExists )
            {
               inbox.copyMessages( copyToArchive.toArray( new Message[0] ), archive );
               archive.close( false );
            }

            inbox.close( config.configuration().deleteMailOnInboxClose().get() );

            store.close();

            if( config.configuration().debug().get() )
            {
                logger.info("Checked email");
            }

            circuitBreaker.success();
         } catch (Throwable e)
         {
            logger.error( "Error in mail receiver: ", e );
            circuitBreaker.throwable(e);

            try
            {
               if (inbox != null && inbox.isOpen())
                  inbox.close(false);

               if (store != null && store.isConnected())
                  store.close();
            } catch (Throwable e1)
            {
               logger.error("Could not close inbox", e1);
            }
         }
      }

      /**
       * Handel multipart messages recursive until we find the first text/html message.
       * Or text/plain if html is not available.
       * @param multipart the multipart portion
       * @param message the message
       * @param builder the email value builder
       * @throws MessagingException
       * @throws IOException
       */
      private void handleMultipart( Multipart multipart, Message message, ValueBuilder<EmailValue> builder )
            throws MessagingException, IOException
      {
         String body = "";

         String contentType = cleanContentType( multipart.getContentType() );

         for (int i = 0, n = multipart.getCount(); i < n; i++)
         {
            BodyPart part = multipart.getBodyPart(i);

            String disposition = part.getDisposition();

            if ((disposition != null) &&
                  ((disposition.equalsIgnoreCase( Part.ATTACHMENT ) ||
                        (disposition.equalsIgnoreCase( Part.INLINE )))))
            {
               builder.prototype().attachments().get().add( createAttachedFileValue( message.getSentDate(), part ) );

            } else
            {
               if (part.isMimeType( Translator.PLAIN ))
               {
                  // if contents is multipart mixed concatenate text plain parts
                  if( "multipart/mixed".equalsIgnoreCase( contentType ) )
                  {
                     body += (String)part.getContent() + "\n\r";
                  } else
                  {
                     body = (String) part.getContent();
                  }
                  builder.prototype().content().set(body);
                  builder.prototype().contentType().set(Translator.PLAIN);

               } else if (part.isMimeType( Translator.HTML ))
               {
                  body = (String) part.getContent();
                  builder.prototype().contentHtml().set(body);
                  builder.prototype().contentType().set( Translator.HTML) ;

               } else if( part.isMimeType( "image/*" ) )
               {
                  builder.prototype().attachments().get().add( createAttachedFileValue( message.getSentDate(), part ) );

               } else if (part.getContent() instanceof Multipart) {
                  handleMultipart( (Multipart)part.getContent(), message, builder );
               }
            }
         }
         // if contentHtml is not empty set the content type to text/html
         if( !Strings.empty( builder.prototype().contentHtml().get() ) )
         {
            builder.prototype().content().set( builder.prototype().contentHtml().get() );
            builder.prototype().contentType().set( Translator.HTML );
         }
      }

      private AttachedFileValue createAttachedFileValue( Date sentDate, BodyPart part ) throws MessagingException, IOException
      {
         // Create attachment
         ValueBuilder<AttachedFileValue> attachmentBuilder = module.valueBuilderFactory().newValueBuilder(AttachedFileValue.class);

         AttachedFileValue prototype = attachmentBuilder.prototype();
         //check contentType and fetch just the first part if necessary
         String contentType = "";
         if(part.getContentType().indexOf( ';' ) == -1 )
            contentType = part.getContentType();
         else
            contentType = part.getContentType().substring( 0, part.getContentType().indexOf( ';' ) );

         prototype.mimeType().set( contentType );
         prototype.modificationDate().set( sentDate );
         String fileName =  part.getFileName();
         prototype.name().set( fileName == null ? "Nofilename" : MimeUtility.decodeText( fileName ) );
         prototype.size().set((long) part.getSize());

         InputStream inputStream = part.getInputStream();
         String id = attachmentStore.storeAttachment( Inputs.byteBuffer( inputStream, 4096 ));
         String uri = "store:"+id;
         prototype.uri().set(uri);
         return attachmentBuilder.newInstance();
      }

      /**
       * Try to determine what address from the to address list should be used as main TO address for
       * the different EmailReceiver's.
       * In case the references header is null we try to find a matching email access point.
       * @param recipients The recipients from the TO address list
       * @param references The references header
       * @return A hopefully valid email address as a string
       */
      private String toaddress( final Address[] recipients, String references )
      {
         String result = "";
         // No recipients found return n/a
         if( recipients == null || recipients.length == 0 )
            return "n/a";

         if (!hasStreamflowReference( references ))
         {
            Organizations.Data organizations = module.unitOfWorkFactory().currentUnitOfWork().get( Organizations.Data.class, OrganizationsEntity.ORGANIZATIONS_ID );
            EmailAccessPoints.Data emailAccessPoints = (EmailAccessPoints.Data) organizations.organization().get();

            Iterable<EmailAccessPoint> possibleAccesspoints = Iterables.filter( new Specification<EmailAccessPoint>()
            {
               public boolean satisfiedBy( final EmailAccessPoint accessPoint )
               {
                  return Iterables.matchesAny( new Specification<Address>()
                  {
                     public boolean satisfiedBy( Address address )
                     {
                        return ((InternetAddress) address).getAddress().equalsIgnoreCase( accessPoint.getDescription() );
                     }
                  }, Arrays.asList( recipients ) );
               }
            }, emailAccessPoints.emailAccessPoints().toList() );

            if (Iterables.count( possibleAccesspoints ) > 0)
               result = Iterables.first( possibleAccesspoints ).getDescription();
            else
               result = ((InternetAddress) recipients[0]).getAddress();

         } else
         {
            result = ((InternetAddress) recipients[0]).getAddress();
         }

         return Strings.empty( result ) ? "n/a" : result.toLowerCase();
      }

      private String cleanContentType( String contentType )
      {
         String type = "";
         if(contentType.indexOf( ';' ) == -1 )
            type = contentType;
         else
            type = contentType.substring( 0, contentType.indexOf( ';' ) );
         return type;
      }
   }
}