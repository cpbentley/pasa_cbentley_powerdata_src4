package pasa.cbentley.powerdata.src4.trie;

import java.util.Vector;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.structs.IntBufferMatrix;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.IDataMorphable;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechNodeData;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNodeTopo;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.string.CharColUtilz;

public class PowerCharTrie extends PowerCharTrieRoot implements IPowerCharTrie, ITechCharTrie, IPointerUser {

   /**
    * We want to flag nodes that are actualy words
    * 
    */
   public static final int    FLAG_1_WORD                   = 1;

   public static final int    SAFETY_VALVE                  = 100;

   /**
    * The buffer size for the number of words for prefix searches
    */
   public static final int    SEARCH_DEFAULT_WORD_LIST_SIZE = 20;

   //#enddebug

   public static final int    SEARCH_INTBUFFER_START_SIZE   = 20;

   /**
    * Could be Build or Run
    */
   public IPowerCharCollector charco;

   /**
    * Flags a node which is a dictionnary node
    */
   public int                 FLAG_2_DIC_WORD               = 2;

   /**
    * 
    */
   public IPowerTrieNodesChar nodedata;

   private Vector             pointerUsers;

   public PowerCharTrie(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerCharTrieTechDefault());
   }

   public PowerCharTrie(PDCtx pdc, ByteController mod, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   /**
    * Creates an empty Trie.
    * <br>
    * <br>
    * @param tech the {@link PowerCharTrie} header. Possible with Data. But this method does a def init.
    */
   public PowerCharTrie(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructor();
   }

   private int addCharCoChars(char[] c, int offset, int len) {
      int pointer = IPowerCharCollector.CHARS_NOT_FOUND;
      if (isCharTrieFlagSet(ITechCharTrie.CTRIE_FLAG_1_REUSE_CHARS)) {
         pointer = charco.getPointer(c, offset, len);
      }
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         //if this method mofifies the charco pointers, we have the call-back to the new pointer to re-arrange.
         pointer = charco.addChars(c, offset, len);
      }
      return pointer;
   }

   /**
    * Adds a word to the {@link IPowerCharTrie}. No data link.
    * @param word
    * @param offset
    * @param len
    */
   public int addChars(char[] word, int offset, int len) {
      return addChars(word, offset, len, true);
   }

   /**
    * Main Add Method
    * <br>
    * <br>
    * This method does not make assumptions on how the node store characters.
    * <br>
    * <br>
    * It will ask the {@link IPowerTrieNodesChar} to split {@link IPowerTrieNodesChar#splitNode(int)}.
    * <br>
    * <br>
    * What about the suffix sub trie?
    * <br>
    * <br>
    * 
    * @param look
    * @param offset
    * @param len
    * @param isWord true if it is a user word
    * @return the node id of the string or -1 if failure.
    */
   public int addChars(char[] look, int offset, int len, boolean isWord) {
      methodStarts();
      int nodeResult = insideAddChars(look, offset, len, isWord);
      methodEnds();
      return nodeResult;
   }

   public int addChars(String s) {
      return addChars(s.toCharArray(), 0, s.length());
   }

   public void addPointerUser(IPointerUser pointerUser) {
      if (pointerUsers == null) {
         pointerUsers = new Vector();
      }
      pointerUsers.addElement(pointerUser);
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      char[] cs = getChars(pointer);
      sb.append(cs);
   }

   /**
    * Build a string with the nodes, use the order
    * <br>
    * <br>
    * @param nodes
    * @return "" if bad offset
    */
   private String buildString(int[] nodes, int offset, int len) {
      if (offset >= nodes.length)
         return "";
      int[] charp = new int[len];
      int count = 0;
      for (int i = offset; i < offset + len; i++) {
         charp[count] = nodedata.getNodeCharPointer(nodes[i]);
         count++;
      }
      return new String(charco.getChars(charp));
   }

   /**
    * Appends all the nodes characters in the given order to the {@link StringBuilder}
    * <br>
    * <br>
    * 
    * @param nodes
    * @param offset
    * @param len
    * @param sb
    */
   void buildString(int[] nodes, int offset, int len, StringBBuilder sb) {
      if (offset < nodes.length) {
         for (int i = offset; i < offset + len; i++) {
            nodedata.appendNodeChars(nodes[i], sb);
         }
      }
   }

   protected void checkParentPointers() {
      if (!nodedata.getTech().hasFlag(ITechNodeData.NODEDATA_OFFSET_01_FLAG, ITechNodeData.NODEDATA_FLAG_1_PARENT)) {
         throw new RuntimeException("FLAG_PARENT_NEEDED");
      }
   }

   /**
    * 
    */
   public int copyChars(int pointer, char[] c, int offset) {
      checkParentPointers();
      //safety valve in case parent data is corrupted wich results in an infinite loop
      int count = 0;
      int maxH = getTech().get1(ITechCharCol.CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1);
      int rootNode = nodedata.getRootNode();
      int numCopied = 0;
      int totalNUm = 0;
      while (pointer != rootNode && count != maxH) {
         count++;
         int charP = nodedata.getNodeCharPointer(pointer);
         IPowerCharCollector charco = nodedata.getCharCo();
         numCopied = charco.copyChars(charP, c, offset);
         offset += numCopied;
         totalNUm += numCopied;
         pointer = nodedata.getNodeParent(pointer);
      }
      return totalNUm;
   }

   /**
    * When word is not valid?
    * 
    * return 0.
    */
   public int countWords(int wordid) {
      if (wordid == CHARS_NOT_FOUND || wordid > nodedata.getLastNode()) {
         return 0;
      }
      TrieNodeTopo tnt = new TrieNodeTopo(TrieNodeTopo.TOPO_TYPE_01_NUM_NODES, wordid, false);
      tnt.addFlagCondition(FLAG_1_WORD, true);
      nodedata.getTopology(tnt);
      return tnt.resultCount;
   }

   /**
    * Inclusive of node
    * @param node
    * @param max stops counting once max is reached
    * @return
    */
   public int countWords(int node, int max) {
      //create a topology for counting words
      return 0;
   }

   /**
    * Count the words without building a result table.
    */
   public int countWords(String prefix) {
      int id = getPointer(prefix.toCharArray(), 0, prefix.length());
      return countWords(id);
   }

   public int createLeaf(int parentNode, char[] c, int offset, int len) {
      return createLeaf(parentNode, c, offset, len, this.get1(CTRIE_OFFSET_03_MAX_CHARS_PER_NODE1));
   }

   /**
    * Create a leaf node. The method might create several nodes down the branch because of a characters' maximum per node 
    * <br>
    * <br>
    * @param parentNode the parent node of the new leaf node
    * @param c the chars of the leaf node
    * @param offset first char in the char array
    * @param len the number of chars for that node
    * @param max the maximum number of characters per node. When 0, this business is ignored.
    * @return
    */
   public int createLeaf(int parentNode, char[] c, int offset, int len, int max) {
      if (max != 0 && len > max) {
         int newParent = parentNode;
         int pointer = 0;
         int lenLeft = len;
         while (pointer < len) {
            int interLen = Math.min(max, lenLeft);
            int intermediate = createLeaf(newParent, c, offset + pointer, interLen, 0);
            pointer += interLen;
            lenLeft -= interLen;
            newParent = intermediate;
         }
         return newParent;
      } else {
         int childnode = nodedata.nodeAddCharChild(parentNode, c, offset, len);
         return childnode;
      }
   }

   /**
    * 
    */
   public int find(char[] look, int offset, int len) {
      return getPointer(look, offset, len);
   }

   public int getBiggestWordSize() {
      return get1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1);
   }

   /**
    * 
    */
   public char getChar(int pointer) {
      methodStarts();
      char c = insideGetChar(pointer);
      methodEnds();
      return c;
   }

   public IPowerCharCollector getCharCo() {
      return charco;
   }

   /**
    * Read the characters
    */
   public char[] getChars(int pointer) {
      methodStarts();
      char[] cc = insideGetChars(pointer);
      methodEnds();
      return cc;
   }

   public char[] getChars(int[] pointers) {
      return CharColUtilz.get(this, pointers);
   }

   /**
    * Return the store characters at that node
    * <br>
    * <br>
    * @param c
    * @return
    */
   private char[] getCharsAtNode(int node) {
      return charco.getChars(nodedata.getNodeCharPointer(node));
   }

   protected IPowerEnum getEnumOnPointers() {
      return null;
   }

   public String getKeyStringFromPointer(int pointer) {
      return new String(getChars(pointer));
   }

   /**
    * 
    */
   public int getLen(int pointer) {
      char[] cs = getChars(pointer);
      return cs.length;
   }

   /**
    * When Type is 1, returns a {@link IntToStrings} with all Strings within the Trie.
    * <br>
    * <br>
    * Othrwise returns this.
    */
   public Object getMorph(MorphParams p) {
      if (p.cl == IntToStrings.class) {
         //retursn all the strings as
         return searchPrefix(0).searchWait("");
      }
      return this;
   }

   public IPowerTrieNodesChar getNodeData() {
      return nodedata;
   }

   public int getPointer(char[] chars) {
      return getPointer(chars, 0, chars.length);
   }

   public int getPointer(char[] chars, int offset, int len) {
      methodStarts();
      int node = insideGetPointer(chars, offset, len);
      methodEnds();
      //if we got here,
      return node;
   }

   public int getPointer(String str) {
      return getPointer(str.toCharArray(), 0, str.length());
   }

   public IntToStrings getPrefixed(String prefix) {
      return pdc.getTrieU().getPrefixedStrings(this, prefix);
   }

   /**
    * Looks up the {@link TrieSearchSession} prefix and adds the nodes to
    * <br>
    * <br>
    * Empties the prefix node buffer.
    * When the prefix is not found in the Trie, the node buffer is cleared and flag not found is set
    * to the {@link TrieSearchSession}.
    * <br>
    * <br>
    * Adds the nodes up to prefix
    * @param tss
    */
   protected int getPrefixedNodes(IntBuffer prefixNodes, String str, ICharComparator tss) {
      methodStarts();
      try {
         return insideGetPrefixedNodes(prefixNodes, str, tss);
      } finally {
         methodEnds();
      }
   }

   /**
    * The pointer of the first Word matching the prefix.
    * <br>
    * With words alliance and all. 
    * <li> Prefix al returns the pointer for all.
    * <li> Prefix al returns the pointer for all.
    * With words alliance and all and altitude.
    * <li> Prefix al returns the pointer for al node.
    * <br>
    * The pointer doesn't point to a user word. but is a valid node.
    * <br>
    * @param prefix
    * @return
    */
   public int getPrefixPointer(String str, ICharComparator tss) {
      char[] look = str.toCharArray();
      int offset = 0;
      int len = str.length();
      toCharTrieFormat(look, offset, len);
      //start at the root
      int node = nodedata.getRootNode();
      int lastCharOffsetExcluded = offset + len;
      int lengthLeft = len;
      int nodeResult = IPowerTrieNodes.NOT_A_NODE;
      //loop until all characters are processed.
      while (offset < lastCharOffsetExcluded) {
         char lookupchar = look[offset];
         int charsLoopProcessed = 0;
         //look ups node but what about soft equality here? the CharComparator does it.
         node = nodedata.nodeFindCharChild(lookupchar, node, tss);
         //check if the char exists as a child.
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            charsLoopProcessed = lengthLeft;
            return IPowerTrieNodes.NOT_A_NODE;
         } else {
            char[] nodechars = nodedata.getNodeChars(node);
            charsLoopProcessed = 1;
            //first character has already been checked above so we start with i=1
            for (int i = 1; i < nodechars.length; i++) {
               int currentOffsetPos = offset + charsLoopProcessed; //
               if (currentOffsetPos < lastCharOffsetExcluded) {
                  //for prefix searching, we want soft equality i.e a 'é' is equal to 'e'
                  if (!tss.isEqual(look[currentOffsetPos], nodechars[i])) {
                     //case where prefix 'bonnep' is not matching a node.
                     return IPowerTrieNodes.NOT_A_NODE;
                  } else {
                     //prefect match with a node
                     if (i == nodechars.length - 1) {
                        nodeResult = node;
                     }
                     charsLoopProcessed++;
                  }
               } else {
                  //prefix string is a sub lemme of string in the Trie e.g : 'bon' prefixing 'bonjour'
                  nodeResult = node;
                  charsLoopProcessed = lengthLeft;
                  break;
               }
            }
            nodeResult = node;
         }
         //get to next char
         offset += charsLoopProcessed;
         lengthLeft -= charsLoopProcessed;
      }
      return nodeResult;
   }

   /**
    * The Trie must count the words
    */
   public int getSize() {
      return countWords(nodedata.getRootNode());
   }

   /**
    * Checks if the character is deemed equal
    * <br>
    * <br>
    * For some languages such as french, it might be convenient to return
    * é è ê and e letters for the letter e
    * <br>
    * <br>
    * @param look
    * @param pointer
    * @param nodechars
    * @param i
    * @return
    */
   private boolean getterHardEquals(char[] look, int pointer, char[] nodechars, int i) {
      return nodechars[i] != look[pointer];
   }

   /**
    * Returns true if the given chars returns a match node for 
    * <br>
    * <br>
    * It will return true even when char is not a word, but is a sub lemme.
    * <br>
    * <br>
    * It needs to be a valid prefix.
    */
   public boolean hasChars(char[] look, int offset, int len) {
      methodStarts();
      boolean v = insideHasChars(look, offset, len);
      methodEnds();
      return v;
   }

   public boolean hasChars(String str) {
      return hasChars(str.toCharArray(), 0, str.length());
   }

   /**
    * 
    */
   protected void initEmptyConstructor() {
      //rebuild index
      int charcoRef = this.get2(CTRIE_OFFSET_06_REF_CHARCOL2);
      charco = (IPowerCharCollector) byteCon.getAgentFromRefOrCreate(charcoRef, IPowerCharCollector.INT_ID);
      charco.addPointerUser(this);

      int nodeDataRef = this.get2(CTRIE_OFFSET_07_REF_TRIEDATA2);
      nodedata = (IPowerTrieNodesChar) byteCon.getAgentFromRefOrCreate(nodeDataRef, IPowerTrieNodesChar.INT_ID);
      nodedata.setCharCo(charco);
   }

   private int insideAddChars(char[] look, int offset, int len, boolean isWord) {
      toCharTrieFormat(look, offset, len);
      int pointer = offset; //points
      //start at the root
      int node = nodedata.getRootNode();
      int lastOffset = offset + len;
      int lengthLeft = len;
      //start at the root
      int nodeResult = IPowerTrieNodes.CHILD_NODE_NOT_FOUND;
      //loop until all characters are processed.
      while (pointer < lastOffset) {
         char lookupchar = look[pointer];
         int parentNode = node;
         int charsLoopProcessed = 0;
         //look ups if the node is a grappe suffix start
         node = nodedata.nodeFindCharChild(lookupchar, node);
         //check if the char exists as a child.
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            //insert a leaf to its parent
            //nodeResult = createLeaf(parentNode, look, pointer, searchlen - (pointer - 1));
            int max = get1(ITechCharTrie.CTRIE_OFFSET_03_MAX_CHARS_PER_NODE1);
            // set pointer to end the loop
            int interLen = Math.min(max, lengthLeft);
            node = nodedata.nodeAddCharChild(parentNode, look, pointer, interLen);
            charsLoopProcessed = interLen;
            nodeResult = node;
         } else {
            //result is this node when string finishes right at this node
            nodeResult = node;
            char[] nodechars = nodedata.getNodeChars(node);
            charsLoopProcessed = 1;
            //first character has already been checked above so we start with i=1
            for (int i = 1; i < nodechars.length; i++) {
               int currentPos = pointer + charsLoopProcessed;
               if (currentPos < lastOffset) {
                  //for adding new words, we want hard equality i.e a 'é' is never equal to 'e'
                  if (nodechars[i] != look[currentPos]) {
                     //case where 'bonne' is added just after 'bonjour'. Must break  'bonjour' into 'bon' and 'jour' 
                     //and create new leaf 'ne'
                     int firstNode = splitNode(node, i, nodechars);
                     node = firstNode;
                     //we don't use the burgeon node 'jour' because we now create a new leaf on the common node 'bon'
                     //for the node creation we send it back on the loop
                     //nodeResult = createLeaf(node, look, pointer, searchlen - pointer);
                     break;
                  } else {
                     if (i == nodechars.length - 1) {
                        nodeResult = node;
                     }
                     charsLoopProcessed++;
                  }
               } else {
                  //when code branches here it means the following:
                  //input string is a sub lemme of string already inside the Trie e.g : 'bon' added just after 'bonjour'
                  //You need to split 'bonjour' into 'bon' and 'jour' 
                  int firstNode = splitNode(node, i, nodechars);
                  node = firstNode;
                  nodeResult = node;
                  charsLoopProcessed = lengthLeft;
                  break;
               }
            }
         }
         //get to next char
         pointer += charsLoopProcessed;
         lengthLeft -= charsLoopProcessed;
      }
      if (isWord && nodeResult != IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
         setUserNode(nodeResult, true);
         if (len > get1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1)) {
            set1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1, len);
         }
      }
      return nodeResult;
   }

   private char insideGetChar(int pointer) {
      checkParentPointers();
      //safety valve in case parent data is corrupted which results in an infinite loop
      int maxH = SAFETY_VALVE;
      int firstChildRootNode = -1;
      int root = nodedata.getRootNode();
      int count = 0;
      while (pointer != root && count != maxH) {
         count++;
         firstChildRootNode = pointer;
         pointer = nodedata.getNodeParent(pointer);
      }
      return nodedata.getNodeChar(firstChildRootNode);
   }

   private char[] insideGetChars(int pointer) {
      checkParentPointers();
      IntBuffer bu = new IntBuffer(getUCtx(), 5);
      int parentNode = pointer;
      //safety valve in case parent data is corrupted wich results in an infinite loop
      int count = 0;
      int maxH = SAFETY_VALVE; //defensive programming in case data is corrupted
      int rootNode = nodedata.getRootNode();
      while (parentNode != rootNode && count != maxH) {
         count++;
         bu.addInt(parentNode);
         parentNode = nodedata.getNodeParent(parentNode);
      }
      StringBBuilder sb = new StringBBuilder(pdc.getUCtx());
      int size = bu.getSize();
      for (int i = 0; i < size; i++) {
         int node = bu.removeLast();
         nodedata.appendNodeChars(node, sb);
      }
      if (hasFlag(CTRIE_OFFSET_02_FLAX, CTRIE_FLAGX_1_RETURN_FIRST_CAP)) {
         if (sb.getCount() > 0) {
            char c = sb.charAt(0);
            sb.setCharAt(0, StringUtils.toUpperCase(c));
         }
      }
      return sb.toString().toCharArray();
   }

   private int insideGetPointer(char[] chars, int offset, int len) {
      toCharTrieFormat(chars, offset, len);
      int pointer = offset;
      int node = nodedata.getRootNode();
      int searchlen = offset + len;
      while (pointer < searchlen) {
         char lookupchar = chars[pointer];
         node = nodedata.nodeFindCharChild(lookupchar, node);
         pointer++;
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            return IPowerCharCollector.CHARS_NOT_FOUND;
         } else {
            //first char has already been check above
            char[] nodechars = nodedata.getNodeChars(node);
            for (int i = 1; i < nodechars.length; i++) {
               if (pointer < searchlen) {
                  if (getterHardEquals(chars, pointer, nodechars, i)) {
                     return IPowerCharCollector.CHARS_NOT_FOUND;
                  }
                  pointer++;
               } else {
                  //case where look is a sub lemme of another one
                  // we return null as it is not the precise string
                  return IPowerCharCollector.CHARS_NOT_FOUND;
               }
            }
         }
      }
      return node;
   }

   /**
    * From a given prefix String, 
    * @param tss
    * @return the node for the prefix
    */
   private int insideGetPrefixedNodes(IntBuffer prefixNodes, String str, ICharComparator tss) {
      char[] look = str.toCharArray();
      int offset = 0;
      int len = str.length();
      toCharTrieFormat(look, offset, len);
      int pointer = offset; //points
      //start at the root
      int node = nodedata.getRootNode();
      int lastOffsetExcluded = offset + len;
      int lengthLeft = len;
      //start at the root
      int searchRootNode = IPowerTrieNodes.NOT_A_NODE;
      int nodeResult = IPowerTrieNodes.NOT_A_NODE;
      boolean failureFlag = false;
      //loop until all characters are processed.
      while (pointer < lastOffsetExcluded) {
         char lookupchar = look[pointer];
         int charsLoopProcessed = 0;
         //look ups node but what about soft equality here? the CharComparator does it.
         node = nodedata.nodeFindCharChild(lookupchar, node, tss);
         //check if the char exists as a child.
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            charsLoopProcessed = lengthLeft;
            failureFlag = true;
         } else {
            char[] nodechars = nodedata.getNodeChars(node);
            charsLoopProcessed = 1;
            //first character has already been checked above so we start with i=1
            for (int i = 1; i < nodechars.length; i++) {
               int currentOffsetPos = pointer + charsLoopProcessed; //
               if (currentOffsetPos < lastOffsetExcluded) {
                  //for prefix searching, we want soft equality i.e a 'é' is equal to 'e'
                  if (!tss.isEqual(look[currentOffsetPos], nodechars[i])) {
                     //case where prefix 'bonnep' is not matching a node.
                     failureFlag = true;
                     break;
                  } else {
                     //prefect match with a node
                     if (i == nodechars.length - 1) {
                        nodeResult = node;
                     }
                     charsLoopProcessed++;
                  }
               } else {
                  //prefix string is a sub lemme of string in the Trie e.g : 'bon' prefixing 'bonjour'
                  nodeResult = node;
                  charsLoopProcessed = lengthLeft;
                  break;
               }
            }
            prefixNodes.addInt(node);
            nodeResult = node;
         }
         //get to next char
         pointer += charsLoopProcessed;
         lengthLeft -= charsLoopProcessed;
      }

      if (!failureFlag) {
         searchRootNode = nodeResult;
      }
      return searchRootNode;
   }

   private boolean insideHasChars(char[] look, int offset, int len) {
      toCharTrieFormat(look, offset, len);
      int pointer = offset;
      int searchlen = offset + len;
      //start at the root
      int node = nodedata.getRootNode();
      while (pointer < searchlen) {
         char lookupchar = look[pointer];
         node = nodedata.nodeFindCharChild(lookupchar, node);
         pointer++;
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            return false;
         } else {
            char[] nodechars = nodedata.getNodeChars(node);
            //first char has already been check above. indeed a node has been found with l 
            for (int i = 1; i < nodechars.length; i++) {
               if (pointer < searchlen) {
                  if (getterHardEquals(look, pointer, nodechars, i)) {
                     return false;
                  }
                  pointer++;
               } else {
                  return true;
               }
            }
         }
      }
      return true;
   }

   /**
    * Tests the {@link ITechCharTrie#CTRIE_OFFSET_01_FLAG} flag
    * @param flag
    * @return
    */
   private boolean isCharTrieFlagSet(int flag) {
      return this.hasFlag(ITechCharTrie.CTRIE_OFFSET_01_FLAG, flag);
   }

   protected boolean isUserNode(int node) {
      return nodedata.hasNodeFlag(node, FLAG_1_WORD);
   }

   /**
    * 
    */
   public boolean isValid(int pointer) {
      if (pointer >= nodedata.getFirstNode() && pointer <= nodedata.getLastNode())
         return true;
      return false;
   }

   public boolean isWord(int pointer) {
      return isUserNode(pointer);
   }

   protected int mergeWithParent(int parentNode, char[] c, int offset, int len) {
      char[] cc = nodedata.getNodeChars(parentNode);
      char[] ne = new char[cc.length + len];
      for (int i = 0; i < cc.length; i++) {
         ne[i] = cc[i];
      }
      int count = cc.length;
      for (int i = offset; i < offset + len; i++) {
         ne[count] = c[i];
         count++;
      }
      //        throw new RuntimeException();
      setCharPointer(ne, 0, ne.length, parentNode);
      return parentNode;
   }

   public void pointerSwap(Object struct, int newPointer, int oldPointer) {
      //used when our charcollector re-arrange all the pointers
   }

   /**
    * Remove the word associated?
    * <br>
    * What happens to the node below?
    */
   public int remove(int pointer, boolean useForce) {
      nodedata.setNodeFlag(pointer, FLAG_1_WORD, false);
      return pointer;
   }

   /**
    * 
    */
   protected void search(TrieSearchSession tss) {

      //check if prefix nodes
      if (tss.isFirstFrame()) {
         tss.sb = new StringBBuilder(pdc.getUCtx());
         //init the first nodes to be explored.
         if (tss.isAll()) {
            //special case because it involves the special root node.
            boolean isReverse = tss.isReverse();
            tss.searchRootNode = nodedata.getRootNode();
            //this mean all words ""
            nodedata.getNodeChildren(nodedata.getRootNode(), tss.nodesBeingExplored, isReverse);
         } else {
            IntBuffer pre = new IntBuffer(pdc.getUCtx(), 5);
            //we need to fetch the prefix nodes. 
            tss.searchRootNode = getPrefixedNodes(pre, tss.str, tss);
            //add the node to kick start the buffer
            if (tss.searchRootNode != IPowerTrieNodes.NOT_A_NODE) {
               tss.nodesBeingExplored.addInt(tss.searchRootNode);
               pre.removeLast();
               //append the prefix nodes
               buildString(pre.getIntsRef(), 1, pre.getSize(), tss.sb);
            }
         }
      }
      //if the root node is a valid node
      if (tss.searchRootNode != IPowerTrieNodes.NOT_A_NODE) {
         //initialize the family holder
         tss.algoX(nodedata);
         //searchAddWordAndCheckBelowFlat(tss);
         //searchBuildStringsFromNodes(tss);
      }
   }

   /**
    * Reads the nodes having to be explored. For each words in those nodes,
    * create a new entry in the result buffer.
    * <br>
    * Reads familyHolder as currentRow
    * @param ss
    * @param node
    */
   private void searchAddWordAndCheckBelowFlat(TrieSearchSession ss) {
      //row to be cloned
      IntBuffer currentRow = ss.familyHolder;
      IntBuffer nodesBeingExploredBuffer = ss.nodesBeingExplored;
      int count = 0;
      int currentNode = 0;
      boolean isFinished = false;
      //for alphabetical order add the children node in reverse order so first node is processed first in the loop
      boolean isReverse = !ss.isReverse();
      StringBBuilder sb = new StringBBuilder(pdc.getUCtx());
      //append the prefix
      sb.append(ss.chars, ss.charsOffset, ss.charsLen);
      //algo runs untils all nodes are processed
      while (nodesBeingExploredBuffer.getSize() != 0 && !isFinished) {
         //pop next node to inspect
         currentNode = nodesBeingExploredBuffer.getLast();
         //when a separator marker is found. it means we are at the end of a word.
         if (currentNode == TrieSearchSession.SEPARATOR_MARKER) {
            //
            currentRow.removeLast();
            //removes the last child of this family
            nodesBeingExploredBuffer.removeLast();
            //and remove the parent. all child have been inspected and parent also has been inspected
            nodesBeingExploredBuffer.removeLast();
            continue;
         }
         currentRow.addInt(currentNode);

         //adds marker for new level
         nodesBeingExploredBuffer.addInt(TrieSearchSession.SEPARATOR_MARKER);
         //for alphabetical order add the children node in reverse order so first node is processed first in the loop
         nodedata.getNodeChildren(currentNode, nodesBeingExploredBuffer, isReverse);

         if (isUserNode(currentNode)) {
            //build the string at current row.
            ss.result.add(currentNode, sb.toString());
            ss.dataBuffer.addCopyRow(currentRow);
            count++;
            if (count == ss.getFrameSize()) {
               isFinished = true;
            }
         }
      }
   }

   /**
    * Build Strings from the {@link IntBufferMatrix} and prefix {@link IntBuffer}.
    *  Check if node match {@link TrieSearchSession} constraints. (data wise, hidden or not).
    * <br>
    * <br>
    * This search only search inside this trie. For combined search with several Trie. the controlling Trie does the
    * management work.
    * <br>
    * <br>
    * @param tss contains the root node
    * @param prefix
    * @param max
    * @return
    */
   private void searchBuildStringsFromNodes(TrieSearchSession tss) {
      //prefix node is empty when prefix is ""
      //properties of the search session. not the trie.
      boolean majFirst = tss.tech.hasFlag(ITechSearchTrie.SEARCH_CHAR_OFFSET_01_FLAG1, ITechSearchTrie.SEARCH_CHAR_FLAG_1_RETURN_FIRST_CAP);

      //contains full list of nodes.
      IntBufferMatrix listOfNodes = tss.dataBuffer;

      StringBBuilder sb = new StringBBuilder(pdc.getUCtx());

      int prefixSize = tss.prefixNodes.getSize();

      if (prefixSize != 0) {
         //assume all prefix nodes will start
         buildString(tss.prefixNodes.getIntsRef(), 1, prefixSize, sb);
         if (majFirst) {
            char c = sb.charAt(0);
            char uc = StringUtils.toUpperCase(c);
            sb.setCharAt(0, uc);
         }
      }

      int prefixCount = sb.getCount(); //count at chi

      //when using frame
      int start = tss.getStart();

      int end = tss.getEnd(); //end exclusive offset in buffer matrix

      //iterate over the framed IntBufferMatrix
      for (int i = start; i < end; i++) {

         IntBuffer mybuf = listOfNodes.getRow(i);
         if (mybuf != null) {
            int rowOffset = 1 + prefixSize;
            int rowLen = mybuf.getSize() - prefixSize;
            buildString(mybuf.getIntsRef(), rowOffset, rowLen, sb);
            if (prefixSize == 0 && majFirst) {
               char c = sb.charAt(0);
               char uc = StringUtils.toUpperCase(c);
               sb.setCharAt(0, uc);
            }
            //the pointer for later getWord or identification
            int lastnode = mybuf.getLast();
            String finalString = sb.toString();

            //another structure may decide to reject a string based on some tests
            if (tss.acceptString(finalString)) {
               tss.result.add(lastnode, finalString);
               //reset stringbuilder to the prefix
            } else {
               //increment end
               end++;
            }
            sb.setCount(prefixCount);
         } else {
            //break as soon as we find a null
            break;
         }
      }
   }

   /**
    * Only serialize class data. other ByteObjectManaged are not serialized here.
    * <br>
    * Must be called within a lock
    * @return
    */
   private byte[] serializeRaw() {
      methodStarts();
      //returns the ByteObjectManaged empty of data with headers and trailers
      byte[] header = toByteArray();
      //encapsulates the header for easier manipulation.
      ByteObjectManaged tr = new ByteObjectManaged(getBOC(), header);
      tr.set1(AGENT_OFFSET_03_FLAGZ_1, 0);
      byte[] data = tr.getByteObjectData();

      methodEnds();

      return data;
   }

   private void serializeRawReverse() {
      dataLock();

      methodEnds();
   }

   /**
    * Construct a {@link PowerCharTrie} from a source which was created with the {@link IDataMorphable#serializePack()} methods.
    * <br>
    * <br>
    * When {@link ByteController} is null, there is no context and all the data is provided byte[]. 
    * <br>
    * <br>
    * The method will look at params object. The order is set by the class. This order must be respected by
    * the caller. For instance, a shared object will be provided in that array.
    * <br>
    * <br>
    * When the {@link ByteController} is not null, and data is null, ask the {@link ByteController} to find
    * an area that match the current header of the object. That header provides the different ids for the {@link ByteController}
    * to identifies the right byte area for this {@link ByteObjectManaged}.
    * <br>
    * <br>
    * <br>
    * The {@link ByteController} will control the {@link ByteObjectManaged}.
    * <br>
    * <br>
    * @param bc 
    */
   public void serializeReverse() {
      if (hasData()) {
         serializeRawReverse();
         initEmptyConstructor();
      } else {
         initEmptyConstructor();
      }
   }

   /**
    * How do you link ?
    * <br>
    * <br>
    * By what mean the {@link ByteController} will know which composite class to use?
    * <br>
    * <br>
    * 
    */
   public ByteObjectManaged serializeTo(ByteController bc) {
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(serializeRaw());

      charco.serializeTo(bc);
      bom.set2(CTRIE_OFFSET_06_REF_CHARCOL2, charco.getTech().get2(AGENT_OFFSET_08_REF_ID2));
      //nodedata
      nodedata.serializeTo(bc);
      bom.set2(CTRIE_OFFSET_07_REF_TRIEDATA2 + 2, nodedata.getTech().get2(AGENT_OFFSET_08_REF_ID2));
      return bom;
   }

   public void setCharCo(IPowerCharCollector cc) {
      charco = cc;
      nodedata.setCharCo(cc);
   }

   /**
    * Adding a value may change the order of the {@link IPowerCharCollector}.
    * <br>
    * <br>
    * Everytime a shuffling is done, the method {@link IPowerCharCollector#getNewPointers()}
    * allows for the re-arrangements.
    * <br>
    * <br>
    * 
    * @param c
    * @param offset
    * @param len
    * @param node
    */
   private void setCharPointer(char[] c, int offset, int len, int node) {
      int pointer = IPowerCharCollector.CHARS_NOT_FOUND;
      if (isCharTrieFlagSet(ITechCharTrie.CTRIE_FLAG_1_REUSE_CHARS)) {
         pointer = charco.getPointer(c, offset, len);
      }
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         //if this method mofifies the charco pointers, we have the call-back to the new pointer to re-arrange.
         pointer = charco.addChars(c, offset, len);
      }
      //update the nodes data
      nodedata.setCharPointer(node, pointer);
   }

   /**
    * Several pointers can point to the same char.
    * Use a pointer mapping when not stable.
    */
   public int setChars(int pointer, char[] chars, int offset, int len) {
      if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_1_STABLE)) {
         int currentPointer = getPointer(chars, offset, len);
         if (currentPointer == CHARS_NOT_FOUND) {
            currentPointer = addChars(chars, offset, len);
         }
         //TODO
      } else {

      }
      return -1;
   }

   protected void setUserNode(int node, boolean v) {
      nodedata.setNodeFlag(node, FLAG_1_WORD, v);
   }

   /**
    * Splits the characters stored at the node at the pindex.
    * <br>
    * <br>
    * Transfer the existing node associations to the
    * 
    * This method is never called when all characters are inside their own node. {@link ITechCharTrie#CTRIE_FLAG_8_CHAR_EQUAL_NODE}
    * <br>
    * <br>
    * The root node does not have word flag.
    * <br>
    * <br>
    * 
    * @param node
    * @param pindex
    * @return the new burgoen node, children of parentNode. the node end 
    */
   private int splitNode(int node, int pindex, char[] nodechars) {
      if (true) {
         return splitNode3(node, pindex, nodechars);
      }
      if (pindex >= nodechars.length)
         return node;
      //split procedure
      //take the current node and change it string value to the common letters
      char[] commonChars = new char[pindex];
      char[] splits = new char[nodechars.length - pindex];
      for (int i = 0; i < commonChars.length; i++) {
         commonChars[i] = nodechars[i];
      }
      for (int i = 0; i < splits.length; i++) {
         splits[i] = nodechars[i + pindex];
      }
      //update the old chars with the common trunk only
      setCharPointer(commonChars, 0, commonChars.length, node);

      //transfer the flags
      boolean isUserNode = isUserNode(node);

      setUserNode(node, false);

      int burgeonnode = nodedata.splitNode(node);

      setCharPointer(splits, 0, splits.length, burgeonnode);

      //transfer user node flag
      setUserNode(burgeonnode, isUserNode);

      //nodedata.addData(node, 0);
      return burgeonnode;
   }

   private int splitNode3(int node, int pindex, char[] nodechars) {
      if (pindex >= nodechars.length)
         return node;
      //split procedure
      //take the current node and change it string value to the common letters
      char[] commonChars = new char[pindex];
      char[] splits = new char[nodechars.length - pindex];
      for (int i = 0; i < commonChars.length; i++) {
         commonChars[i] = nodechars[i];
      }
      for (int i = 0; i < splits.length; i++) {
         splits[i] = nodechars[i + pindex];
      }

      int firstPL = addCharCoChars(commonChars, 0, commonChars.length);
      int secondPL = addCharCoChars(splits, 0, splits.length);

      int secondNode = nodedata.payLoadSplit(node, firstPL, secondPL);
      return secondNode;
   }

   /**
    * use the string transformer.
    * Lowercase usually
    */
   private void toCharTrieFormat(char[] look, int offset, int len) {
      if (isCharTrieFlagSet(ITechCharTrie.CTRIE_FLAG_4_LOWERCASE_TRANSFORM)) {
         StringUtils.toLowerCaseMordan(look, offset, len);
      }
   }

   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerCharTrie");
      toStringPrivate(dc);
      super.toString(dc.sup());
      pdc.getTechFactory().toStringTechCharTrie(dc.nLevel(), this);
      charco.toString(dc.nLevel());
      nodedata.toString(dc.nLevel());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerCharTrie");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

   
   public void updatePointers(Object struct, Object mapping) {
      if (struct == charco) {
         if (mapping instanceof int[]) {
            int[] links = (int[]) mapping;
            for (int i = 0; i < links.length; i++) {
               int oldpointer = nodedata.getNodeCharPointer(i);
               int newpointer = links[oldpointer];
               nodedata.setCharPointer(i, newpointer);
            }
         }
      }
   }

}
