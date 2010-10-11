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

package se.streamsource.dci.restlet.client;

import org.apache.velocity.app.VelocityEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.spi.service.importer.NewObjectImporter;
import org.qi4j.spi.structure.ApplicationModelSPI;
import org.qi4j.test.AbstractQi4jTest;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.api.RequiresRoles;
import se.streamsource.dci.api.Role;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.restlet.server.SubResource;
import se.streamsource.dci.restlet.server.SubResources;
import se.streamsource.dci.qi4j.RoleInjectionProviderFactory;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.CommandQueryRestlet2;
import se.streamsource.dci.restlet.server.CommandResult;
import se.streamsource.dci.restlet.server.DefaultResponseWriterFactory;
import se.streamsource.dci.restlet.server.NullCommandResult;
import se.streamsource.dci.restlet.server.ResponseWriterFactory;
import se.streamsource.dci.value.ContextValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.StringValue;

import javax.security.auth.Subject;
import java.io.File;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

/**
 * Test for CommandQueryClient
 */
public class CommandQueryClientTest
      extends AbstractQi4jTest
{
   static public Server server;
   public CommandQueryClient cqc;

   protected ApplicationModelSPI newApplication()
         throws AssemblyException
   {
      ApplicationAssembler assembler = new ApplicationAssembler()
      {
         public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
               throws AssemblyException
         {
            ApplicationAssembly assembly = applicationFactory.newApplicationAssembly( CommandQueryClientTest.this );
            assembly.setMetaInfo( new RoleInjectionProviderFactory() );
            return assembly;
         }
      };
      try
      {
         return qi4j.newApplicationModel( assembler );
      }
      catch (AssemblyException e)
      {
         assemblyException( e );
         return null;
      }
   }

   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.addObjects( RootContext.class, SubContext.class, SubContext2.class, RootResource.class, SubResource1.class );

      module.addObjects( CommandQueryClient.class );

      module.addValues( ContextValue.class, ResourceValue.class );

      module.addValues( TestQuery.class, TestResult.class, TestCommand.class, StringValue.class );
      module.forMixin( TestQuery.class ).declareDefaults().abc().set( "def" );

      module.addObjects( DefaultResponseWriterFactory.class,
            NullCommandResult.class );
      module.importServices( VelocityEngine.class, CommandResult.class, ResponseWriterFactory.class ).importedBy( NEW_OBJECT );
      module.addObjects( VelocityEngine.class,
            RootRestlet.class );

      module.importServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      module.addObjects( InteractionConstraintsService.class );

      module.importServices( MetadataService.class ).importedBy( NewObjectImporter.class );
      module.addObjects( MetadataService.class );

      module.addObjects( DescribableContext.class );
      module.addTransients( TestComposite.class );
   }

   @Before
   public void startWebServer() throws Exception
   {

      server = new Server( Protocol.HTTP, 8888 );
      CommandQueryRestlet2 restlet = objectBuilderFactory.newObjectBuilder( CommandQueryRestlet2.class ).use( new org.restlet.Context() ).newInstance();
      server.setNext( restlet );
      server.start();

      Client client = new Client( Protocol.HTTP );
      Reference ref = new Reference( "http://localhost:8888/" );
      cqc = objectBuilderFactory.newObjectBuilder( CommandQueryClient.class ).use( ref, client, new NullResponseHandler() ).newInstance();
   }

   @After
   public void stopWebServer() throws Exception
   {
      server.stop();
   }

   @Test
   public void testQueryWithValue()
   {
      TestResult result = cqc.query( "querywithvalue", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testQueryWithValueGetDefaults()
   {
      TestQuery result = cqc.query( "querywithvalue", TestQuery.class );

      assertThat( result.toJSON(), equalTo( "{\"abc\":\"def\"}" ) );
   }

   @Test
   public void testCommandWithValueGetDefaults()
   {
      TestCommand result = cqc.query( "commandwithvalue", TestCommand.class );

      assertThat( result.toJSON(), equalTo( "{\"abc\":\"\"}" ) );
   }

   @Test
   public void testQueryWithoutValue()
   {
      TestResult result = cqc.query( "querywithoutvalue", TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testPostCommandWithWrongValue()
   {
      try
      {
         cqc.postCommand( "commandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'wrong'}" ) );
      } catch (ResourceException e)
      {
         assertThat( e.getStatus().getDescription(), equalTo( "Wrong argument" ) );
      }
   }

   @Test
   public void testPostCommandWithRightValue()
   {
      cqc.postCommand( "commandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'right'}" ) );
   }

   @Test
   public void testPutCommandWithRightValue()
   {
      cqc.putCommand( "idempotentcommandwithvalue", valueBuilderFactory.newValueFromJSON( TestCommand.class, "{'abc':'right'}" ) );
   }

   @Test
   public void testSubResourceQueryWithValue()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithvalue", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testRootIndex()
   {
      ResourceValue result = cqc.query( "", ResourceValue.class );

      assertThat( result.toJSON(), equalTo( "{\"commands\":[\"commandwithvalue\",\"idempotentcommandwithvalue\"],\"index\":null,\"queries\":[\"querywithvalue\",\"querywithoutvalue\"],\"resources\":[\"resource\"]}"
 ) );
   }

   @Test
   public void testSubResourceIndex()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      ResourceValue result = cqc2.query( "", ResourceValue.class );

      assertThat( result.toJSON(), equalTo( "{\"commands\":[\"commandwithrolerequirement\",\"changedescription\"],\"index\":null,\"queries\":[\"querywithvalue\",\"querywithrolerequirement\",\"genericquery\",\"description\"],\"resources\":[\"subresource1\",\"subresource2\"]}" ) );
   }

   @Test
   public void testSubResourceQueryWithRoleRequirement()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithrolerequirement", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceQueryWithRoleRequirement2()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithrolerequirement2", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceGenericQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "genericquery", valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ), TestResult.class );

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceCompositeCommandQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      cqc2.postCommand( "changedescription", valueBuilderFactory.newValueFromJSON( StringValue.class, "{'string':'foo'}" ) );
      StringValue result = cqc2.query( "description", StringValue.class );

      assertThat( result.toJSON(), equalTo( "{\"string\":\"foo\"}" ) );
   }

   @Test
   public void testContext()
   {
      UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
      RoleMap rm = new RoleMap();
      rm.set( transientBuilderFactory.newTransient(TestComposite.class ));
      uow.metaInfo().set( rm );

      DescribableContext context = objectBuilderFactory.newObject(DescribableContext.class);

      ValueBuilder<StringValue> vb = valueBuilderFactory.newValueBuilder( StringValue.class );
      vb.prototype().string().set( "Foo" );
      context.changeDescription( vb.newInstance() );

      assertThat(context.description().string().get(), equalTo("Foo"));

      uow.discard();
   }

   public interface TestQuery
         extends ValueComposite
   {
      @UseDefaults
      Property<String> abc();
   }

   public interface TestCommand
         extends ValueComposite
   {
      Property<String> abc();
   }

   public interface TestResult
         extends ValueComposite
   {
      Property<String> xyz();
   }

   public static class RootRestlet
         extends CommandQueryRestlet2
   {
      @Override
      protected Uniform createRoot( Request request, Response response )
      {
         return module.objectBuilderFactory().newObjectBuilder( RootResource.class ).newInstance();
      }
   }

   public static class RootResource
         extends CommandQueryResource
         implements SubResources
   {
      private static TestComposite instance;


      public RootResource()
      {
         super( RootContext.class );
      }

      public void querywithvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "querywithvalue", request, response ) );
      }

      public void querywithoutvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "querywithoutvalue", request, response ) );
      }

      public void commandwithvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "commandwithvalue", request, response ) );
      }

      public void resource( String currentSegment, Request request, Response response )
      {
         RoleMap roleMap = getRoleMap( request );

         roleMap.set( new File( "" ) );

         if (instance == null)
            roleMap.set( instance = module.transientBuilderFactory().newTransient( TestComposite.class ) );
         else
            roleMap.set( instance );

         subResource( SubResource1.class, request, response );
      }
   }

   public static class SubResource1
         extends CommandQueryResource
   {
      public SubResource1()
      {
         super( SubContext.class, SubContext2.class, DescribableContext.class );
      }

      public void genericquery( Request request, Response response ) throws Throwable
      {
         result( invoke( "genericquery", request, response ) );
      }

      public void querywithvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "querywithvalue", request, response ) );
      }

      public void querywithoutvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "querywithoutvalue", request, response ) );
      }

      public void commandwithvalue( Request request, Response response ) throws Throwable
      {
         result( invoke( "commandwithvalue", request, response ) );
      }

      @SubResource
      public void subresource1( Request request, Response response )
      {
         subResource( SubResource1.class, request, response );
      }

      @SubResource
      public void subresource2( Request request, Response response )
      {
         subResource( SubResource1.class, request, response );
      }
   }

   public static class RootContext
         extends ContextMixin
   {
      private static int count = 0;

      @Structure
      UnitOfWorkFactory uowf;

      @Structure
      ValueBuilderFactory vbf;

      public TestResult queryWithValue( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      public TestResult queryWithoutValue()
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      public void commandWithValue( TestCommand command )
      {
         if (!command.abc().get().equals( "right" ))
            throw new IllegalArgumentException( "Wrong argument" );

         // Done
      }

      public void idempotentCommandWithValue( TestCommand command ) throws ConcurrentEntityModificationException
      {
         // On all but every third invocation, throw a concurrency exception
         // This is to test retries on the server-side
         count++;
         if (count%3 != 0)
         {
            uowf.currentUnitOfWork().addUnitOfWorkCallback( new UnitOfWorkCallback()
            {
               public void beforeCompletion() throws UnitOfWorkCompletionException
               {
                  throw new ConcurrentEntityModificationException( Collections.<EntityComposite>emptyList());
               }

               public void afterCompletion( UnitOfWorkStatus status )
               {
               }
            });
         }

         if (!command.abc().get().equals( "right" ))
            throw new IllegalArgumentException( "Wrong argument" );

         // Done
      }
   }

   public static class SubContext
         extends ContextMixin
   {
      @Structure
      ValueBuilderFactory vbf;

      public TestResult queryWithValue( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      // Test interaction constraints

      @RequiresRoles(File.class)
      public TestResult queryWithRoleRequirement( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      @RequiresRoles(Subject.class)
      public TestResult queryWithRoleRequirement2( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }

      @RequiresRoles(File.class)
      public void commandWithRoleRequirement()
      {
      }

      @RequiresRoles(Subject.class)
      public void commandWithRoleRequirement2()
      {
      }
   }

   public static class SubContext2
         extends ContextMixin
   {
      @Structure
      ValueBuilderFactory vbf;

      public TestResult genericQuery( TestQuery query )
      {
         return vbf.newValueFromJSON( TestResult.class, "{'xyz':'bar'}" );
      }
   }

   public static class DescribableContext
   {
      @Structure
      ValueBuilderFactory vbf;

      @Role
      Describable describable;

      @Role
      DescribableData describableData;

      public StringValue description()
      {
         ValueBuilder<StringValue> builder = vbf.newValueBuilder( se.streamsource.dci.value.StringValue.class );
         builder.prototype().string().set( describableData.description().get() );
         return builder.newInstance();
      }

      public void changeDescription( StringValue newDesc )
      {
         describable.changeDescription( newDesc.string().get() );
      }

      @Mixins(Describable.Mixin.class)
      public interface Describable
      {
         void changeDescription( String newDesc );

         class Mixin
               implements Describable
         {
            @This
            DescribableData data;

            public void changeDescription( String newDesc )
            {
               data.description().set( newDesc );
            }
         }
      }
   }

   public interface DescribableData
   {
      @UseDefaults
      Property<String> description();
   }

   public interface TestComposite
         extends TransientComposite, DescribableData, DescribableContext.Describable
   {

   }
}