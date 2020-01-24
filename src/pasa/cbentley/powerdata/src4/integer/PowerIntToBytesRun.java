package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToBytesRun;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRun;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Implements Random Access File behaviour
 * <br>
 * <br>
 * Implements a memory optimized Map<int,[byte]>
 * <br>
 * @author Charles Bentley
 *
 */
public class PowerIntToBytesRun extends PowerIntToBytes implements ITechIntToBytesRun {

   private class Request {

      byte[]     b;

      int        blockAboveOffset     = -1;

      int        blockBelowOffset     = -1;

      /**
       * value when rid is just above a block
       */
      int        borderBlockSize      = -1;

      int        borderBlockSizeAbove = -1;

      int        flags;

      /**
       * From Rid and Base
       */
      int        id;

      boolean    isNull;

      public int num;

      int        numBelow;

      /**
       * Offset of block
       */
      int        offset;

      int        size;

      public int getNumAboveRid() {
         int numRec = id - base;
         return numBelow + num - numRec;
      }

      public int getNumBelowRid() {
         int numRec = id - base;
         return numRec - numBelow;
      }

      public int getRidOffset() {
         int numBelowRid = getNumBelowRid();
         return offset + (numBelowRid * size);
      }

      public boolean hasFlag(int flag) {
         return BitUtils.hasFlag(flags, flag);
      }
   }

   private static final int LEN_DELETE = -2;

   private static final int LEN_NULL   = -1;

   
   private int              base;

   private Request          r          = new Request();

   public PowerIntToBytesRun(PDCtx pdc) {
      super(pdc, pdc.getTechFactory().getPowerIntToBytesRunRootTech());
   }

   public PowerIntToBytesRun(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructor();
   }

   public int addBytes(byte[] b) {
      methodStartsWrite();
      int v = insideAddBytes(b, 0, b.length);
      methodEndsWrite();
      return v;
   }

