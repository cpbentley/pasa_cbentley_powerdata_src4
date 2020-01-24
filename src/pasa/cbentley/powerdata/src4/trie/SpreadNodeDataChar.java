package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSpreadNodeDataChar;
import pasa.cbentley.powerdata.spec.src4.power.trie.ICharComparator;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodesChar;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public class SpreadNodeDataChar extends SpreadNodeData implements ITechSpreadNodeDataChar, IPowerTrieNodesChar {

   public static final int AgentID             = 5556;

   public static final int CHARTRIE_HEADERSIZE = 1;

   protected int           bitCharSize;

   protected int           bitWordSize;

   public SpreadNodeDataChar(PDCtx pdc, byte[] data, int index) {
      super(pdc, data, index);
      fsp = new FamilySearchPacked(pdc, this);
   }

   public void appendNodeChars(int node, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   /**
    * 
    */
   public void expandChar(int newSize) {
      int diff = newSize - bitCharSize;
      int oldsize = bitCharSize;
      //_bitCharSize = newSize;
      throw new RuntimeException("not implemented " + newSize + " old = " + oldsize);
   }

   public IPowerCharCollector getCharCo() {
      // TODO Auto-generated method stub
      return null;
   }

   public int getFamilyPosition(char mychar, int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getHeaderSizeAddition() {
      return CHARTRIE_HEADERSIZE;
   }

   public int getMemAgentID() {
      return AgentID;
   }

   public char getNodeChar(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeCharPointer(int node) {
      BitCoordinate c = new BitCoordinate(pdc.getUCtx());
      c.map((subClassDataOffset * 8) + 8 + ((node - 1) * (bitCharSize + 1)));
      return BitUtils.readBits(data, c, bitCharSize);
   }

   public char[] getNodeChars(int node) {
      // TODO Auto-generated method stub
      return null;
   }

   private void initConstructor() {
      //+1 because 8 bits are read above
      bitCharSize = get1(SPREAD_CHAR_OFFSET_02_CHARPOINTER_BIT_SIZE1);
   }

   public boolean isUserNode(int node) {
      //minus 1 because Spread nodes start at 1
      BitCoordinate c = new BitCoordinate(pdc.getUCtx());
      c.map((subClassDataOffset * 8) + 8 + ((node - 1) * (bitCharSize + 1)) + bitCharSize);
      return BitUtils.readBit(data, c) == 1;
   }

   public int nodeAddCharChild(int node, char c) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int nodeAddCharChild(int node, char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int nodeFindCharChild(char mychar, int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int nodeFindCharChild(char mychar, int node, ICharComparator icc) {
      // TODO Auto-generated method stub
      return 0;
   }

   public void setCharCo(IPowerCharCollector cc) {
      // TODO Auto-generated method stub

   }

   /**
    * 
    */
   public void setCharPointer(int node, int pointer) {
      if (BitUtils.widthInBits(pointer) > bitCharSize) {
         expandChar(BitUtils.widthInBits(pointer));
         throw new RuntimeException("");
      }
      BitCoordinate c = new BitCoordinate(pdc.getUCtx());
      c.map((subClassDataOffset * 8) + 8 + ((node - 1) * (bitCharSize + 1)));
      BitUtils.copyBits(data, c, pointer, bitCharSize);
   }

   public void setCharsAtNode(int node, char[] c, int offset, int len) {
      // TODO Auto-generated method stub

   }

   public void setSuffixTrie(IPowerCharTrie suffixTrie) {
      // TODO Auto-generated method stub

   }

   public void setWordBit(int node, int bit) {
      BitCoordinate c = new BitCoordinate(pdc.getUCtx());
      c.map((subClassDataOffset * 8) + 8 + ((node - 1) * (bitCharSize + 1)) + bitCharSize);
      BitUtils.copyBit(data, c, bit);
   }

   private BitCoordinate createBitCoordinate() {
      BitCoordinate c = new BitCoordinate(pdc.getUCtx());
      return c;
   }

   public void updateCharPointer(int pointer, int i) {
      //only for run
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "SpreadNodeDataChar");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   private void toStringPrivate(Dctx sb) {
      sb.append("bitcharSize=" + bitCharSize);
      sb.append('\n');
      int bitb = (subClassDataOffset + 1) * 8;
      BitCoordinate c = createBitCoordinate();
      c.map(bitb);
      int count = 0;
      for (int i = 0; i < getNumberOfNodes(); i++) {
         int val = BitUtils.readBits(data, c, bitCharSize);
         int bit = BitUtils.readBit(data, c);
         count++;
         sb.append(count + "\t charpointer=" + val + " word=" + bit);
         sb.append('\n');

      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "SpreadNodeDataChar");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
