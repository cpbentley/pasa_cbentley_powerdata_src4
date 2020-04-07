package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.structs.IntBufferMatrix;
import pasa.cbentley.core.src4.structs.IntToObjects;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.CharSearchSession;

/**
 * 
 * @author Charles Bentley
 *
 */
public class TrieSearchSession extends CharSearchSession implements ITechSearchTrie, IPrefixSearchSession {

   static final int          SEPARATOR_MARKER = -1;

   /**
    * 
    */
   public int                customIndex;

   public ISearchSession[]   subs;

   public IntToObjects       insides;

   public boolean            isDeepFirst;

   /**
    * When a search is looking into the base  CharTrie and also in auxilliary tries?
    * How do you define that? That will depend on the CharTrie implementation.
    * <br>
    * <br>
    * Customs also allows to some word ids to be associated with a prefix, so this word is shown
    * first in the list.
    * When 
    * 
    */
   public int[]              customs;

   /**
    * Each line contains the nodes for constructing the results prefixed by prefix.
    */
   IntBufferMatrix           dataBuffer;

   /**
    * 
    */
   IntBuffer                 familyHolder;

   /**
    * Tracks the last visited nodes. This can be seen as a row in all the Trie entries.
    * <br>
    * <br>
    * When looking for strings prefixed with 'ea'
    * <li>eat
    * <li>eaten
    * <br>
    * <br>
    * 
    */
   public IntBuffer          lastVisitedRow;

   /**
    * Temporary node value holder used by the CharTrie when searching down.
    * <br>
    * <br>
    * Once the {@link IPowerCharTrie#search(TrieSearchSession)} call returns, it holds the node of the last word.
    */
   public int                node;

   /**
    * The nodes being explored. Used to continue the framing of a search.
    * <br>
    * For a "" prefix, these are the children nodes of the root node.
    * <br>
    * For searching for "ea" prefix, it will be the node of the prefix.
    */
   IntBuffer                 nodesBeingExplored;

   /**
    * Nodes whose children are fully
    */
   IntBuffer                 nodesFullyExplored;

   /**
    * Children nodes of the current prefix root node. 
    */
   IntBuffer                 nodesLastFamily;

   /**
    * Set when prefix was not found.
    */
   boolean                   prefixNotFound;

   boolean                   isSearching      = false;

   /**
    * The Trie Nodes that represent the prefix.
    * <br>
    * <br>
    * Once built, the Trie search can re-use it.
    * <br>
    * <br>
    * {@link ICharTrie#getWord(int)}
    */
   IntBuffer                 prefixNodes;

   /**
    * The node of the prefix.
    * 
    * Prefix "prem" might be matched to node "premi" because i is internally merged to prem.
    * <br>
    * <br>
    * The last node in the prefix.
    * <br>
    * <br>
    * Reset to {@link IPowerTrieNodes#NOT_A_NODE}, which is used to check if prefix.
    * 
    */
   int                       searchRootNode   = IPowerTrieNodes.NOT_A_NODE;

   StringBBuilder            sb;

   /**
    * Base String of the current search.
    */
   StringBBuilder            stringCurrent;

   /**
    * We have no idea how is implemented the Nodedata.
    * Maybe it is using {@link IPowerTrieNodesChar}
    * but maybe something else.
    */
   private PowerCharTrieRoot trie;

   /**
    * Words, Leaves, Non Leaves. Non Words
    */
   private int               searchType;

   /**
    * History of {@link IntToStrings} while doing a search. This allows
    * to quickly returns a result after a remove
    */
   private IntToObjects      history;

   public TrieSearchSession(PDCtx pdc, PowerCharTrieRoot cr, ByteObject tech) {
      super(pdc, cr, tech);
      trie = cr;
      searchType = tech.get1(SEARCH_TRIE_OFFSET_02_TYPE1);
      UCtx uc = pdc.getUCtx();
      dataBuffer = new IntBufferMatrix(uc);

      familyHolder = new IntBuffer(uc, 6);
      nodesBeingExplored = new IntBuffer(uc, 6);

      nodesFullyExplored = new IntBuffer(uc, 6);

      nodesLastFamily = new IntBuffer(uc, 6);
      prefixNodes = new IntBuffer(uc);
      
      stringCurrent = new StringBBuilder(pdc.getUCtx());
      sb = new StringBBuilder(pdc.getUCtx());

   }

   public void buildNodes1(StringBBuilder sb) {
      int prefixSize = sb.getCount();
      //when using frame
      int start = getStart();

      int end = getEnd(); //end exclusive offset in buffer matrix

      //iterate over the framed IntBufferMatrix
      for (int i = start; i < end; i++) {

         IntBuffer mybuf = dataBuffer.getRow(i);
         if (mybuf != null) {
            int rowOffset = 1 + prefixSize;
            int rowLen = mybuf.getSize() - prefixSize;
            trie.buildString(mybuf.getIntsRef(), rowOffset, rowLen, sb);
            //the pointer for later getWord or identification
            int lastnode = mybuf.getLast();
            String finalString = sb.toString();

            //another structure may decide to reject a string based on some tests
            if (acceptString(finalString)) {
               result.add(lastnode, finalString);
               //reset stringbuilder to the prefix
            } else {
               //increment end
               end++;
            }
            sb.setCount(prefixSize);
         } else {
            //break as soon as we find a null
            break;
         }
      }
   }

   /**
    * The trie search session may have string acceptors.
    * <br>
    * <br>
    * If String has already been found in the session (string was found is higher priority trie)
    * <br>
    * <br>
    *  
    * @param finalString
    * @return
    */
   public boolean acceptString(String finalString) {
      return true;
   }

