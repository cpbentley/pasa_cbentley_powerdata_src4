package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRun;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Use the same principles as {@link PowerIntToBytesRun}.
 * <br>
 * 
 * Compress int[][] double array into a byte[] array
 * <br>
 * <br>
 * This class is read-only. For building mode, morph into build mode.
 * <br>
 * 
 * myInts function access the integers
 * 
 * The class uses a pointer table
 * 
 * [ bitPointerSize 1 byte ]
 * [ bitIntSize 1 byte ]
 * [ numChunks 2 bytes]
 * [ tableoffset 4 bytes]
 * [ lastpointer bit used 4 bytes]
 * [ lastDataBit used 4 bytes]
 * [ negative flag 1 byte]
 * [ 0 pointer array ]
 * [ 1 pointer array ]
 * [ 2 pointer array ]
 * [ 3 pointer array ]
 * [ bitspointer for 0] the pointer are at the end to make the smaller and hopefully consume less bits
 * [ bitspointer for 1]
 * [ bitspointer for 2]
 * [ bitspointer for 3]
 * <br>
 * <br>
 * Useful to store the page numbers at which a word appears in a book.
 * <br>
 * <br>
 * @author Charles Bentley
 *
 */
public class PowerIntToIntsRun extends PowerIntToInts implements IPowerLinkIntToInts, ITechIntToIntsRun {

   public int  bitIntSize;

   public int  bitPointerSize;

   boolean     hasNegatives = false;

   private int headersBit;

   private int headerSize;

   /**
    * pointer 
    */

   private int lastDataBitUsed;

   private int lastPointerBitUsed;

   public int  numChunks;

   public int  pointerTableOffset;

