package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.spi.entity.association.AssociationType;
import org.qi4j.spi.entity.association.ManyAssociationType;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.structure.ModuleSPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ruslan on 03.03.17.
 */
public class EntityExportHelper extends AbstractExportHelper
{

   private List<PropertyType> existsProperties;
   private Iterable<AssociationType> existsAssociations;
   private Iterable<ManyAssociationType> existsManyAssociations;
   private Map<String, Object> subProps;
   private JSONObject entity;
   private ArrayList<PropertyType> allProperties;
   private ArrayList<ManyAssociationType> allManyAssociations;
   private ArrayList<AssociationType> allAssociations;
   private String className;
   private EntityExportService entityExportService;

   public void help() throws Exception
   {

      connection.setAutoCommit( false );

      final String identity = entity.getString( "identity" );
      final ResultSet isExistRS = selectFromWhereId( tableName(), identity );

      checkEntityExists( className, identity );

      if ( isExistRS.next() )
      {
         deleteEntityAndRelations( isExistRS.getString( "identity" ) );
      }
      isExistRS.close();

      final StringBuilder query = mainUpdate();

      final Map<String, String> associations = updateAssociations( query );

      saveManyAssociations();

      final List<SingletonMap> subProperties = saveSubProperties( query );

      query
              .deleteCharAt( query.length() - 1 )
              .append( " WHERE " )
              .append( escapeSqlColumnOrTable( "identity" ) )
              .append( " = ?" );

      final PreparedStatement statement = connection.prepareStatement( query.toString() );
      addArguments( statement, associations, subProperties, identity );
      statement.executeUpdate();
      statement.close();

      connection.commit();

   }

