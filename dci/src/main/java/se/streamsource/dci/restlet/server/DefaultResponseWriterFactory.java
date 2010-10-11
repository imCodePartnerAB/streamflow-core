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

package se.streamsource.dci.restlet.server;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.Value;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.Qi4jSPI;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.value.ValueDescriptor;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.velocity.ValueCompositeContext;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * JAVADOC
 */
public class DefaultResponseWriterFactory
      implements ResponseWriterFactory
{
   @Structure
   UnitOfWorkFactory uowf;

   @Structure
   Qi4jSPI spi;

   VelocityEngine velocity;

   @Service
   MetadataService metadataService;

   private Template linksHtmlTemplate;
   private Template linksAtomTemplate;
   private Template formHtmlTemplate;
   private Template valueHtmlTemplate;
   private Template resourceHtmlTemplate;

   public DefaultResponseWriterFactory( @Service VelocityEngine velocity ) throws Exception
   {
      this.velocity = velocity;

      URL inputStream = getClass().getResource( "/velocity.properties" );

      if (inputStream == null)
         throw new IllegalStateException("Could not find velocity.properties in classpath");

      Properties properties = new Properties();
      properties.load( inputStream.openStream() );

      velocity.init( properties );

      linksHtmlTemplate = velocity.getTemplate( "rest/template/links.htm" );
      linksAtomTemplate = velocity.getTemplate( "rest/template/links.atom" );
      formHtmlTemplate = velocity.getTemplate( "rest/template/form.htm" );
      valueHtmlTemplate = velocity.getTemplate( "rest/template/value.htm" );
      resourceHtmlTemplate = velocity.getTemplate( "rest/template/resource.htm" );
   }

   public ResponseWriter createWriter( List<String> segments, Class resultType, RoleMap roleMap, Variant variant )
         throws Exception
   {
      if (resultType == null)
      {
         final String extension = metadataService.getExtension( variant.getMediaType() );

         String tn = templateName( segments, extension );
         Template template = resolveTemplate( new File( tn ) );

         if (template != null)
         {
            return new VelocityResponseWriter( template, roleMap, variant );
         } else
         {
            return new NoContentResponseWriter();
         }
      } else
      {
         if (LinkValue.class.isAssignableFrom( resultType ) && !variant.getMediaType().equals( MediaType.APPLICATION_JSON ))
         {
            return new RedirectResponseWriter();
         } else if (Representation.class.isAssignableFrom( resultType ))
         {
            // Return representation as-is
            // TODO refactor this into its own factory
            return new RepresentationResponseWriter( variant );
         } else if (variant.getMediaType().equals( MediaType.APPLICATION_JSON ) && Value.class.isAssignableFrom( resultType ))
         {
            return new JsonResponseWriter( variant, roleMap );
         } else if (ValueDescriptor.class.isAssignableFrom( resultType))
         {
            if (variant.getMediaType().equals( MediaType.APPLICATION_JSON ))
            {
               return new JsonValueDescriptorWriter();
            } else
               return new VelocityResponseWriter( formHtmlTemplate, roleMap, variant );
         } else
         {
            final String extension = metadataService.getExtension( variant.getMediaType() );

            String tn = templateName( segments, extension );
            Template template = resolveTemplate( new File( tn ) );

            if (template != null)
            {
               return new VelocityResponseWriter( template, roleMap, variant );
            } else
            {
               // Check if links, then try default templates
               if (LinksValue.class.isAssignableFrom( resultType))
               {
                  // Use standard links rendering templates
                  if (variant.getMediaType().equals(MediaType.TEXT_HTML))
                  {
                     return new VelocityResponseWriter( linksHtmlTemplate, roleMap, variant );
                  } else if (variant.getMediaType().equals( MediaType.APPLICATION_ATOM ))
                  {
                     return new VelocityResponseWriter( linksAtomTemplate, roleMap, variant );
                  }
               } else if (ValueDescriptor.class.equals(resultType))
               {
                  return new VelocityResponseWriter( formHtmlTemplate, roleMap, variant );
               } else if (ValueComposite.class.isAssignableFrom( resultType ))
               {
                  // Look for type specific template
                  try
                  {
                     template = velocity.getTemplate( "rest/template/"+resultType.getInterfaces()[0].getSimpleName()+"."+extension );
                  } catch (ResourceNotFoundException e)
                  {
                     template = valueHtmlTemplate;
                  }

                  return new VelocityResponseWriter( template, roleMap, variant );
               }

               throw new IllegalArgumentException( "Cannot handle URL with this variant" );
            }
         }
      }
   }

   private String templateName( List<String> segments, String extension )
   {
      String templateName = "";
      for (String segment : segments)
      {
         if (!(segment.equals( "" ) || segment.equals( "." )))
            templateName += "/" + segment;
         else
            templateName += "/resource";
      }
      templateName += "." + extension;

      return templateName;
   }

   private Template resolveTemplate( File templateName ) throws Exception
   {

      Template template = null;
      do
      {
         try
         {
            template = velocity.getTemplate( new File( "rest", templateName.toString() ).toString() );
         } catch (ResourceNotFoundException e)
         {
            // If we can't find the specific template, then check if we are looking for the
            // resource template, and if so, use the default one
            if (templateName.getName().equals("resource.htm"))
               template = resourceHtmlTemplate;
            else
            {
               // Try looking up the stack
               File parentFile = templateName.getParentFile();
               if (parentFile.toString().equals( "/" ))
                  return null;

               templateName = new File( parentFile.getParentFile(), templateName.getName() );
            }
         }

      } while (template == null);

      return template;
   }

   private class VelocityResponseWriter implements ResponseWriter
   {
      private Template template;
      private RoleMap roleMap;
      private Variant variant;

      public VelocityResponseWriter( Template template, RoleMap roleMap, Variant variant )
      {
         this.template = template;
         this.roleMap = roleMap;
         this.variant = variant;
      }

      public void write( final Object result, final Request request, final Response response )
      {
         Representation rep = new WriterRepresentation( variant.getMediaType() )
         {
            @Override
            public void write( Writer writer ) throws IOException
            {
               VelocityContext context = new VelocityContext();
               context.put( "request", request );
               context.put( "response", response );
               context.put( "roleMap", VelocityResponseWriter.this.roleMap );

               Object contextResult = result;

               if (contextResult instanceof Value)
                  contextResult = new ValueCompositeContext((ValueComposite) contextResult);

               context.put( "result", contextResult );
               template.merge( context, writer );
            }
         };
         rep.setCharacterSet( variant.getCharacterSet() );
         rep.setLanguages( variant.getLanguages() );

         response.setStatus( Status.SUCCESS_OK );
         response.setEntity( rep );
      }
   }

   private class RepresentationResponseWriter implements ResponseWriter
   {
      private Variant variant;

      public RepresentationResponseWriter( Variant variant )
      {
         this.variant = variant;
      }

      public void write( Object result, Request request, Response response ) throws ResourceException
      {
         Representation rep = (Representation) result;

         // Ignore media type - interaction has already chosen!
         response.setStatus( Status.SUCCESS_OK );
         response.setEntity( rep );
      }
   }

   private class JsonResponseWriter implements ResponseWriter
   {
      private Variant variant;
      private Date lastModified;
      private Tag tag;

      public JsonResponseWriter( Variant variant, RoleMap roleMap )
      {
         this.variant = variant;

         try
         {
            EntityComposite entity = roleMap.get( EntityComposite.class );
            EntityState state = spi.getEntityState( entity );
            lastModified = new Date( state.lastModified());
            tag = new Tag(state.identity().identity()+"/"+state.version());
         } catch (IllegalArgumentException e)
         {
            // Ignore
         }
      }

      public void write( Object result, Request request, Response response ) throws ResourceException
      {
         StringRepresentation representation = new StringRepresentation( ((Value) result).toJSON(),
               MediaType.APPLICATION_JSON,
               variant.getLanguages().get( 0 ),
               variant.getCharacterSet() );

         if (tag != null)
         {
            representation.setModificationDate( lastModified );
            representation.setTag( tag );
         }

         response.setEntity( representation );
         response.setStatus( Status.SUCCESS_OK );
      }
   }

   private class JsonValueDescriptorWriter implements ResponseWriter
   {
      public void write( Object result, Request request, Response response ) throws ResourceException
      {
         JSONObject json = new JSONObject();

         ValueDescriptor vd = (ValueDescriptor) result;

         try
         {
            for (PropertyDescriptor propertyDescriptor : vd.state().properties())
            {
               Object o = propertyDescriptor.initialValue();
               if (o == null)
                  json.put( propertyDescriptor.qualifiedName().name(), JSONObject.NULL );
               else
                  json.put(propertyDescriptor.qualifiedName().name(), o.toString());
            }
         } catch (JSONException e)
         {
            e.printStackTrace();
         }

         StringRepresentation representation = new StringRepresentation( json.toString(),
               MediaType.APPLICATION_JSON,
               Language.ENGLISH,
               CharacterSet.UTF_8 );

         response.setEntity( representation );
         response.setStatus( Status.SUCCESS_OK );
      }
   }
}
