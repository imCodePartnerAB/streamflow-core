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
package se.streamsource.streamflow.client.ui.menu;

import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;

/**
 * File menu
 */
public class HelpMenu
      extends AbstractMenu
{
   @Service
   FileConfiguration config;

   protected void init()
   {
      if (config.os() == FileConfiguration.OS.mac)
      {
         menu( "helpMenu"/*,
               "showHelp"*/ );
      } else
      {
         menu( "helpMenu",
               /*"showHelp",*/
               "showAbout" );
      }
   }
}