   public int addBytes(byte[] b, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * RID is deleted. Never reused and throws {@link Exception}.
    * <br>
    * <br>
    * Create a special block DELETED
    * @param rid
    */
   public void deleteBytes(int rid) {
      methodStartsWrite();
      insideDeleteBytes(rid);
      methodEndsWrite();
   }

   public void getBytes(int rid, byte[] b, int offset, int len) {
      // TODO Auto-generated method stub

   }

   public int getBytesSize(int rid) {
      methodStarts();
      int i = insideGetBytesSize(rid, r);
      methodEnds();
      return i;
   }

   public Object getMorph(MorphParams p) {
      return this;
   }

   /**
    * When 2 threads use this method to create a new record, they have to synchronized anyways
    * because
    * @return
    */
   public int getNextID() {
      methodStarts();
      int nid = get4(ITB_OFFSET_03_NEXT4);
      methodEnds();
      return nid;
   }

   private void incrVersion() {
      increment(ITB_OFFSET_05_VERSION4, 4, 1);
   }

   private void initEmptyConstructor() {
      base = get4(ITB_OFFSET_02_BASE4);
   }

   /**
    * Writes a block
    * @param b
    * @param offset
    * @param newLen
    */
   private void insertBlock(byte[] b, int offset, int newLen, int expandSize, int flags) {
      //check if there is border block
      if (r.borderBlockSize != Integer.MIN_VALUE) {
         if (r.borderBlockSize == newLen) {
            //
            increment(r.blockBelowOffset + ITBR_BLOCK_OFFSET_02_NUMS3, 3, 1);
            //write something if there is something to write
            if (newLen > 0) {
               copyBytesTo(r.getRidOffset(), b, offset, newLen);
            }
         } else if (r.borderBlockSizeAbove == newLen) {
            increment(r.blockAboveOffset + ITBR_BLOCK_OFFSET_02_NUMS3, 3, 1);
            //shift back block data and write
            if (newLen > 0) {
               shiftBytesDown(r.blockAboveOffset, ITBR_BLOCK_SIZE, newLen);
               copyBytesTo(r.blockAboveOffset + ITBR_BLOCK_SIZE, b, offset, newLen);
            }
         }

      }
      //split the block, this insert 2 block
      expandDataArray(expandSize, r.offset);
      int roffset = r.getRidOffset();
      //write new block
      writeBlock(roffset, flags, 1, newLen);
      //copy our new bytes
      copyBytesTo(roffset + ITBR_BLOCK_SIZE, b, offset, newLen);
      roffset = roffset + ITBR_BLOCK_SIZE + newLen;
      //create next block
      int numAbove = r.getNumAboveRid();
      writeBlock(roffset, 0, numAbove, newLen);

      //count the number of elements below by d
      int numBelow = r.getNumBelowRid();
      int blockBelowOffset = r.offset;
      set3(blockBelowOffset + ITBR_BLOCK_OFFSET_02_NUMS3, numBelow);

      //update number of block by 2
      increment(ITBR_OFFSET_04_NUM_BLOCK3, 3, 2);
   }

   private int insideAddBytes(byte[] b, int offset, int len) {
      //how to code a null array?
      if (b == null) {
         //special null block
         len = LEN_NULL;
      }
      //when first byte added. init the header
      int numEl = get4(ITB_OFFSET_04_NUM_ELEMENTS4);
      int next = get4(ITB_OFFSET_03_NEXT4);
      int roffset = ITBR_BASIC_SIZE;
      if (numEl == 0) {
         r.size = Integer.MIN_VALUE;
      } else {
         insideGetBytesSize(next - 1, r);
         roffset = r.getRidOffset() + r.size;
      }
      int expandSize = len;
      if (r.size != len) {
         //create  
         expandSize += ITBR_BLOCK_SIZE;
      }
      expandData(expandSize);
      if (r.size != len) {
         writeBlock(roffset, 0, 1, len);
      }

      copyBytesTo(roffset + ITBR_BLOCK_SIZE, b, offset, len);
      increment(ITB_OFFSET_04_NUM_ELEMENTS4, 4, 1);
      increment(ITB_OFFSET_03_NEXT4, 4, 1);
      incrVersion();
      return next;
   }

   private void insideDeleteBytes(int rid) {
      insideSetBytes(rid, null, 0, 0, true);
      increment(ITB_OFFSET_04_NUM_ELEMENTS4, 4, -1);
      incrVersion();
   }

   protected byte[] insideGetBytes(int rid) {
      insideGetBytesSize(rid, r);
      //first look the size block in which rid resides
      if (r.hasFlag(ITBR_BLOCK_FLAG_1_NULL)) {
         return null;
      } else {
         int offset = r.getRidOffset();
         byte[] ar = new byte[r.size];
         readBytes(offset, ar, 0, ar.length);
         return ar;
      }
   }

   private int insideGetBytesSize(int rid, Request r) {
      int base = get4(ITB_OFFSET_02_BASE4);
      int next = get4(ITB_OFFSET_03_NEXT4);
      if (rid >= base && rid < next) {
         int numBelow = 0;
         int blockOffset = ITBR_BASIC_SIZE;
         int blockSize = get3(blockOffset + ITBR_BLOCK_OFFSET_02_SIZE3);
         int num = get3(blockOffset + ITBR_OFFSET_04_NUM_BLOCK3);
         int flags = get1(blockOffset + ITBR_BLOCK_OFFSET_01_FLAG1);
         if (!hasFlag(ITBR_OFFSET_01_FLAG1, ITBR_FLAG_1_UNIQUE_SIZE)) {
            //valid rid
            while (base + rid < num) {
               blockOffset = num * blockSize + ITBR_BLOCK_SIZE + blockOffset;
               blockSize = get3(blockOffset + ITBR_BLOCK_OFFSET_02_SIZE3);
               num = get3(blockOffset + ITBR_BLOCK_OFFSET_02_NUMS3);
               numBelow += num;
               flags = get3(blockOffset + ITBR_BLOCK_OFFSET_01_FLAG1);
            }
         }
         r.numBelow = numBelow;
         r.offset = blockOffset;
         r.size = blockSize;
         r.id = rid;
         r.num = num;
         r.flags = flags;
         return blockSize;
      }
      throw new IllegalArgumentException();
   }

   private void insideSetBytes(int id, byte[] b, int offset, int len) {
      insideSetBytes(id, b, offset, len, false);
   }

   private void insideSetBytes(int id, byte[] b, int offset, int len, boolean delete) {
      insideGetBytesSize(id, r);
      if (delete) {
         if (r.hasFlag(ITBR_BLOCK_FLAG_2_DELETED)) {
            //nothing to do
         } else {
            //delete rid
            int expandSize = ITBR_BLOCK_SIZE * 2;
            insertBlock(null, 0, LEN_DELETE, expandSize, 1);
         }
      } else if (b == null) {
         if (!r.hasFlag(ITBR_BLOCK_FLAG_1_NULL)) {
            //notning to do
         } else {
            //set bytes to null
            int expandSize = ITBR_BLOCK_SIZE * 2;
            insertBlock(null, 0, LEN_NULL, expandSize, 1);
         }
      } else {
         int newLen = len;
         if (r.size == newLen) {
            //just write it
            copyBytesTo(r.offset, b, offset, newLen);
         } else {
            int expandSize = ITBR_BLOCK_SIZE * 2 + newLen - r.size;
            insertBlock(b, offset, newLen, expandSize, 0);
         }

      }
   }

   public void serializeReverse() {
      initEmptyConstructor();
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      byte[] ar = getByteArrayCopy();
      return bc.serializeToUpdateAgentData(ar);
   }

   public void setBytes(int id, byte[] b) {
      //set a write lock.
      methodStartsWrite();
      insideSetBytes(id, b, 0, b.length);
      methodEndsWrite();
   }

   public void setBytes(int id, byte[] b, int offset, int len) {
      methodStartsWrite();
      insideSetBytes(id, b, 0, b.length);
      methodEndsWrite();
   }

   private void writeBlock(int roffset, int flags, int num, int size) {
      set1(roffset + ITBR_BLOCK_OFFSET_01_FLAG1, flags);
      set3(roffset + ITBR_BLOCK_OFFSET_02_NUMS3, num);
      set3(roffset + ITBR_BLOCK_OFFSET_02_SIZE3, size);
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToBytesRun");
      toStringPrivate(dc);
      super.toString(dc.sup());
      int start = get4(PS_OFFSET_04_START_POINTER4);
      int end = get4(ITB_OFFSET_03_NEXT4);
      for (int rid = start; rid < end; rid++) {
         byte[] b = getBytes(rid);
         dc.nl();
         dc.append(rid + " = " + pdc.getUCtx().getBU().toStringBytes(b, 0, ","));
      }
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntToBytesRun");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   


}
