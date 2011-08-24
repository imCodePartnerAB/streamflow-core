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

package se.streamsource.streamflow.client.ui.administration;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.TreeList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.ContextItem;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;
import java.util.List;

/**
 * JAVADOC
 */
public class AdministrationModel
   extends ResourceModel<LinksValue>
      implements Refreshable, TransactionListener
{
   @Structure
   Module module;

   private EventList<LinkValue> links = new TransactionList<LinkValue>(new BasicEventList<LinkValue>());
   private TreeList<LinkValue> linkTree = new TreeList<LinkValue>(links, new TreeList.Format<LinkValue>()
   {
      public void getPath(List<LinkValue> linkValues, LinkValue linkValue)
      {
         String classes = linkValue.classes().get();
         if (classes != null)
            for (LinkValue value : links)
            {
               if (classes.contains(value.id().get()))
               {
                  getPath(linkValues, value);
                  break;
               }
            }

         linkValues.add(linkValue);
      }

      public boolean allowsChildren(LinkValue linkValue)
      {
         for (LinkValue link : links)
         {
            String classes = link.classes().get();
            if (classes != null && classes.contains(linkValue.id().get()))
               return true;
         }
         return false;
      }

      public Comparator<? extends LinkValue> getComparator(int i)
      {
         return null;
      }
   }, TreeList.NODES_START_EXPANDED);

   public AdministrationModel()
   {
      relationModelMapping("server", ServerModel.class);
      relationModelMapping("organization", OrganizationModel.class);
      relationModelMapping("organizationalunit", OrganizationalUnitModel.class);
   }

   public void refresh()
   {
      super.refresh();

      LinksValue administration = getIndex();

      EventListSynch.synchronize(administration.links().get(), links);
   }

   public TreeList<LinkValue> getLinkTree()
   {
      return linkTree;
   }

   public void changeDescription( Object node, String newDescription )
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem client = (ContextItem) treeNode.getUserObject();

      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
      builder.prototype().string().set( newDescription );
      client.getClient().postCommand( "changedescription", builder.newInstance() );
   }

   public void createOrganizationalUnit( Object node, String name )
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();

      Form form = new Form();
      form.set("name", name);
      contextItem.getClient().postCommand( "create", form );
   }

   public void removeOrganizationalUnit( Object node )
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      try
      {
         contextItem.getClient().delete();
      } catch (ResourceException e)
      {
         if (Status.SERVER_ERROR_INTERNAL.equals( e.getStatus() ))
         {
            throw new OperationException( AdministrationResources.could_not_remove_organisation_with_open_projects, e);

         }
      }
   }

   public EventList<LinkValue> possibleMoveTo(Object node)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      EventList<LinkValue> links = new BasicEventList<LinkValue>();
      EventListSynch.synchronize(contextItem.getClient().query( "possiblemoveto", LinksValue.class ).links().get(), links);
      return links;
   }

   public void move(Object node, LinkValue link)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      contextItem.getClient().postLink( link );
   }

   public EventList<LinkValue> possibleMergeWith(Object node)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      EventList<LinkValue> links = new BasicEventList<LinkValue>();
      EventListSynch.synchronize(contextItem.getClient().query( "possiblemergewith", LinksValue.class ).links().get(), links);
      return links;
   }

   public void merge(Object node, LinkValue link)
   {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      ContextItem contextItem = (ContextItem) treeNode.getUserObject();
      contextItem.getClient().postLink( link );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
// TODO       if (Events.matches( transactions, Events.onEntities( )))
      refresh();
   }
}
