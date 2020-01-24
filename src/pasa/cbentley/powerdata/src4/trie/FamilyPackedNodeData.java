package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSpreadNodeData;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public abstract class FamilyPackedNodeData extends ByteObjectManaged implements ITechSpreadNodeData, IPowerTrieNodes {

   /**
    * Must be initialized by sub class.
    * Does not respect OOP because we talk about characters in this class
    * New type will have to modify this.
    * This is because we don't have multiple inheritance.
    */
   protected FamilySearchPacked fsp;

   protected final PDCtx        pdc;

   public FamilyPackedNodeData(PDCtx pdc, byte[] data, int index) {
      super(pdc.getBoc(), data, index);
      this.pdc = pdc;
   }

   public FamilyPackedNodeData(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc.getBoc(), tech);
      this.pdc = pdc;
   }

   public int getNodeChildren(int node, IntBuffer dest) {
      int family = getNodeFamilySize(node);
      int childp = getNodeChildFirst(node);
      for (int i = 0; i < family; i++) {
         dest.addInt(childp + i);
      }
      return family;
   }

   public int getNodeChildren(int node, int[] dest, int offset) {
      int family = getNodeFamilySize(node);
      int childp = getNodeChildFirst(node);
      int count = offset;
      for (int i = 0; i < family; i++) {
         dest[count] = childp + i;
         //System.out.println("\t child " + (childp + i) + " of parent " + node + " inserted at " + count);
         count++;
      }
      return family;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "FamilyPackedNodeData");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "FamilyPackedNodeData");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
