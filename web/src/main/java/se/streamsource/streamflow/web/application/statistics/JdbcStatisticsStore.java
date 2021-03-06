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
package se.streamsource.streamflow.web.application.statistics;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.database.Databases;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Statistics store that saves data into a JDBC database
 */
@Mixins(JdbcStatisticsStore.Mixin.class)
public interface JdbcStatisticsStore
      extends ServiceComposite, StatisticsStore, Activatable
{
   class Mixin
         implements StatisticsStore, Activatable
   {
      @Service
      ServiceReference<DataSource> dataSource;

      private Logger log;

      public void activate() throws Exception
      {
         log = LoggerFactory.getLogger( JdbcStatisticsStore.class );
      }

      public void passivate() throws Exception
      {
      }

      public void related( final RelatedStatisticsValue related ) throws StatisticsStoreException
      {
         Databases databases = getDatabases();

         try
         {
            int rows = databases.update( "UPDATE descriptions SET description=?, type=? WHERE id=?", new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement )
                     throws SQLException
               {
                  statement.setString( 1, related.description().get() );
                  statement.setString( 2, related.relatedType().get().name() );
                  statement.setString( 3, related.identity().get() );
               }
            } );

            if (rows == 0)
            {
               // Do insert
               databases.update( "INSERT INTO descriptions (id,description,type) VALUES (?,?,?)", new Databases.StatementVisitor()
               {
                  public void visit( PreparedStatement statement ) throws SQLException
                  {
                     statement.setString( 1, related.identity().get() );
                     statement.setString( 2, related.description().get() );
                     statement.setString( 3, related.relatedType().get().name() );
                  }
               } );
            }
         } catch (SQLException e)
         {
            throw new StatisticsStoreException( "Could not update database", e );
         }
      }

      public void caseStatistics( final CaseStatisticsValue caseStatistics )
            throws StatisticsStoreException
      {
         Databases databases = getDatabases();

         try
         {
            // Delete any old data
            databases.update( "DELETE FROM cases WHERE id=?", new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  statement.setString( 1, caseStatistics.identity().get() );
               }
            } );

            databases.update( "INSERT INTO cases VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  int idx = 1;
                  statement.setString( idx++, caseStatistics.identity().get() );
                  statement.setString( idx++, caseStatistics.caseId().get() );
                  statement.setString( idx++, caseStatistics.description().get() );
                  statement.setString( idx++, caseStatistics.resolutionId().get() );
                  statement.setTimestamp(  idx++, new java.sql.Timestamp( caseStatistics.createdOn().get().getTime() ) );
                  statement.setTimestamp( idx++, new java.sql.Timestamp( caseStatistics.closedOn().get().getTime() ) );
                  statement.setLong( idx++, caseStatistics.duration().get() );

                  statement.setString( idx++, caseStatistics.assigneeId().get() );
                  statement.setString( idx++, caseStatistics.caseTypeId().get() );
                  statement.setString( idx++, caseStatistics.caseTypeOwnerId().get() );
                  statement.setString( idx++, caseStatistics.projectId().get() );
                  statement.setString( idx++, caseStatistics.organizationalUnitId().get() );
                  statement.setString( idx++, caseStatistics.groupId().get() );
                  statement.setTimestamp(  idx++, caseStatistics.dueOn().get() != null ? new java.sql.Timestamp( caseStatistics.dueOn().get().getTime() ) : null );
                  statement.setString( idx++, caseStatistics.priority().get() );
                  statement.setBoolean( idx, false );
                  
               }
            } );

            databases.update( "DELETE FROM labels WHERE id=?", new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  statement.setString( 1, caseStatistics.identity().get() );
               }
            } );

            for (final String label : caseStatistics.labels().get())
            {
               databases.update("INSERT INTO labels (id,label) VALUES (?,?)", new Databases.StatementVisitor()
               {
                  public void visit( PreparedStatement statement ) throws SQLException
                  {
                     statement.setString( 1, caseStatistics.identity().get() );
                     statement.setString( 2, label );
                  }
               });
            }

            databases.update( "DELETE FROM fields WHERE id=?", new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  statement.setString( 1, caseStatistics.identity().get() );
               }
            } );

            for (final FormFieldStatisticsValue field : caseStatistics.fields().get())
            {
               databases.update("INSERT INTO fields (id,form,field,value,datatype) VALUES (?,?,?,?,?)", new Databases.StatementVisitor()
               {
                  public void visit( PreparedStatement statement ) throws SQLException
                  {
                     statement.setString( 1, caseStatistics.identity().get() );
                     statement.setString( 2, field.formId().get() );
                     statement.setString( 3, field.fieldId().get() );
                     statement.setString( 4, field.value().get() );
                     statement.setString( 5, field.datatype().get() );
                  }
               });
            }
         } catch (SQLException exception)
         {
            throw new StatisticsStoreException( "Could not update database", exception );
         }
      }

      public void removedCase( final String id ) throws StatisticsStoreException
      {
         Databases databases = getDatabases();

         try
         {
            Databases.StatementVisitor visitor = new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  statement.setBoolean( 1, true );
                  statement.setString( 2, id );
               }
            };
            databases.update( "UPDATE cases SET deleted=? WHERE id=?", visitor );
         } catch (SQLException e)
         {
            throw new StatisticsStoreException("Could not remove case", e);
         }
      }

      public void structure(OrganizationalStructureValue structureValue) throws StatisticsStoreException
      {
         Databases database = getDatabases();

         try
         {
            database.update("DELETE FROM organization");
            for (final OrganizationalUnitValue organizationalUnitValue : structureValue.structure().get())
            {
               database.update("INSERT INTO organization VALUES (?,?,?,?,?)", new Databases.StatementVisitor()
               {
                  public void visit(PreparedStatement preparedStatement) throws SQLException
                  {
                     preparedStatement.setString(1, organizationalUnitValue.id().get());
                     preparedStatement.setString(2, organizationalUnitValue.name().get());
                     preparedStatement.setInt(3, organizationalUnitValue.left().get());
                     preparedStatement.setInt(4, organizationalUnitValue.right().get());
                     preparedStatement.setString(5, organizationalUnitValue.parent().get());
                  }
               });
            }
         } catch (SQLException e)
         {
            log.error("Could not update organizational structure", e);
            throw new StatisticsStoreException("Could not update organizational structure", e);
         }
      }

      public void clearAll() throws StatisticsStoreException
      {
         Databases databases = getDatabases();

         try
         {
            databases.update( "DELETE FROM descriptions" );
            databases.update( "DELETE FROM labels" );
            databases.update( "DELETE FROM fields" );

            Databases.StatementVisitor visitor = new Databases.StatementVisitor()
            {
               public void visit( PreparedStatement statement ) throws SQLException
               {
                  statement.setBoolean( 1, false );
               }
            };
            databases.update( "DELETE FROM cases where deleted=?", visitor );
         } catch (SQLException e)
         {
            log.error( "Could not remove statistics", e );
            throw new StatisticsStoreException("Could not remove case", e);
         }
      }

      public Databases getDatabases() throws StatisticsStoreException
      {
         try
         {
            return new Databases(dataSource.get());
         } catch (Exception e)
         {
            throw new StatisticsStoreException("DataSource not available", e);
         }
      }
   }
}
