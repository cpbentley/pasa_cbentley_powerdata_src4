package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNode;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public class FamilySearchPacked {

   private IPowerTrieNodesChar nodedata;

   private boolean             useBinarySearch;

   private PDCtx pdc;

   public FamilySearchPacked(PDCtx pdc, IPowerTrieNodesChar nodedata) {
      this.pdc = pdc;
      this.nodedata = nodedata;
   }

   /**
    * Finds a matching child {@link TrieNode} for character c for TrieNode node
    * <br>
    * <br>
    * For a character c, a TrieNode can only have one child
    * <br>
    * <br>
    * Returns -1 if no matching child node which means either the family is empty, or the node does not have c as a child
    * <br>
    * <br>
    * TODO This code is identical too all instance of family based Node Data.
    * <br>
    * <br>
    * 
    * @param mychar
    * @param trienode
    * @return -1 if no matching child node either the family is empty, or the node does not have c as a child
    */
   public int findChildNode(char mychar, int trienode) {
      TrieNode da = nodedata.getNode(trienode);
      if (da.familySize == 0) {
         return -1;
      }
      if (da.familySize == 1 || da.familySize == 2) {
         return findChildNodeLinear(mychar, da);
      }
      //logn search is in practice not usefull because there are very few big families with a natural language Trie.
      if (useBinarySearch) {
         return findChildNodeLogn(mychar, da);
      } else {
         return findChildNodeLinear(mychar, da);
      }
      // the number of nodes for generation leader
   }

   /**
    * TODO parametrize the function for checking matching.
    * <br>
    * Introduce
    * <br>
    * <br>
    * @param mychar
    * @param da
    * @return
    */
   private int findChildNodeLinear(char mychar, TrieNode da) {
      for (int i = 0; i < da.familySize; i++) {
         char firstchar = nodedata.getNodeChar(da.childPointer + i);
         if (mychar == firstchar) {
            return da.childPointer + i;
         }
      }
      return IPowerTrieNodes.CHILD_NODE_NOT_FOUND;
   }

   private int findChildNodeLogn(char mychar, TrieNode da) {
      // the number of nodes for generation leader
      int compare_count = 1;
      int lowerbound = da.childPointer;
      int upperbound = da.childPointer + da.familySize - 1;
      // calculate initial search position.
      int node = (lowerbound + upperbound) / 2;
      char c = '+';
      while ((lowerbound <= upperbound) && ((c = nodedata.getNodeChar(node)) != mychar)) {
         compare_count++;
         if (pdc.getUCtx().getCU().compareChar(c, mychar) == 1) {
            upperbound = node - 1; // search position minus one.
         } else {
            lowerbound = node + 1; // Else, change lowerbound to search position plus one.
         }
         //the log(n) factor
         node = (lowerbound + upperbound) / 2;
      }
      if (c == mychar) {
         //System.out.println("char="+mychar+" Found the child at " + position + " in " + compare_count + " comparisons for a family size of " + familySize);
         return node;
      } else
         return IPowerTrieNodes.CHILD_NODE_NOT_FOUND;
   }

   /**
    * Returns the would be node for mychar
    */
   public int getFamilyPosition(char mychar, int node) {
      int familySize = nodedata.getNodeFamilySize(node);
      int childPointer = nodedata.getNodeChildFirst(node);
      for (int i = 0; i < familySize; i++) {
         //get first character of the child
         char firstchar = nodedata.getNodeChar(childPointer + i);
         int comp = pdc.getUCtx().getCU().compareChar(mychar, firstchar);
         if (comp == -1) {
            //insert
            return childPointer + i;
         }
         //this is the case where first char is equal. this cannot not be
         if (comp == 0) {
            throw new RuntimeException("Family Position already taken:  firstChar=" + firstchar + " : " + mychar);
         }
      }
      //append at the end
      return childPointer + familySize;
   }

}