   private void saveManyAssociations() throws Exception
   {
      for ( ManyAssociationType existsManyAssociation : existsManyAssociations )
      {
         String tableName = tableName() + "_" + toSnackCaseFromCamelCase( existsManyAssociation.qualifiedName().name() ) + "_cross_ref";

         final String name = existsManyAssociation.qualifiedName().name();
         final JSONArray array = entity.getJSONArray( name );
         for ( int i = 0; ; i++ )
         {
            final JSONObject arrEl = array.optJSONObject( i );
            if ( arrEl == null )
            {
               break;
            }

            final Class<?> assocClassName = Class.forName( existsManyAssociation.type() );
            int j = 0;
            String associationClass = null;
            for ( EntityInfo entityInfo : EntityInfo.values() )
            {
               if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
               {
                  associationClass = entityInfo.getEntityClass().getName();
                  j++;
               }
            }
            if ( j == 1 )
            {
               checkEntityExists( associationClass, arrEl.getString( "identity" ) );
            }

            final PreparedStatement preparedStatement = connection.prepareStatement( "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                    " (owner_id,link_id) VALUES (?,?)" );
            preparedStatement.setString( 1, entity.getString( "identity" ) );
            preparedStatement.setString( 2, arrEl.getString( "identity" ) );
            preparedStatement.executeUpdate();
            preparedStatement.close();
         }

      }


   }

   private StringBuilder mainUpdate() throws SQLException, JSONException, ClassNotFoundException
   {

      StringBuilder query = new StringBuilder( "UPDATE  " )
              .append( escapeSqlColumnOrTable( tableName() ) )
              .append( " SET " );


      for ( PropertyType existsProperty : existsProperties )
      {
         final String name = existsProperty.qualifiedName().name();

         if ( !name.equals( "identity" ) && subProps.get( name ) == null )
         {
            query
                    .append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                    .append( " = ?," );

         }
      }

      return query;

   }

   private List<SingletonMap> saveSubProperties( StringBuilder query ) throws Exception
   {

      List<SingletonMap> subProperties = new ArrayList<>();
      final Set<String> keys = subProps.keySet();
      for ( String key : keys )
      {

         final Object value = subProps.get( key );

         if ( value instanceof Collection || value instanceof Map )
         {

            if ( !( value instanceof Map ) )
            {
               final Object first = Iterables.first( ( Iterable<?> ) value );
               if ( first instanceof ValueComposite )
               {
                  for ( Object o : ( Collection<?> ) value )
                  {
                     processValueComposite( ( ValueComposite ) o );
                  }
               } else
               {
                  processCollection( value, key );
               }
            } else
            {
               processCollection( value, key );
            }

         } else if ( value instanceof ValueComposite )
         {
            query
                    .append( key )
                    .append( "=?," )
                    .append( key )
                    .append( ValueExportHelper.COLUMN_DESCRIPTION_SUFFIX )
                    .append( "=?," );

            subProperties.add( processValueComposite( ( ValueComposite ) value ) );
         }

      }

      return subProperties;
   }

   private Map<String, String> updateAssociations( StringBuilder query ) throws Exception
   {
      Map<String, String> associations = new LinkedHashMap<>();

      for ( AssociationType existsAssociation : existsAssociations )
      {
         final String name = existsAssociation.qualifiedName().name();
         final JSONObject jsonObject = entity.getJSONObject( name );
         final String identity = jsonObject.getString( "identity" );

         final Class<?> assocClassName = Class.forName( existsAssociation.type().name() );
         String associationClass = null;
         for ( EntityInfo entityInfo : EntityInfo.values() )
         {
            if ( assocClassName.isAssignableFrom( entityInfo.getEntityClass() ) )
            {
               associationClass = entityInfo.getEntityClass().getName();
            }
         }

         checkEntityExists( jsonObject.optString( "_type", associationClass ), identity );
         associations.put( toSnackCaseFromCamelCase( name ), identity );
      }

      if ( associations.size() > 0 )
      {
         final Set<String> keys = associations.keySet();

         for ( String key : keys )
         {
            query
                    .append( escapeSqlColumnOrTable( key ) )
                    .append( " = ?," );
         }

      }

      return associations;
   }

   private void checkEntityExists( String type, String identity ) throws Exception
   {
      final String tableName = toSnackCaseFromCamelCase( classSimpleName( type ) );
      final ResultSet resultSet = selectFromWhereId( tableName, identity );

      if ( !resultSet.next() )
      {
         final String qeury = "INSERT INTO " + escapeSqlColumnOrTable( tableName ) +
                 " (" + escapeSqlColumnOrTable( "identity" ) + ")" + " VALUES (?)";
         final PreparedStatement preparedStatement = connection.prepareStatement( qeury );
         preparedStatement.setString( 1, identity );
         preparedStatement.executeUpdate();
         preparedStatement.close();
      }

   }

   private void deleteEntityAndRelations( String identity ) throws SQLException, ClassNotFoundException, JSONException
   {
      StringBuilder select = new StringBuilder( "SELECT " );

      for ( PropertyType property : allProperties )
      {
         deleteSubProperty( property.qualifiedName().type() );
      }

      //Delete many associations
      for ( ManyAssociationType manyAssociation : allManyAssociations )
      {

         String tableName = tableName() + "_" + toSnackCaseFromCamelCase( manyAssociation.qualifiedName().name() ) + "_cross_ref";
         final String delete = "DELETE FROM " + escapeSqlColumnOrTable( tableName ) + " WHERE owner_id = ?";
         final PreparedStatement preparedStatement = connection.prepareStatement( delete );
         preparedStatement.setString( 1, identity );
         preparedStatement.executeUpdate();
         preparedStatement.close();

      }

      //Set main entity all columns to NULL except identity

      final StringBuilder queryNullUpdate = new StringBuilder( "UPDATE " )
              .append( tableName() )
              .append( " SET " );

      for ( PropertyType property : allProperties )
      {
         final String name = property.qualifiedName().name();
         if ( !name.equals( "identity" ) )
         {
            queryNullUpdate.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                    .append( "=NULL," );
         }
      }

      for ( AssociationType association : allAssociations )
      {
         final String name = association.qualifiedName().name();
         if ( !name.equals( "identity" ) )
         {
            queryNullUpdate.append( escapeSqlColumnOrTable( toSnackCaseFromCamelCase( name ) ) )
                    .append( "=NULL," );
         }
      }

      final String query = queryNullUpdate
              .deleteCharAt( queryNullUpdate.length() - 1 )
              .toString();

      final PreparedStatement preparedStatement = connection.prepareStatement( query );

      preparedStatement.executeUpdate();
      preparedStatement.close();
   }

   private void deleteSubProperty( String type )
   {
      // TODO: 23.03.17
   }

   private void addArguments( PreparedStatement statement,
                             Map<String, String> associations,
                             List<SingletonMap> subProperties,
                             String identity
                             ) throws JSONException, SQLException, ClassNotFoundException
   {
      int i = 1;
      for ( PropertyType existsProperty : existsProperties )
      {
         final Class type = Class.forName( existsProperty.type().type().name() );

         final String name = existsProperty.qualifiedName().name();

         if ( name.equals( "identity" ) || subProps.get( name ) != null )
         {
            continue;
         }

         setSimpleType(statement, type, entity, name, i++);

      }

      for ( String key : associations.keySet() )
      {
         statement.setString( i++, associations.get( key ) );
      }

      for ( SingletonMap subProperty : subProperties )
      {
         statement.setInt( i++, ( Integer ) subProperty.getKey() );
         statement.setString( i++, subProperty.getValue().toString() );
      }

      statement.setString( i, identity );

   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( classSimpleName( className ) );
   }

   //setters


   public void setExistsProperties( Iterable<PropertyType> existsProperties )
   {
      this.existsProperties = new LinkedList<>();
      for ( PropertyType existsProperty : existsProperties )
      {
         this.existsProperties.add( existsProperty );
      }
   }

   public void setExistsAssociations( Iterable<AssociationType> existsAssociations )
   {
      this.existsAssociations = existsAssociations;
   }

   public void setExistsManyAssociations( Iterable<ManyAssociationType> existsManyAssociations )
   {
      this.existsManyAssociations = existsManyAssociations;
   }

   public void setSubProps( Map<String, Object> subProps )
   {
      this.subProps = subProps;
   }

   public void setConnection( Connection connection )
   {
      this.connection = connection;
   }

   public void setEntity( JSONObject entity )
   {
      this.entity = entity;
   }

   public void setAllProperties( Set<PropertyType> allProperties )
   {
      this.allProperties = new ArrayList<>( allProperties );
   }

   public void setAllManyAssociations( Set<ManyAssociationType> allManyAssociations )
   {
      this.allManyAssociations = new ArrayList<>( allManyAssociations );
   }

   public void setAllAssociations( Set<AssociationType> allAssociations )
   {
      this.allAssociations = new ArrayList<>( allAssociations );
   }

   public void setClassName( String className )
   {
      this.className = className;
   }

   public void setModule ( ModuleSPI module )
   {
      this.module = module;
   }

   public void setEntityExportService( EntityExportService entityExportService )
   {
      this.entityExportService = entityExportService;
   }
}
