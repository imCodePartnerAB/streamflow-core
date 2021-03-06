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
package se.streamsource.dci.restlet.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.qi4j.bootstrap.ImportedServiceDeclaration.NEW_OBJECT;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.composite.TransientComposite;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCallback;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.util.Iterables;
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
import org.restlet.data.Form;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.MetadataService;

import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.InteractionConstraintsService;
import se.streamsource.dci.api.InteractionValidation;
import se.streamsource.dci.api.Requires;
import se.streamsource.dci.api.RequiresValid;
import se.streamsource.dci.api.Role;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.qi4j.RoleInjectionProviderFactory;
import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.CommandQueryRestlet;
import se.streamsource.dci.restlet.server.CommandResult;
import se.streamsource.dci.restlet.server.DCIAssembler;
import se.streamsource.dci.restlet.server.NullCommandResult;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.restlet.server.api.SubResources;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.ValueAssembler;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.Links;

/**
 * Test for CommandQueryClient
 */
public class CommandQueryClientTest
      extends AbstractQi4jTest
{
   static public Server server;
   public CommandQueryClient cqc;

   public static String command = null; // Commands will set this
   private ModuleAssembly module;

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
            assembly.setMetaInfo( new MetadataService() );
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
      module.objects( RootContext.class, SubContext.class, SubContext2.class, RootResource.class, SubResource1.class );

      new ClientAssembler().assemble( module );

      module.values(TestQuery.class, TestResult.class, TestCommand.class);
      module.forMixin( TestQuery.class ).declareDefaults().abc().set("def");


      new ValueAssembler().assemble( module );
      new DCIAssembler().assemble( module );

      module.objects(NullCommandResult.class );
      module.importedServices(CommandResult.class).importedBy( NEW_OBJECT );
      module.objects(RootRestlet.class );

      module.importedServices( InteractionConstraintsService.class ).
            importedBy( NewObjectImporter.class ).
            visibleIn( Visibility.application );
      module.objects( InteractionConstraintsService.class );

      module.importedServices( MetadataService.class ).importedBy( NEW_OBJECT );
      module.objects( MetadataService.class );

      module.objects( DescribableContext.class );
      module.transients( TestComposite.class );
   }

   @Before
   public void startWebServer() throws Exception
   {

      server = new Server( Protocol.HTTP, 8888 );
      CommandQueryRestlet restlet = objectBuilderFactory.newObjectBuilder( CommandQueryRestlet.class ).use( new org.restlet.Context() ).newInstance();
      server.setNext(restlet);
      server.start();

      Client client = new Client( Protocol.HTTP );
      Reference ref = new Reference( "http://localhost:8888/" );
      cqc = objectBuilderFactory.newObjectBuilder( CommandQueryClientFactory.class ).use(client, new NullResponseHandler() ).newInstance().newClient( ref );
   }

   @After
   public void stopWebServer() throws Exception
   {
      server.stop();
   }

   @Test
   public void testQueryWithValue()
   {
      TestResult result = cqc.query( "querywithvalue", TestResult.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat(result.toJSON(), equalTo("{\"xyz\":\"bar\"}"));
   }

   @Test
   public void testQueryWithValueGetDefaults()
   {
      TestQuery result = cqc.query( "querywithvalue", TestQuery.class );

      assertThat( result.toJSON(), equalTo( "{\"abc\":\"def\"}" ) );
   }

   @Test
   public void testQueryWithStringResult()
   {
      String result = cqc.query( "querywithstringresult", String.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat( result, equalTo( "bar") );
   }

   @Test
   public void testQueryWithIntegerResult()
   {
      int result = cqc.query( "querywithintegerresult", Integer.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat( result, equalTo( 7) );
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
      cqc.postCommand("commandwithvalue", valueBuilderFactory.newValueFromJSON(TestCommand.class, "{'abc':'right'}"));
   }

   @Test
   public void testPutCommandWithRightValue()
   {
      cqc.putCommand("idempotentcommandwithvalue", valueBuilderFactory.newValueFromJSON(TestCommand.class, "{'abc':'right'}"));
   }

   @Test
   public void testDelete()
   {
      cqc.delete();

      assertThat( command, equalTo( "delete" ) );
   }

   @Test
   public void testSubResourceQueryWithValue()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithvalue", TestResult.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat(result.toJSON(), equalTo("{\"xyz\":\"bar\"}"));
   }

   @Test
   public void testInteractionValidation()
   {
      CommandQueryClient cqc2 = cqc.getSubClient("subresource");

      LinkValue xyzLink;
      {
         ResourceValue result = cqc2.query();
         xyzLink = Iterables.first( Iterables.filter( Links.withId( "xyz" ), result.commands().get() ) );
         assertThat( xyzLink, CoreMatchers.<Object>notNullValue());
         Form form = new Form();
         form.set( "valid", "false" );
         cqc2.postCommand( xyzLink.rel().get(), form.getWebRepresentation());
      }

      {
         ResourceValue result = cqc2.query();
         LinkValue nullLink = Iterables.first( Iterables.filter( Links.withId( "xyz" ), result.commands().get() ) );
         assertThat( nullLink, CoreMatchers.<Object>nullValue());

      }

      try
      {
         cqc2.postCommand( xyzLink.rel().get(), new StringRepresentation("{valid:false}") );
         Assert.fail("ResourceException should have been thrown");
      } catch (ResourceException e)
      {
         // Ok
      }

      Form form = new Form();
      form.set( "valid", "true" );
      cqc2.postCommand("notxyz", form.getWebRepresentation());
   }

   @Test
   public void testRootIndex()
   {
      ResourceValue result = cqc.query();


      ResourceValue expected = moduleInstance.valueBuilderFactory().newValueFromJSON(ResourceValue.class, "{\"commands\":[{\"classes\":\"command\",\"href\":\"delete\",\"id\":\"delete\",\"rel\":\"delete\",\"text\":\"Delete\"},{\"classes\":\"command\",\"href\":\"commandwithvalue\",\"id\":\"commandwithvalue\",\"rel\":\"commandwithvalue\",\"text\":\"Command with value\"},{\"classes\":\"command\",\"href\":\"idempotentcommandwithvalue\",\"id\":\"idempotentcommandwithvalue\",\"rel\":\"idempotentcommandwithvalue\",\"text\":\"Idempotent command with value\"}],\"index\":null,\"queries\":[{\"classes\":\"query\",\"href\":\"querywithvalue\",\"id\":\"querywithvalue\",\"rel\":\"querywithvalue\",\"text\":\"Query with value\"},{\"classes\":\"query\",\"href\":\"querywithoutvalue\",\"id\":\"querywithoutvalue\",\"rel\":\"querywithoutvalue\",\"text\":\"Query without value\"},{\"classes\":\"query\",\"href\":\"querywithstringresult\",\"id\":\"querywithstringresult\",\"rel\":\"querywithstringresult\",\"text\":\"Query with string result\"},{\"classes\":\"query\",\"href\":\"querywithintegerresult\",\"id\":\"querywithintegerresult\",\"rel\":\"querywithintegerresult\",\"text\":\"Query with integer result\"}],\"resources\":[]}");
      assertThat( result.commands().get().size(), equalTo( expected.commands().get().size() ));
      assertThat( result.queries().get().size(), equalTo( expected.queries().get().size() ));
   }

   @Test
   public void testSubResourceIndex()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      ResourceValue result = cqc2.query();

      ResourceValue expected = moduleInstance.valueBuilderFactory().newValueFromJSON(ResourceValue.class, "{\"commands\":[{\"classes\":\"command\",\"href\":\"xyz\",\"id\":\"xyz\",\"rel\":\"xyz\",\"text\":\"Xyz\"},{\"classes\":\"command\",\"href\":\"commandwithrolerequirement\",\"id\":\"commandwithrolerequirement\",\"rel\":\"commandwithrolerequirement\",\"text\":\"Command with role requirement\"},{\"classes\":\"command\",\"href\":\"changedescription\",\"id\":\"changedescription\",\"rel\":\"changedescription\",\"text\":\"Change description\"}],\"index\":null,\"queries\":[{\"classes\":\"query\",\"href\":\"querywithvalue\",\"id\":\"querywithvalue\",\"rel\":\"querywithvalue\",\"text\":\"Query with value\"},{\"classes\":\"query\",\"href\":\"querywithrolerequirement\",\"id\":\"querywithrolerequirement\",\"rel\":\"querywithrolerequirement\",\"text\":\"Query with role requirement\"},{\"classes\":\"query\",\"href\":\"genericquery\",\"id\":\"genericquery\",\"rel\":\"genericquery\",\"text\":\"Generic query\"},{\"classes\":\"query\",\"href\":\"description\",\"id\":\"description\",\"rel\":\"description\",\"text\":\"Description\"}],\"resources\":[{\"classes\":\"resource\",\"href\":\"subresource1/\",\"id\":\"subresource1\",\"rel\":\"subresource1\",\"text\":\"Subresource 1\"},{\"classes\":\"resource\",\"href\":\"subresource2/\",\"id\":\"subresource2\",\"rel\":\"subresource2\",\"text\":\"Subresource 2\"}]}");
      assertThat( result.commands().get().size(), equalTo( expected.commands().get().size() ));
      assertThat( result.queries().get().size(), equalTo( expected.queries().get().size() ));
   }

   @Test
   public void testSubResourceQueryWithRoleRequirement()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "querywithrolerequirement", TestResult.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceGenericQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      TestResult result = cqc2.query( "genericquery", TestResult.class, valueBuilderFactory.newValueFromJSON( TestQuery.class, "{'abc':'foo'}" ));

      assertThat( result.toJSON(), equalTo( "{\"xyz\":\"bar\"}" ) );
   }

   @Test
   public void testSubResourceCompositeCommandQuery()
   {
      CommandQueryClient cqc2 = cqc.getSubClient( "subresource" );
      Form form = new Form();
      form.set("description", "foo");
      cqc2.postCommand( "changedescription", form );
      String result = cqc2.query( "description", String.class );

      assertThat( result, equalTo( "foo" ) );
   }

   @Test
   public void testContext()
   {
      DescribableContext context = objectBuilderFactory.newObjectBuilder(DescribableContext.class).use(transientBuilderFactory.newTransient(TestComposite.class )).newInstance();
      context.changeDescription( "Foo" );
      assertThat(context.description(), equalTo("Foo"));
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
      @UseDefaults
      Property<String> abc();
   }

   public interface TestResult
         extends ValueComposite
   {
      Property<String> xyz();
   }

   public static class RootRestlet
         extends CommandQueryRestlet
   {
      @Override
      protected Uniform createRoot( Request request, Response response )
      {
         return module.objectBuilderFactory().newObjectBuilder( RootResource.class ).use(this).newInstance();
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

      public TestResult querywithvalue(TestQuery testQuery ) throws Throwable
      {
         return context(RootContext.class).queryWithValue(testQuery);
      }

      public TestResult querywithoutvalue( ) throws Throwable
      {
         return context(RootContext.class).queryWithoutValue();
      }

      public String querywithstringresult(TestQuery query ) throws Throwable
      {
         return context(RootContext.class).queryWithStringResult(query);
      }

      public void commandwithvalue( TestCommand command ) throws Throwable
      {
         context(RootContext.class).commandWithValue(command);
      }

      public void resource( String currentSegment )
      {
         RoleMap roleMap = RoleMap.current();

         roleMap.set( new File( "" ) );

         if (instance == null)
            roleMap.set( instance = module.transientBuilderFactory().newTransient( TestComposite.class ) );
         else
            roleMap.set( instance );

         subResource( SubResource1.class );
      }
   }

   public static class SubResource1
         extends CommandQueryResource
   {
      public SubResource1()
      {
         super( SubContext.class, SubContext2.class, DescribableContext.class );
      }

      public TestResult genericquery( TestQuery query ) throws Throwable
      {
         return context(SubContext2.class).genericQuery(query);
      }

      public TestResult querywithvalue(TestQuery query ) throws Throwable
      {
         return context(SubContext.class).queryWithValue(query);
      }

      @SubResource
      public void subresource1( )
      {
         subResource( SubResource1.class );
      }

      @SubResource
      public void subresource2(  )
      {
         subResource( SubResource1.class );
      }
   }

   public static class RootContext
      implements DeleteContext
   {
      private static int count = 0;

      @Structure
      Module module;

      public TestResult queryWithValue( TestQuery query )
      {
         return module.valueBuilderFactory().newValueFromJSON(TestResult.class, "{'xyz':'bar'}");
      }

      public TestResult queryWithoutValue()
      {
         return module.valueBuilderFactory().newValueFromJSON(TestResult.class, "{'xyz':'bar'}");
      }

      public String queryWithStringResult( TestQuery query )
      {
         return "bar";
      }

      public int queryWithIntegerResult( TestQuery query )
      {
         return 7;
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
            module.unitOfWorkFactory().currentUnitOfWork().addUnitOfWorkCallback( new UnitOfWorkCallback()
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

      public void delete() throws ResourceException, IOException
      {
         // Ok!
         command = "delete";
      }
   }

   public static class SubContext
      implements InteractionValidation
   {
      @Structure
      Module module;

      public TestResult queryWithValue( TestQuery query )
      {
         return module.valueBuilderFactory().newValueFromJSON(TestResult.class, "{'xyz':'bar'}");
      }

      // Test interaction constraints

      @Requires(File.class)
      public TestResult queryWithRoleRequirement( TestQuery query )
      {
         return module.valueBuilderFactory().newValueFromJSON(TestResult.class, "{'xyz':'bar'}");
      }

      @Requires(File.class)
      public void commandWithRoleRequirement()
      {
      }

      // Interaction validation
      private static boolean xyzValid = true;

      @RequiresValid("xyz")
      public void xyz(@Name("valid") boolean valid)
      {
         xyzValid = valid;
      }

      @RequiresValid("notxyz")
      public void notxyz(@Name("valid") boolean valid)
      {
         xyzValid = valid;
      }

      public boolean isValid( String name )
      {
         if (name.equals("xyz"))
            return xyzValid;
         else if (name.equals( "notxyz" ))
            return !xyzValid;
         else
            return false;
      }
   }

   public static class SubContext2
   {
      @Structure
      Module module;

      public TestResult genericQuery( TestQuery query )
      {
         return module.valueBuilderFactory().newValueFromJSON(TestResult.class, "{'xyz':'bar'}");
      }
   }

   public static class DescribableContext
   {
      @Structure
      Module module;

      Describable describable = new Describable();

      public void bind(@Uses DescribableData describableData)
      {
         describable.bind(describableData);
      }

      public String description()
      {
         return describable.description();
      }

      public void changeDescription( @Name("description") String newDesc )
      {
         describable.changeDescription(newDesc);
      }

      public static class Describable
         extends Role<DescribableData>
      {
            public void changeDescription( String newDesc )
            {
               self.description().set( newDesc );
            }

         public String description()
         {
            return self.description().get();
         }
      }
   }

   public interface DescribableData
   {
      @UseDefaults
      Property<String> description();
   }

   public interface TestComposite
         extends TransientComposite, DescribableData
   {
      @Optional
      Property<String> foo();
   }
}
