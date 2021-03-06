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
package se.streamsource.streamflow.web.infrastructure.caching;

import java.io.Serializable;

public class CaseCountItem implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 2215168683765433245L;

   int count;
   int unread;

   public void addToCount(int increment)
   {
      count += increment;
   }

   public void addToUnread(int increment)
   {
      unread += increment;
   }

   public int getCount()
   {
      return count;
   }

   public int getUnread()
   {
      return unread;
   }

}