   public PowerIntToIntsRun(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerIntToIntsRunRootTech());
   }


   public PowerIntToIntsRun(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      bitPointerSize = get1(ITIS_RUN_OFFSET_02_POINTER_BIT_SIZE1);
      bitIntSize = get1(ITIS_RUN_OFFSET_03_INT_BIT_SIZE1);
      numChunks = get2(ITIS_RUN_OFFSET_04_NUM_CHUNKS2);
      pointerTableOffset = get4(ITIS_RUN_OFFSET_05_TABLE4);
      lastPointerBitUsed = get4(ITIS_RUN_OFFSET_06_LAST_POINTER4);
      lastDataBitUsed = get4(ITIS_RUN_OFFSET_07_LAST_DATA_BIT4);
      if (hasFlag(ITIS_RUN_OFFSET_01_FLAG1, ITIS_RUN_FLAG_1_NEGATIVE)) {
         hasNegatives = true;
      }
      headerSize = 17;
      headersBit = headerSize * 8;
   }

   public void removeValueFromKey(int value) {
      throw new RuntimeException();
   }

   /**
    * 
    * @param pointer
    * @return a non null pointer
    */
   public int[] getKeyValues(int pointer) {
      if (pointer < 0)
         return null;
      int bitPointerOffset = getBitPointerOffset(pointer);
      if ((index * 8) + lastPointerBitUsed <= bitPointerOffset) {
         return null;
      }
      //get the number of values associated with the pointer
      int num = getIntNumber(pointer);
      //now use c.
      BitCoordinate c = getChunkCoord(bitPointerOffset);
      int[] ints = new int[num];
      if (hasNegatives) {
         for (int i = 0; i < ints.length; i++) {
            int val = readNegativeValue(c);
            ints[i] = val;
         }
      } else {
         for (int i = 0; i < ints.length; i++) {
            int val = BitUtils.readBits(data, c, bitIntSize);
            ints[i] = val;
         }
      }
      return ints;
   }

   public int[] getKeysValues(int[] keys) {
      IntBuffer ib = new IntBuffer(pdc.getUC());
      for (int i = 0; i < keys.length; i++) {
         int key = keys[i];
         ib.addInt(getKeyValues(key));
      }
      return ib.getIntsClonedTrimmed();
   }

   
   public int getKeyValue(int pointer, int index) {
      int[] ss = getKeyValues(pointer);
      return ss[index];
   }

   /**
    * at which bit, does the pointer data starts
    * @param pointer
    * @return
    * loaded
    */
   public int getBitPointerOffset(int pointer) {
      //-1 because pointers start at 1 in our case
      return (index * 8) + (pointerTableOffset * 8) + ((bitPointerSize) * (pointer));
   }

   /**
    * From the offset in the pointer table, read the coordinate of the chunk
    * @param bitpointeroffset
    * @return
    * offset loaded
    */
   public BitCoordinate getChunkCoord(int bitpointeroffset) {
      BitCoordinate c = createBitCoordinate();
      c.map(bitpointeroffset);
      //reads the relative value
      int bitnum = BitUtils.readBits(data, c, bitPointerSize);
      c.map((index * 8) + headersBit + bitnum);
      return c;
   }

   /**
    * The number of ints for that pointer
    * @param pointer
    * @return
    */
   public int getIntNumber(int pointer) {
      if (pointer < 0)
         return 0;
      int bitpointer = getBitPointerOffset(pointer);
      return getIntNumber(pointer, getChunkCoord(bitpointer));
   }

   /**
    * The number of values at a pointer is computed
    * with the next pointer position
    * @param pointer
    * @param chunkCoord
    * @return
    */
   public int getIntNumber(int pointer, BitCoordinate chunkCoord) {
      int valdown = chunkCoord.unmap();
      int valup = 0;
      if (pointer + 1 >= numChunks) {
         valup = getLoadedLastBitUsed();
      } else {
         int bitpointer2 = getBitPointerOffset(pointer + 1);
         BitCoordinate c = getChunkCoord(bitpointer2);
         valup = c.unmap();
      }
      int bitsInChunk = valup - valdown;
      if (bitIntSize == 0)
         return 0;
      return bitsInChunk / bitIntSize;
   }

   public int getLoadedLastBitUsed() {
      return (index * 8) + lastDataBitUsed;
   }

   public Object getMorphedBuild(int type) {
      // TODO Auto-generated method stub
      return null;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getSize() {
      return 0;
   }

   public int getRowSize() {
      return numChunks;
   }

   public boolean hasPointer(int value) {
      return true;
   }

   /**
    * next available pointer
    */
   public int newPointer() {
      return numChunks;
   }

   /**
    * number of values at pointer
    */
   public int getNumValuesFromKey(int pointer) {
      return getKeyValues(pointer).length;
   }

   private int readNegativeValue(BitCoordinate c) {
      int signbit = BitUtils.readBit(data, c);
      int val = BitUtils.readBits(data, c, bitIntSize - 1);
      if (signbit == 1) {
         val = 0 - val;
      }
      return val;
   }

   /**
    * Remove pointer datas
    */
   public int removeKeyValues(int pointer) {
      throw new RuntimeException("not implemented");
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   
   public void addValueToKey(int pointer, int data) {
      // TODO Auto-generated method stub

   }

   
   public void addValuesToKey(int[] value, int data) {
      // TODO Auto-generated method stub

   }

   
   public int getKeyData(int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   public void setKeyData(int key, int data) {
      // TODO Auto-generated method stub

   }

   
   public boolean isValidKey(int key) {
      // TODO Auto-generated method stub
      return false;
   }

   
   public void removeValueFromKey(int value, int key) {
      // TODO Auto-generated method stub

   }

   
   public IPowerEnum getEnu(int type, Object param) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected int insideGetKeyValue(int key, int index) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected int insideGetIndexOfValueFromKey(int value, int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideSetKeyData(int key, int data) {
      // TODO Auto-generated method stub
      
   }

   
   protected int[] insideGetKeysValues(int[] keys) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected int insideGetKeyData(int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideAddValuesToKey(int[] value, int key) {
      // TODO Auto-generated method stub
      
   }

   
   protected void insideAddValueToKey(int value, int key) {
      // TODO Auto-generated method stub
      
   }

   
   protected int insideRemoveKeyValues(int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideRemoveValueFromKey(int value, int key) {
      // TODO Auto-generated method stub
      
   }

   
   protected int insideGetNumValuesFromKey(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected int[] insideGetKeyValues(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected void insideSetValuesForKey(int[] values, int key) {
      // TODO Auto-generated method stub
      
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToIntsRun");
      toStringPrivate(dc);
      super.toString(dc.sup());
      
   }

   private void toStringPrivate(Dctx sb) {
      int len = getLength();
      sb.append("Total size = " + len + " bytes or " + (len / 1000) + " kb or " + (len * 8) + " bits");
      sb.nl();
      sb.append("bitPointerSize=" + bitPointerSize + "(bit size of the pointer coordinate )");
      sb.nl();
      sb.append("bitIntSize=" + bitIntSize + " bit size consumed by 1 integer");
      sb.nl();
      sb.append("numChunks=" + numChunks);
      sb.nl();
      sb.append("pointerTableOffset\t=" + pointerTableOffset);
      sb.nl();
      sb.append("lastpointerbitused\t=" + lastPointerBitUsed);
      sb.nl();
      sb.append("lastdatabitused\t=" + lastDataBitUsed);
      sb.nl();
      sb.append("hasNegatives \t=" + hasFlag(ITIS_RUN_OFFSET_01_FLAG1, ITIS_RUN_FLAG_1_NEGATIVE));
      sb.nl();
      sb.append("pointer table size = " + (len - pointerTableOffset) + " bytes");
      sb.nl();
      sb.append("integer data size = " + (pointerTableOffset) + " bytes");

      sb.nl();
      sb.append("pointer position = byte:bit #datas:bitPointerOffset : values");
      sb.nl();
      int total = 0;
      for (int i = 0; i < numChunks; i++) {
         // the pointer bit address
         int bitpointeroffset = getBitPointerOffset(i);
         // the address of the data
         BitCoordinate c = getChunkCoord(bitpointeroffset);
         sb.append("pointer " + i);
         sb.append(" coord = " + c.getBytenum() + ":" + c.getBitnum());
         int num = getIntNumber(i);
         sb.append(" #" + num + ":" + bitpointeroffset + " : ");
         total += num;
         int[] vals = getKeyValues(i);
         for (int j = 0; j < vals.length; j++) {
            sb.append(vals[j]);
            if (j != vals.length - 1)
               sb.append(',');
         }
         if (i % 1 == 0)
            sb.nl();
         else
            sb.append(',');
      }
      if (numChunks != 0)
         sb.append("average per pointer = " + (total) / numChunks);
      sb.nl();
      sb.append("we have " + total + " integers that would take " + total * 4 + " raw bytes compared to " + len + " compressed bytes");
      double d = total * 4 / len;
      sb.nl();
      sb.append("Factor=" + Double.toString(d) + " " + d);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntToIntsRun");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   


}