   /**
    * We delegate the search to the {@link IPowerCharTrie} implementation.
    * <br>
    *  
    * When first call is made
    */
   public void search() {
      trie.search(this);
      searchFrameCount++;
   }

   /**
    * 
    * @return
    */
   public int getEnd() {
      int frameSize = getFrameSize();
      if (frameSize == 0) {
         return dataBuffer.getSize();
      } else {
         return getStart() + frameSize;
      }
   }

   public int getStart() {
      int frameSize = getFrameSize();
      if (frameSize == 0) {
         return 0;
      } else {
         return searchFrameCount * frameSize;
      }
   }

   public void algoX(IPowerTrieNodesChar nodedata) {
      //row to be cloned
      int count = 0;
      int currentNode = 0;
      boolean isFinished = false;
      //for alphabetical order add the children node in reverse order so first node is processed first in the loop
      boolean isReverse = !isReverse();
      int frameSize = getFrameSize();
      if (frameSize == 0) {
         frameSize = trie.getSize();
      }
      //algo runs untils all nodes are processed
      while (nodesBeingExplored.getSize() != 0 && !isFinished) {
         //pop next node to inspect
         currentNode = nodesBeingExplored.getLast();
         //when a separator marker is found. it means we are at the end of a family.
         if (currentNode == TrieSearchSession.SEPARATOR_MARKER) {
            //removes the last node
            int val = familyHolder.removeLast();
            sb.decrementCount(val);
            //removes the last child of this family
            nodesBeingExplored.removeLast();
            //and remove the parent. all child have been inspected and parent also has been inspected
            nodesBeingExplored.removeLast();
            continue;
         }
         int prevC = sb.getCount();
         nodedata.appendNodeChars(currentNode, sb);
         int num = sb.getCount() - prevC;
         familyHolder.addInt(num);

         //adds marker for new family
         nodesBeingExplored.addInt(TrieSearchSession.SEPARATOR_MARKER);
         //for alphabetical order add the children node in reverse order so first node is processed first in the loop
         int added = nodedata.getNodeChildren(currentNode, nodesBeingExplored, isReverse);

         if (searchType == SEARCTRIE_TYPE_0_WORDS) {
            if (nodedata.hasNodeFlag(currentNode, PowerCharTrie.FLAG_1_WORD)) {
               //build the string at current row.
               result.add(currentNode, sb.toString());
               count++;
               if (count == frameSize) {
                  isFinished = true;
               }
            }
         } else if (searchType == SEARCTRIE_TYPE_1_LEAVES) {
            if (added == 0) {
               result.add(currentNode, sb.toString());
               count++;
               if (count == frameSize) {
                  isFinished = true;
               }
            }
         }
      }
   }

   /**
    * 
    * @param o
    * @return
    */
   public boolean wasAdded(Object o) {
      if (insides == null) {
         insides = new IntToObjects(pdc.getUCtx());
      }
      boolean b = insides.hasObject(o);
      if (!b) {
         insides.add(o);
      }
      return b;
   }

   protected void reset() {
      super.reset();
      node = 0;
      customIndex = 0;
      customs = null;
      prefixNodes.clear();
      familyHolder.clear();
      nodesBeingExplored.clear();
      dataBuffer.clear();
      lastVisitedRow = null;
      searchRootNode = IPowerTrieNodes.NOT_A_NODE;
      result.nextempty = 0;
   }

   //#mdebug

   public void toString1Line(Dctx dc) {
      dc.root(this, "TrieSearchSession");
   }
   //#enddebug

   /**
    * 
    */
   public void toString(Dctx sb) {
      sb.root(this, "TrieSearchSession");
      sb.nl();
      pdc.getTechFactory().toStringTechTrieSearch(sb.nLevel(), tech);
      sb.nl();
      sb.append("searchRootNode=" + searchRootNode);
      ///
      if (searchRootNode != IPowerTrieNodes.NOT_A_NODE) {
         sb.append(trie.getChars(searchRootNode));
      }
      sb.nlLvl("NodesFullyExplored", nodesFullyExplored);
      ////
      sb.nlLvl("NodesBeingExplored", nodesBeingExplored);
      ////////////////////////
      sb.nlLvl("lastVisitedRow", lastVisitedRow);
      sb.nlLvl("familyHolder", familyHolder);
      ////////////////////////
      sb.nlLvl("prefixNodes", prefixNodes);
      ////
      sb.nlLvl("Current Results", result);
   }

   public void addSession(ISearchSession issNewWords) {
      issNewWords.subordinateTo(this);
   }

   public void addResults(IntToStrings searchWait) {
      // TODO Auto-generated method stub

   }

   /**
    * TODO While a thread is managing the previous 
    * a new thread might add a character.
    * When adding a new characters, the previous thread stops.
    * <br>
    * <br>
    * Right now assume single thread
    */
   public IntToStrings searchAdd(String c) {
      //what do we have already?
      //if the prefix node 

      return super.searchAdd(c);
   }

   /**
    * For the special case of a single remove. show results from previous history
    */
   public IntToStrings searchRemove(int v) {
      return super.searchRemove(v);
   }

   /**
    * Inspect the last
    */
   public char[] nextPossibleChars() {
      //implementation for a trie is easy
      //get search root node
      if (stringCurrent.getCount() > charsLen) {
         // case Prefix "prem" is be matched to node "premi" because i is internally merged to prem.
         //return i
         return new char[] { stringCurrent.charAt(charsLen) };
      } else {
         //right on a node
         int s = nodesBeingExplored.getSize();
         char[] c = new char[s];
         for (int i = 0; i < c.length; i++) {
            c[i] = (char) nodesBeingExplored.get(i);
         }
         return c;
      }
   }

   //#enddebug

}
