package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Node data strcuture where children within a family are chained using sister right pointers
 * <br>
 * <br>
 * @author Charles Bentley
 *
 */
public abstract class FamilyChainedNodeData extends ByteObjectManaged implements IPowerTrieNodes {

   private IntBuffer child;

   /**
    * Must be initialized by sub class.
    * Does not respect OOP because we talk about characters in this class
    * New type will have to modify this.
    * This is because we don't have multiple inheritance.
    */
   protected FamilySearchChained fsp;

   protected final PDCtx         pdc;

   public FamilyChainedNodeData(PDCtx pdc, byte[] data, int index) {
      super(pdc.getBoc(), data, index);
      this.pdc = pdc;
      child = new IntBuffer(pdc.getUCtx(), 10);
   }

   public FamilyChainedNodeData(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc.getBoc(), tech);
      this.pdc = pdc;
      child = new IntBuffer(pdc.getUCtx(), 10);
   }

   /**
    * 
    */
   public int getNodeChildren(int node, int[] dest, int offset) {
      //do not assume family size is store. compute it
      int childp = getNodeChildFirst(node);
      int count = 0;
      child.clear();
      while (childp != 0) {
         count++;
         child.addInt(childp);
         dest[offset] = childp;
         offset++;
         int sisterRight = getNodeSisterRight(childp);
         childp = sisterRight;
      }
      return count;
   }

   public int getNodeChildren(int node, IntBuffer dest) {
      int childp = getNodeChildFirst(node);
      int count = 0;
      while (childp != 0) {
         count++;
         dest.addInt(childp);
         int sisterRight = getNodeSisterRight(childp);
         childp = sisterRight;
      }
      return count;
   }

   public int getNodeChildren(int node, IntBuffer dest, boolean reverse) {
      if (reverse) {
         int flagIndex = dest.getSize() + 1;
         int num = getNodeChildren(node, dest);
         IntUtils.reverse(dest.getIntsRef(), flagIndex, num);
         return num;
      } else {
         return getNodeChildren(node, dest);
      }
   }
}
