package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.io.BADataIS;
import pasa.cbentley.core.src4.io.BADataOS;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntSorter;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Builder for compressing int[][] double array into a byte[] array.
 * <br>
 * Buffer for double arrays of integers
 * <br>
 * Heuristic allow for faster computation but bigger use of memory
 * <br>
 * @author Charles Bentley
 *
 */
public class PowerIntToIntsBuild extends PowerIntToInts implements IPowerLinkIntToInts, ITechIntToIntsBuild {

   /**
    * The data is actually a buffer, the first 
    * <br>
    * When flag {@link ITechPointerStruct#PS_FLAG_6_REFERENCE}
    * <br>
    * We return references
    */
   private int[][]   data;

   /**
    * null when data has full references. after a deserialization for instance.
    * <br>
    * In build mode with many integer, it is good to use a buffer.
    * contain the lastused integer for that index
    */
   private int[]     lengthsOfArrays;

 
   public PowerIntToIntsBuild(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerIntToIntsBuildRootTech());
   }

   public PowerIntToIntsBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructor();
   }


   /**
    * If using
    * @param value
    * @param key
    * @return
    */
   private void addValueToArrayImpl(int value, int key) {
      int len = lengthsOfArrays[key];
      int val = getSorter().addToArray(value, data[key], 0, len);
      if (val == IntSorter.VALUE_ADDED) {
         lengthsOfArrays[key]++;
      } else if (val == IntSorter.VALUE_ADDED_AND_ARRAY_MODIFIED) {
         data[key] = getSorter().getArray();
         lengthsOfArrays[key]++;
      } else {
         //value was not added
      }
   }

   private void checkGet(int key) {
      if (key < 0 || key >= data.length) {
         throw new IllegalArgumentException(key + " >= " + data.length);
      }
   }

   private void checkNullArrayForPointer(int key) {
      if (data[key] == null) {
         data[key] = new int[get2(ITIS_OFFSET_03_COL_INIT_SIZE2)];
      }
   }

   /**
    * 
    * @param key
    * @return
    */
   private int ensureNextEmpty(int key) {
      if (data[key] == null) {
         data[key] = new int[1];
         return 0;
      } else {
         int currentNumberOfItems = lengthsOfArrays[key];
         if (data[key].length <= currentNumberOfItems) {
            int newCapacity = currentNumberOfItems + currentNumberOfItems / 2;
            data[key] = pdc.getUC().getMem().ensureCapacity(data[key], newCapacity);
         }
         return currentNumberOfItems;
      }
   }

   public IPowerEnum getEnu(int type, Object param) {
      if (type == 0) {
         return new IPowerEnum() {

            public Object getNext() {
               // TODO Auto-generated method stub
               return null;
            }

            public boolean hasNext() {
               // TODO Auto-generated method stub
               return false;
            }
         };
      }
      return null;
   }

   /**
    * This class is good for building a huge index base and then morph it quickly in a fast read
    * low memory version with its custom morph algo.
    * 
    */
   public Object getMorph(MorphParams p) {
      if (p.type == MODE_2_RUN) {
         int size = getSize();
         //ask factory to compress ? we don't want to know which
         //run implementation to use. its the job of the factory
         return pdc.getMorpher().createPowerIntToIntsRun(data, lengthsOfArrays, 0, size);
      } else {
         return this;
      }
   }

   private void initEmptyConstructor() {
      super.initEmpty();
      int irow = get2(ITIS_OFFSET_04_ROW_INIT_SIZE2);
      data = new int[irow][];
      lengthsOfArrays = new int[irow];
   }

   protected void insideAddValuesToKey(int[] value, int key) {
      makeKeyValid(key);
      checkNullArrayForPointer(key);
      for (int i = 0; i < value.length; i++) {
         addValueToArrayImpl(value[i], key);
      }
   }

   /**
    * Add the data for that pointer
    * @param _arrayData
    * @param key pointer range is [0, Integer.MAX_VALUE]
    */
   protected void insideAddValueToKey(int value, int key) {
      makeKeyValid(key);
      checkNullArrayForPointer(key);
      addValueToArrayImpl(value, key);
   }

   protected int insideGetIndexOfValueFromKey(int value, int key) {
      checkGet(key);
      if (data[key] == null) {
         return -1;
      }
      int len = lengthsOfArrays[key];
      return getSorter().getFirstIndexOfValue(value, data[key], 0, len);
   }

   protected int insideGetKeyData(int key) {
      checkGet(key);
      if (data[key] == null || data[key].length == 0) {
         return get4(ITI_OFFSET_02_DEF_VALUE4);
      }
      return data[key][0];
   }

   protected int[] insideGetKeysValues(int[] keys) {
      int size = 0;
      for (int i = 0; i < keys.length; i++) {
         int key = keys[i];
         int len = data[key].length;
         size += len;
      }
      int[] d = new int[size];
      int count = 0;
      for (int i = 0; i < keys.length; i++) {
         int key = keys[i];
         int len = data[key].length;
         for (int j = 0; j < len; j++) {
            d[count] = data[key][j];
            count++;
         }
      }
      return d;
   }

   protected int insideGetKeyValue(int key, int index) {
      checkGet(key);
      if (index >= lengthsOfArrays[key]) {
         throw new IllegalArgumentException();
      }
      return data[key][index];
   }

   /**
    * The method to retrieve values of a pointer.
    * A new array is created.
    * @param pointer must be a valid pointer i.e. >= 0
    * @return null array if key is null
    */
   protected int[] insideGetKeyValues(int key) {
      checkGet(key);
      if (data[key] == null) {
         return null;
      }
      int[] d = new int[lengthsOfArrays[key]];
      for (int i = 0; i < d.length; i++) {
         d[i] = data[key][i];
      }
      return d;
   }

   protected int insideGetNumValuesFromKey(int key) {
      checkGet(key);
      return lengthsOfArrays[key];
   }

   /**
    * Removes all values for pointer.
    * 
    * @param pointer
    * @return the number of elements in the array that was deleted
    */
   protected int insideRemoveKeyValues(int key) {
      checkGet(key);
      int size = 0;
      if (data[key] != null) {
         size = lengthsOfArrays[key];
         lengthsOfArrays[key] = 0;
      }
      data[key] = null;
      return size;
   }

   protected void insideRemoveValueFromKey(int value, int key) {
      checkGet(key);
      int index = insideGetIndexOfValueFromKey(value, key);
      if (index != -1) {
         //if we use buffers
         //just shift down
         IntUtils.shiftIntDown(data[key], 1, index, lengthsOfArrays[key], false);
         lengthsOfArrays[key]--;
      }
   }

   /**
    * Clone values
    */
   protected void insideSetValuesForKey(int[] values, int key) {
      makeKeyValid(key);
      this.data[key] = pdc.getUC().getIU().clone(values);
      lengthsOfArrays[key] = values.length;
   }

   protected void insideSetKeyData(int key, int data) {
      makeKeyValid(key);
      this.data[key] = new int[] { data };
      lengthsOfArrays[key] = 1;
   }

   /**
    * After calling this method, the key is valid data and firstEmpties accept the key
    * @param key
    */
   private void makeKeyValid(int key) {
      if (key < pointerStart) {
         data = checkPointersBelow(key, data);
         lengthsOfArrays = checkPointersBelow(key, 0, lengthsOfArrays);
         updateStartPointer(key);
      } else if (key >= pointerLast) {
         data = checkPointersTop(key, data);
         lengthsOfArrays = checkPointersTop(key, 0, lengthsOfArrays);
         updateLastPointer(key);
      }
   }

   private void serialeRevRaw() {
      BADataIS dis = getDataInputStream();
      int size = dis.readInt();
      data = new int[size][];
      lengthsOfArrays = new int[size];
      for (int i = 0; i < size; i++) {
         int len = dis.readInt();
         if (len != -1) {
            lengthsOfArrays[i] = len;
            int[] ar = new int[len];
            for (int j = 0; j < len; j++) {
               ar[j] = dis.readInt();
            }
            data[i] = ar;
         }
      }
      removeData();
   }

   /**
    * @return
    */
   public byte[] serializeRaw() {
      BADataOS dos = new BADataOS(pdc.getUC());
      int size = getSize();
      dos.writeInt(size);
      for (int i = 0; i < size; i++) {
         int len = -1;
         if (data[i] != null) {
            if (lengthsOfArrays != null) {
               len = lengthsOfArrays[i];
            } else {
               len = data[i].length;
            }
         }
         dos.writeInt(len);
         for (int j = 0; j < len; j++) {
            dos.writeInt(data[i][j]);
         }
      }
      return serializeRawHelper(dos.getOut().toByteArray());
   }

   public void serializeReverse() {
      if (hasData()) {
         super.initEmpty();
         serialeRevRaw();
      } else {
         initEmptyConstructor();
      }
   }

   /**
    * This class serialize to a Run
    */
   public ByteObjectManaged serializeTo(ByteController bc) {
      return bc.serializeToUpdateAgentData(serializeRaw());

   }

   //#mdebug

   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerIntToIntsBuild");
   }
   //#enddebug

   public void toString(Dctx sb) {
      sb.root(this, "PowerIntToIntsBuild");
      sb.appendVarWithSpace("Size", getSize());
      pdc.getTechFactory().toStringPowerIntToIntsBuildTech(sb.newLevel(), this);
      super.toString(sb.newLevel());

      int size = getSize();
      for (int i = 0; i < size; i++) {
         sb.nl();
         if (data[i] == null) {
            sb.append(i + "=null");
         } else {
            sb.append(i + "=");
            if (lengthsOfArrays != null) {
               sb.append("[" + lengthsOfArrays[i]);
               sb.append("]");
            }
            int len = 0;
            if (lengthsOfArrays != null) {
               len = lengthsOfArrays[i];
            } else {
               len = data[i].length;
            }
            for (int j = 0; j < len; j++) {
               sb.append(data[i][j]);
               if (j + 1 != len) {
                  sb.append(',');
               }
            }
         }
      }
      sb.nl();
      if (lengthsOfArrays == null) {
         sb.append("firstEmpties[] is null");
      } else {
         sb.append("firstEmpties[] length is " + lengthsOfArrays.length);
      }
      sb.nl();
      if (data == null) {
         sb.append("data[][] is null");
      } else {
         sb.append("data[] length is " + data.length);
      }
   }

   public void updatePointers(Object struct, Object mapping) {
      if (struct instanceof IPowerLinkIntToInt) {
         IPowerLinkIntToInt pl = (IPowerLinkIntToInt) struct;
         int start = pl.getTech().get4(PS_OFFSET_04_START_POINTER4);
         int end = pl.getTech().get4(PS_OFFSET_05_END_POINTER4);
         int[][] newData = new int[data.length][];
         int[] newF = null;
         if (lengthsOfArrays != null) {
            newF = new int[lengthsOfArrays.length];
         }
         //can pointer update change the start and end pointers? Yes possibly TODO
         //iterate on outside pointer
         for (int i = start; i < end; i++) {
            int newPointer = pl.getKeyData(i);
            int insideI = getInsidePointer(i);
            int insideNew = getInsidePointer(newPointer);
            newData[insideNew] = data[insideI];
            if (newF != null) {
               newF[insideNew] = lengthsOfArrays[insideI];
            }
         }
         data = newData;
         lengthsOfArrays = newF;
      }
   }
}
