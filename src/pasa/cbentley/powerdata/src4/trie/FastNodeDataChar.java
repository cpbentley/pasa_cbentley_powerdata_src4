package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechFastNodeData;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNodeTopo;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * For building we emphasize on speed for write access
 * <br>
 * <br>
 * Interpret the payload as a character.
 * <br>
 * <br>
 * @author Charles Bentley
 *
 */
public class FastNodeDataChar extends FastNodeData implements IPowerTrieNodesChar, ITechFastNodeData {


   /**
    * How is this value initialized?
    * <br>
    * 
    */
   public IPowerCharCollector charco;

   public FastNodeDataChar(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getFastNodeDataCharTechDefault());
   }

   public FastNodeDataChar(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructor();
   }


   public void appendNodeChars(int node, StringBBuilder sb) {
      int pointer = getNodeCharPointer(node);
      charco.appendChars(pointer, sb);
   }

   public IPowerCharCollector getCharCo() {
      return charco;
   }

   /**
    * 
    */
   public int getFamilyPosition(char mychar, int node) {
      return fsp.getFamilyPosition(mychar, node);
   }

   public char getNodeChar(int node) {
      int pointer = getNodeCharPointer(node);
      return charco.getChar(pointer);
   }

   public int getNodeCharPointer(int node) {
      return offsetToPayload[node];
   }

   public char[] getNodeChars(int node) {
      int pointer = getNodeCharPointer(node);
      return charco.getChars(pointer);
   }

   /**
    * Override to check if {@link TrieNodeTopo} needs access to subclass data. here the char data.
    * If not simply calls super class method.
    */
   public void getTopology(TrieNodeTopo tnt) {

      super.getTopology(tnt);
   }

   private void initEmptyConstructor() {
      int charcoRef = this.get2(FAST_NODE_OFFSET_REF_ID2);
      charco = (IPowerCharCollector) byteCon.getAgentFromRefOrCreate(charcoRef, IPowerCharCollector.INT_ID);
      fsp = new FamilySearchChained(pdc,this);
   }

   /**
    * Add a new node for character c for parent node.
    * <br>
    * <br>
    * Use alpha numerial order
    */
   public int nodeAddCharChild(int node, char c) {
      int wouldBeIndex = getFamilyPosition(c, node);
      int newnode = addChild(node, wouldBeIndex);
      return newnode;
   }

   /**
    * Adds the node according to alpha numerical order
    */
   public int nodeAddCharChild(int node, char[] c, int offset, int len) {
      int newnode = nodeAddCharChild(node, c[offset]);
      int pointer = charco.addChars(c, offset, len);
      offsetToPayload[newnode] = pointer;
      return newnode;
   }

   public int nodeAddCharChildSet(int node, char c) {
      int newnode = nodeAddCharChild(node, c);
      int pointer = charco.addChars(c + "");
      offsetToPayload[newnode] = pointer;
      return newnode;
   }

   /**
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
   public int nodeFindCharChild(char mychar, int trienode) {
      return fsp.findChildNode(mychar, trienode);
   }

   public int nodeFindCharChild(char mychar, int node, ICharComparator icc) {
      return fsp.findChildNode(mychar, node, icc);
   }

   public void serializeReverse() {
      super.serializeReverse();
      initEmptyConstructor();
   }

   /**
    * 
    */
   public ByteObjectManaged serializeTo(ByteController bc) {
      ByteObjectManaged bom = super.serializeTo(bc);
      charco.serializeTo(bc);
      bom.set2(FAST_NODE_OFFSET_REF_ID2, charco.getTech().getIDRef());
      return bom;
   }

   /**
    * Setting an external {@link IPowerCharCollector}, means an update of {@link ByteController}
    * <br>
    * 
    */
   public void setCharCo(IPowerCharCollector cc) {
      if (charco != cc) {
         byteCon.mergeBOM(cc.getTech());
         charco = cc;
         set2(FAST_NODE_OFFSET_REF_ID2, charco.getTech().getIDRef());
      }
   }

   public void setCharPointer(int node, int pointer) {
      offsetToPayload[node] = pointer;
   }

   public void setCharsAtNode(int node, char[] c, int offset, int len) {
      int pointer = getNodeCharPointer(node);
      charco.setChars(pointer, c, offset, len);
   }

   //#mdebug

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "FastNodeDataChar");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {
      dc.append("CharCoRef=" + get2(FAST_NODE_OFFSET_REF_ID2));
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "FastNodeDataChar");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   


   public void toStringSub(Dctx sb, int offset) {
      int pointer = getNodeCharPointer(offset);
      if (charco != null) {
         try {
            sb.append(" " + charco.getKeyStringFromPointer(pointer) + " charp=" + pointer + " usernode=" + hasNodeFlag(offset, 1));
         } catch (Exception e) {
            sb.append(e.getMessage() + " for offset=" + offset + " pointer=" + pointer);
         }
      }
   }
   //#enddebug
}
