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

/**
 * Services that store statistics should implement this interface.
 */
public interface StatisticsStore
{
   /**
    * Add information about a statistics related entity
    *
    * @param related
    * @throws StatisticsStoreException
    */
   void related(RelatedStatisticsValue related)
         throws StatisticsStoreException;

   /**
    * Add statistics for a single case
    * @param caseStatistics
    * @throws StatisticsStoreException
    */
   void caseStatistics(CaseStatisticsValue caseStatistics)
      throws StatisticsStoreException;

   void removedCase(String id)
      throws StatisticsStoreException;

   void structure(OrganizationalStructureValue structureValue) throws StatisticsStoreException;

   /**
    * Clear out all statistics from the store. This is usually
    * done before repopulating the statistics store from scratch.
    *
    */
   void clearAll()
      throws StatisticsStoreException;
}
