/*
 * Copyright (c) 2009, Arvid Huss. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.infrastructure.index;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.spi.query.EntityFinderException;
import org.qi4j.spi.query.NamedEntityFinder;
import org.qi4j.spi.query.NamedQueryDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SolrEntityQueryMixin
      implements NamedEntityFinder
{
   @Service
   private EmbeddedSolrService solr;


   public Iterable<EntityReference> findEntities( NamedQueryDescriptor queryDescriptor, String resultType, @Optional Map<String, Object> variables, @Optional OrderBy[] orderBySegments, @Optional Integer firstResult, @Optional Integer maxResults ) throws EntityFinderException
   {
      try
      {
         SolrServer server = solr.getSolrServer();

         NamedList list = new NamedList();

         list.add( "q", queryDescriptor.compose(variables, orderBySegments, firstResult, maxResults));
         list.add( "rows", maxResults != 0 ? maxResults : 10000 );
         list.add( "start", firstResult );

         if (orderBySegments != null && orderBySegments.length > 0)
         {
            for (OrderBy orderBySegment : orderBySegments)
            {
               String propName = orderBySegment.propertyReference().propertyName()+"_for_sort";
               String order = orderBySegment.order() == OrderBy.Order.ASCENDING ? "asc" : "desc";
               list.add( "sort", propName+" "+order );

            }
         }

         QueryResponse query = server.query( SolrParams.toSolrParams( list ) );
         SolrDocumentList results = query.getResults();

         List<EntityReference> references = new ArrayList<EntityReference>( results.size() );
         for (SolrDocument result : results)
         {
            references.add( EntityReference.parseEntityReference( result.getFirstValue( "id" ).toString() ) );
         }
         return references;

      } catch (SolrServerException e)
      {
         throw new EntityFinderException( e );
      }
   }

   public EntityReference findEntity( NamedQueryDescriptor name, String resultType, Map<String, Object> variables ) throws EntityFinderException
   {
      return null;
   }

   public long countEntities( NamedQueryDescriptor name, String resultType, @Optional Map<String, Object> variables ) throws EntityFinderException
   {
      return 0;
   }

   public String showQuery( NamedQueryDescriptor namedQueryDescriptor )
   {
      return null;
   }
}
