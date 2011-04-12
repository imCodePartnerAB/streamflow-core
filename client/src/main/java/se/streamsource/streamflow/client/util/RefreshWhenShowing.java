/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.client.util;

import javax.swing.*;
import java.awt.event.*;

/**
 * Refresh a Refreshable when a component becomes showing on screen.
 */
public class RefreshWhenShowing
      implements HierarchyListener
{
   private Refreshable refreshable;
   private JComponent component;

   public RefreshWhenShowing( JComponent component, Refreshable refreshable )
   {
      this.refreshable = refreshable;
      this.component = component;

      component.addHierarchyListener( this );
   }

   public void hierarchyChanged( HierarchyEvent e )
   {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED)>0 && component.isShowing())
         refresh();
   }

   private void refresh()
   {
      SwingUtilities.invokeLater( new Runnable()
      {
         public void run()
         {
            refreshable.refresh();
         }
      });
   }
}
