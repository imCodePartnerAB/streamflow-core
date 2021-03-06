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
package se.streamsource.streamflow.web.rest.resource.events;

import static java.util.Collections.reverse;
import static org.qi4j.api.io.Outputs.collection;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.util.DateFunctions;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.atom.Feed;
import org.restlet.ext.atom.Text;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import se.streamsource.streamflow.infrastructure.event.application.ApplicationEvent;
import se.streamsource.streamflow.infrastructure.event.application.TransactionApplicationEvents;
import se.streamsource.streamflow.infrastructure.event.application.source.ApplicationEventSource;
import se.streamsource.streamflow.infrastructure.event.application.source.helper.ApplicationEvents;

/**
 * Get application events before or after a given date in various formats
 */
public class ApplicationEventsServerResource
      extends ServerResource
{
   @Service
   ApplicationEventSource source;

   public ApplicationEventsServerResource()
   {
      getVariants().add(new Variant( MediaType.TEXT_HTML));
      getVariants().add(new Variant( MediaType.APPLICATION_ATOM));
      getVariants().add(new Variant( MediaType.APPLICATION_JSON));
   }

   @Override
   protected Representation get( Variant variant ) throws ResourceException
   {
      try
      {
         String before = null;
         String after = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "after" );

         final List<TransactionApplicationEvents> events = new ArrayList<TransactionApplicationEvents>();

         // Get transactions first
         if (after != null)
         {
            final long afterDate = Long.parseLong( after );

            source.transactionsAfter( afterDate, 100 ).transferTo( collection(events ));

         } else
         {
            before = getRequest().getResourceRef().getQueryAsForm().getFirstValue( "before" );

            final long beforeDate = before == null ? System.currentTimeMillis() : Long.parseLong( before );

            source.transactionsBefore( beforeDate, 100 ).transferTo( collection(events ));
            reverse( events );
         }

         if (variant.getMediaType().equals( MediaType.TEXT_HTML ))
         {
            WriterRepresentation representation = new WriterRepresentation( MediaType.TEXT_HTML )
            {
               public void write( final Writer writer ) throws IOException
               {
                  String earlier = events.size() == 0 ? System.currentTimeMillis()+"" : events.get( 0 ).timestamp().get().toString();
                  String later = events.size() == 0 ? System.currentTimeMillis()+"" : events.get( events.size()-1).timestamp().get().toString();

                  writer.append( "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                        "<head>\n" +
                        "    <title>Application events</title>\n" +
                        "</head>\n" +
                        "\n" +
                        "<body>\n" +
                        "<a href=\"?before="+earlier+"\">Earlier</a> | \n" +
                        "<a href=\"?after="+later+"\">Later</a>\n" +
                        "<table style=\"font-size:10\" border=\"1\" cellpadding=\"0\">\n" +
                        "    <tr>\n" +
                        "        <th>Usecase</th>\n" +
                        "        <th>Event</th>\n" +
                        "        <th>Timestamp</th>\n" +
                        "        <th>Parameters</th>\n" +
                        "    </tr>\n");

                  for (ApplicationEvent domainEvent : ApplicationEvents.events( events ))
                  {
                     writer.append( "<tr><td>" ).append( domainEvent.usecase().get() ).
                           append("</td><td>").append( domainEvent.name().get() ).
                           append("</td><td>").append( DateFunctions.toUtcString( domainEvent.on().get()) ).
                           append("</td><td>").append( domainEvent.parameters().get() ).
                           append("</td></tr>");
                  }

                  writer.append("</table>\n" +
                        "</body>\n" +
                        "\n" +
                        "</html>" );
               }
            };

            representation.setCharacterSet( CharacterSet.UTF_8 );
            return representation;
         } else if (variant.getMediaType().equals( MediaType.APPLICATION_ATOM ))
         {
            final Feed feed = new Feed();
            feed.setTitle( new Text("Application events"+ (after == null ? " before "+before : " after "+after)));

            for (TransactionApplicationEvents applicationEvents : events)
            {
               Entry entry = new Entry();
               entry.setPublished( new Date( applicationEvents.timestamp().get()) );
               Content content = new Content();
               content.setInlineContent( new StringRepresentation( applicationEvents.toJSON(), MediaType.APPLICATION_JSON) );
               entry.setContent( content );
               feed.getEntries().add( entry );
            }

            WriterRepresentation representation = new WriterRepresentation( MediaType.APPLICATION_ATOM )
            {
               public void write( final Writer writer ) throws IOException
               {
                  feed.write( writer );
               }
            };

            representation.setCharacterSet( CharacterSet.UTF_8 );
            return representation;
         } else
         {
            throw new ResourceException( Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
         }
      } catch (IOException e)
      {
         throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
      }
   }
}
