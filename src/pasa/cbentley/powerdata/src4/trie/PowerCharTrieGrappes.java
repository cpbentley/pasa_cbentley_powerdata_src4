package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
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
public class PowerCharTrieGrappes extends PowerCharTrieRoot implements IPowerCharCollector {
   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getCharEnum(Object param) {
      return null;
   }

   /**
    * could be Build or Run
    */
   public IPowerCharCollector charco;

   public int                 FLAG_1_WORD         = 1;

   public int                 FLAG_2_SUFFIX       = 2;

   private int                flags;
   protected IPowerEnum getEnumOnPointers() {
      return null;
   }
   /**
    * 
    */
   public IPowerTrieNodesChar nodedata;

   private IPowerCharTrie     suffixTrie;

   /**
    * 0 means no limit.
    * <br>
    * <br>
    * 1 means 1 character per node maximum. i.e. all nodes only have 1 char stored by the mean of a char pointer.
    */
   public int                 techMaxCharsPerNode = 0;

   public PowerCharTrieGrappes(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public int addChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int addChars(String s) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Adds a word to the {@link IPowerCharTrie}. No data link.
    * @param word
    * @param offset
    * @param len
    */
   public int addWord(char[] word, int offset, int len) {
      return addWord(word, offset, len, true);
   }

   /**
    * Main Add Method
    * <br>
    * <br>
    * @param look
    * @param offset
    * @param len
    * @param isWord true if it is a user word
    * @return the node id of the string
    */
   public int addWord(char[] look, int offset, int len, boolean isWord) {
      toCharTrieFormat(look, offset, len);
      int pointer = offset;
      //start at the root
      int node = nodedata.getRootNode();
      int nodeResult = CHARS_NOT_FOUND;
      int searchlen = offset + len;
      while (pointer < searchlen) {
         char lookupchar = look[pointer];
         int parentNode = node;
         //look ups if the node is a grappe suffix start
         node = nodedata.nodeFindCharChild(lookupchar, node);
         pointer++;
         //check if the char exists as a child.
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            //insert a leaf to its parent
            nodeResult = createLeaf(parentNode, look, pointer - 1, searchlen - (pointer - 1));
            // set pointer to end the loop
            break;
         } else {
            char[] nodechars = nodedata.getNodeChars(node);
            //first character has already been checked above so we start with i=1
            for (int i = 1; i < nodechars.length; i++) {
               if (pointer < searchlen) {
                  if (getterEquals(look, pointer, nodechars, i)) {
                     //take parent node, break it in two pieces
                     splitNode(node, i, nodechars);
                     // now we can create leaf
                     nodeResult = createLeaf(node, look, pointer, searchlen - pointer);
                     pointer = searchlen;
                     break;
                  }
                  pointer++;
               } else {
                  //when code branches here it means the following:
                  //input string is a sub lemme of string already inside the Trie.
                  //we have to break current node.
                  splitNode(node, i, nodechars);
                  nodeResult = node;
                  // now we can create leaf
                  pointer = searchlen;
                  break;
               }
            }
         }
      }
      if (isWord && nodeResult != -1) {
      }
      return nodeResult;
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   public int copyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int createLeaf(int parentNode, char[] c, int offset, int len) {
      return createLeaf(parentNode, c, offset, len, techMaxCharsPerNode);
   }

   /**
    * Create a leaf node. The method might create several nodes down the branch because of a characters' maximum per node 
    * <br>
    * <br>
    * @param parentNode the parent node of the new leaf node
    * @param c the chars of the leaf node
    * @param offset first char in the char array
    * @param len the number of chars
    * @return
    */
   public int createLeaf(int parentNode, char[] c, int offset, int len, int max) {
      if (max != 0) {
         int newParent = parentNode;
         int pointer = 0;
         while (pointer < len) {
            int intermediate = createLeaf(newParent, c, offset + pointer, max, 0);
            pointer += max;
            newParent = intermediate;
         }
         return newParent;
      }
      int indexPosition = 0;
      //learn where is located the first child of parentNode
      int childPointer = nodedata.getNodeChildFirst(parentNode);
      if (childPointer == 0) {
         //just inserts a node.
         // parent does not have any children, i.e no leaves
         // the first child will be located by a pointer at
         if (parentNode != 0 && !nodedata.hasNodeFlag(parentNode, FLAG_1_WORD)) {
            //the relevance of this code is questionable. is it used at all?
            //this code merges child with parent (there is no distinction in meaning between the two
            return mergeWithParent(parentNode, c, offset, len);
         }
      } else {
         indexPosition = nodedata.getFamilyPosition(c[offset], parentNode);
      }
      //return 
      int childnode = nodedata.addChild(parentNode, indexPosition);
      // add string data to the structure and node is used to inserting charData
      //optimize here by re-using strings in the char compressor
      //iterate over pointers to see if there is such a string of character.
      nodedata.setCharsAtNode(childnode, c, offset, len);
      // we don't have any addresses
      return childnode;
   }

   public int find(char[] str, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getBiggestWordSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public char getChar(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   public char[] getChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public char[] getChars(int[] pointers) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int[] getNewPointers() {
      // TODO Auto-generated method stub
      return null;
   }

   public int getPointer(char[] chars) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPointer(char[] chars, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public String getKeyStringFromPointer(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public ByteObjectManaged getTech() {
      return this;
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
   private boolean getterEquals(char[] look, int pointer, char[] nodechars, int i) {
      return nodechars[i] != look[pointer];
   }

   /**
    * 
    * @param word
    * @param offset
    * @param len
    * @return
    */
   public int getWordID(char[] look, int offset, int len) {
      toCharTrieFormat(look, offset, len);
      int pointer = offset;
      int node = nodedata.getRootNode();
      int searchlen = offset + len;
      while (pointer < searchlen) {
         char lookupchar = look[pointer];
         node = nodedata.nodeFindCharChild(lookupchar, node);
         pointer++;
         if (node == IPowerTrieNodes.CHILD_NODE_NOT_FOUND) {
            return CHARS_NOT_FOUND;
         } else {
            //first char has already been check above
            char[] nodechars = nodedata.getNodeChars(node);
            for (int i = 1; i < nodechars.length; i++) {
               if (pointer < searchlen) {
                  if (getterEquals(look, pointer, nodechars, i)) {
                     return CHARS_NOT_FOUND;
                  }
                  pointer++;
               } else {
                  //case where look is a sub lemme of another one
                  // we return null as it is not the precise string
                  return CHARS_NOT_FOUND;
               }
            }
            //sets the suffix trie inside the NodeData.?
            if (suffixTrie != null && isSuffixNode(node)) {
               int suffixID = suffixTrie.getPointer(look, pointer, searchlen - pointer);
               return ((suffixID & 0xFF) >> 24) + node;
            }
         }
      }
      //if we got here,
      return node;
   }

   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isCharTrieFlagSet(int flag) {
      return (flags & flag) == flag;
   }

   private boolean isSuffixNode(int node) {
      return nodedata.hasNodeFlag(node, FLAG_2_SUFFIX);
   }

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
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

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void search(CharSearchSession css) {
      // TODO Auto-generated method stub

   }

   public void search(TrieSearchSession tss) {

   }

   public byte[] serializePack() {
      // TODO Auto-generated method stub
      return null;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
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
         pointer = charco.find(c, offset, len);
      }
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         //when this method mofifies the charco handles, we need to the new pointer to re-arrange.
         //         pointer = charco.addChars(c, offset, len, node);
         //         int[] links = charco.getNewPointers();
         //         if (links != null) {
         //            for (int i = 0; i < links.length; i++) {
         //               int oldpointer = nodedata.getNodeCharPointer(i);
         //               int newpointer = links[oldpointer];
         //               nodedata.setCharPointer(i, newpointer);
         //            }
         //         }
      }
      //update the nodes data
      nodedata.setCharPointer(node, pointer);
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub

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
    * 
    * @param node
    * @param pindex
    * @return the new burgoen node, children of parentNode
    */
   private int splitNode(int node, int pindex, char[] nodechars) {
      if (pindex >= nodechars.length)
         return node;
      //split procedure
      //take the current node and change it string value to the common letters
      char[] commons = new char[pindex];
      char[] splits = new char[nodechars.length - pindex];
      for (int i = 0; i < commons.length; i++) {
         commons[i] = nodechars[i];
      }
      for (int i = 0; i < splits.length; i++) {
         splits[i] = nodechars[i + pindex];
      }
      //update the old chars with the common trunk only
      setCharPointer(commons, 0, commons.length, node);

      int nodeUserNode = nodedata.hasNodeFlag(node, FLAG_1_WORD) ? 1 : 0;
      //Set the word bit to 1 so that the createleaf does not merge one families

      //
      int burgeonnode = nodedata.splitNode(node);

      setCharPointer(splits, 0, splits.length, burgeonnode);

      //set the word bit back to what it previously

      // int data = nodedata.getNodeData(burgeonnode);
      // if (data != 0) {
      //    throw new RuntimeException("Data From Burgeon Node = " + data);
      // }
      //transfer user node flag

      // why was this line written?
      //nodedata.addData(node, 0);
      return burgeonnode;
   }

   /**
    * use the string transformer
    */
   private void toCharTrieFormat(char[] look, int offset, int len) {
      if (!isCharTrieFlagSet(ITechCharTrie.CTRIE_FLAG_4_LOWERCASE_TRANSFORM)) {
         StringUtils.toLowerCaseMordan(look, offset, len);
      }
   }

   //#mdebug
   public String toString(String nl) {
      // TODO Auto-generated method stub
      return null;
   }
   //#enddebug

   public int getPointer(String str) {
      return getPointer(str.toCharArray());
   }

   
   public boolean hasChars(String str) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public int countWords(int wordid) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public int countWords(String prefix) {
      // TODO Auto-generated method stub
      return 0;
   }


   
   public IntToStrings getPrefixed(String prefix) {
      // TODO Auto-generated method stub
      return null;
   }

   
   public boolean isWord(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public int getPrefixPointer(String str, ICharComparator tss) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   void buildString(int[] nodes, int offset, int len, StringBBuilder sb) {
      // TODO Auto-generated method stub
      
   }

}
