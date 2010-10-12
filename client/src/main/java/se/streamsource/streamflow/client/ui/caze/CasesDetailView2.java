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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.restlet.data.Reference;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.TransactionEvents;
import se.streamsource.streamflow.infrastructure.event.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.source.TransactionListener;
import se.streamsource.streamflow.infrastructure.event.source.TransactionVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.Events;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

import static se.streamsource.streamflow.infrastructure.event.source.helper.Events.*;
import static se.streamsource.streamflow.util.Iterables.filter;

/**
 * JAVADOC
 */
public class CasesDetailView2
      extends JPanel
   implements TransactionListener
{
   private CaseDetailView current = null;

   @Structure
   ObjectBuilderFactory obf;

   private CardLayout layout = new CardLayout();

   private Reference currentCase;

   public CasesDetailView2( )
   {
      setLayout( layout );
      setBorder( BorderFactory.createEmptyBorder() );

      add( new JLabel( i18n.text( WorkspaceResources.choose_case ), JLabel.CENTER ), "blank" );

      layout.show( this, "blank" );

      setPreferredSize( new Dimension( getWidth(), 500 ) );
   }

   public void show( CommandQueryClient client)
   {
      if (currentCase == null || !currentCase.equals( client.getReference() ))
      {
         if (current != null)
         {
            int tab = current.getSelectedTab();
            currentCase = client.getReference();
            add(current = obf.newObjectBuilder( CaseDetailView.class ).use( client ).newInstance(), "detail");
            current.setSelectedTab( tab );
         } else
         {
            currentCase = client.getReference();
            add(current = obf.newObjectBuilder( CaseDetailView.class ).use( client ).newInstance(), "detail");
            layout.show( this, "detail" );

         }
      }
   }

   public void clear()
   {
      layout.show( this, "blank" );
      current = null;
   }

   @Override
   public boolean requestFocusInWindow()
   {
      return current == null ? false : current.requestFocusInWindow();
   }

   public CaseDetailView getCurrentCaseView()
   {
      return current;
   }

   public void refresh()
   {
      if (current != null)
      {
         layout.show( this, "blank" );
         layout.show( this, "detail" );
      }
   }

   public void notifyTransactions( Iterable<TransactionEvents> transactions )
   {
      if (Events.matches( transactions, Events.withNames("deletedEntity" )))
         layout.show( this, "blank" );
   }
}