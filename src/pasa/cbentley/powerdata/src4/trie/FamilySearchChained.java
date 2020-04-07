package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Binary search is impossible
 * <br>
 * <br>
 * @author Charles Bentley
 *
 */
public class FamilySearchChained {

   private IPowerTrieNodesChar nodedata;

   private PDCtx               pdc;

   public FamilySearchChained(PDCtx pdc, IPowerTrieNodesChar nodedata) {
      this.pdc = pdc;
      this.nodedata = nodedata;
   }

   /**
    * As specified by {@link IPowerTrieNodesChar#nodeFindCharChild(char, int)}
    * <br>
    * <br>
    * 
    * @param mychar
    * @param trienode
    * @return {@link IPowerTrieNodes#CHILD_NODE_NOT_FOUND}
    */
   public int findChildNode(char mychar, int node) {
      int nextSister = nodedata.getNodeChildFirst(node);
      while (nextSister != IPowerTrieNodes.NOT_A_NODE) {
         char firstchar = nodedata.getNodeChar(nextSister);
         if (mychar == firstchar) {
            return nextSister;
         }
         nextSister = nodedata.getNodeSisterRight(nextSister);
      }
      return IPowerTrieNodes.CHILD_NODE_NOT_FOUND;
   }

   public int findChildNode(char mychar, int node, ICharComparator icc) {
      if (icc == null) {
         return findChildNode(mychar, node);
      } else {
         int nextSister = nodedata.getNodeChildFirst(node);
         while (nextSister != IPowerTrieNodes.NOT_A_NODE) {
            char firstchar = nodedata.getNodeChar(nextSister);
            if (icc.isEqual(mychar, firstchar)) {
               return nextSister;
            }
            nextSister = nodedata.getNodeSisterRight(nextSister);
         }
         return IPowerTrieNodes.CHILD_NODE_NOT_FOUND;
      }
   }

   /**
    * The position or would be position of mychar as the children of node. 
    * <br>
    * <br>
    * 
    * @param mychar
    * @param node
    * @return
    */
   public int getFamilyPosition(char mychar, int node) {
      int nextSister = nodedata.getNodeChildFirst(node);
      int count = 0;
      while (nextSister != IPowerTrieNodes.NOT_A_NODE) {
         char firstchar = nodedata.getNodeChar(nextSister);
         int comp = pdc.getUCtx().getCU().compareChar(mychar, firstchar);
         if (comp == -1) {
            return count;
         }
         if (comp == 0) {
            throw new RuntimeException("Family Position already taken:  firstChar=" + firstchar + " : " + mychar);
         }
         count++;
         nextSister = nodedata.getNodeSisterRight(nextSister);
      }
      return count;
   }
}
