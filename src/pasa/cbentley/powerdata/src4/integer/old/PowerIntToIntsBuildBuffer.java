package pasa.cbentley.powerdata.src4.integer.old;
//package pasa.cbentley.powerdata.integer;
//
//import mordan.datastruct.power.IPowerEnum;
//import mordan.datastruct.power.MorphParams;
//import mordan.datastruct.power.integers.IPowerLinkIntToInt;
//import mordan.datastruct.power.integers.IPowerLinkIntToInts;
//import mordan.datastruct.power.itech.ITechIntToIntsBuild;
//import mordan.datastruct.power.itech.ITechPointerStruct;
//import pasa.cbentley.byteobjects.core.ByteController;
//import pasa.cbentley.byteobjects.core.ByteObjectManaged;
//import pasa.cbentley.core.src4.io.BADataIS;
//import pasa.cbentley.core.src4.io.BADataOS;
//import pasa.cbentley.core.src4.logging.Dctx;
//import pasa.cbentley.core.src4.structs.IntBuffer;
//import pasa.cbentley.core.src4.structs.IntSorter;
//import pasa.cbentley.powerdata.ctx.PDCtx;
//
///**
// * Builder for compressing int[][] double array into a byte[] array.
// * <br>
// * Buffer for double arrays of integers
// * <br>
// * Heuristic allow for faster computation but bigger use of memory
// * <br>
// * @author Charles Bentley
// *
// */
//public class PowerIntToIntsBuildBuffer extends PowerIntToInts implements IPowerLinkIntToInts, ITechIntToIntsBuild {
//
//   /**
//    * The data is actually a buffer, the first 
//    * <br>
//    * When flag {@link ITechPointerStruct#PS_FLAG_6_REFERENCE}
//    * <br>
//    * We return references
//    */
//   private IntBuffer[] data;
//
//   private IntSorter   sorter;
//
//   public PowerIntToIntsBuildBuffer(PDCtx pdc) {
//      this(pdc, pdc.getTechFactory().getPowerIntToIntsBuildRootTech());
//   }
//
//   public PowerIntToIntsBuildBuffer(PDCtx pdc, ByteObjectManaged tech) {
//      super(pdc, tech);
//      initEmptyConstructor();
//   }
//
//   public IntSorter getSorter() {
//      if (sorter == null) {
//         int type = get2(ITIS_OFFSET_02_ORDER_TYPE1);
//         boolean isUnique = !hasFlag(ITIS_OFFSET_01_FLAG, ITIS_FLAG_2_DUPLICATES);
//         sorter = new IntSorter(getUCtx(), type, isUnique);
//      }
//      return sorter;
//   }
//
//   private void checkGet(int key) {
//      if (key < 0 || key >= data.length) {
//         throw new IllegalArgumentException(key + " >= " + data.length);
//      }
//   }
//
//   private void checkNullArrayForPointer(int pointer) {
//      if (data[pointer] == null) {
//         int sizeInit = 0;
//         if (hasFlag(ITIS_BUILD_OFFSET_01_FLAG1, ITIS_BUILD_FLAG_1_EMPTIES)) {
//            sizeInit = get2(ITIS_OFFSET_03_COL_INIT_SIZE2);
//         }
//         data[pointer] = new IntBuffer(pdc.getUCtx(), sizeInit);
//      }
//   }
//
//   /**
//    * 
//    * @param key
//    * @return
//    */
//   private int ensureNextEmpty(int key) {
//      if (data[key] == null) {
//         data[key] = new int[1];
//         return 0;
//      } else {
//         int firstEmpty = firstEmpties[key];
//         if (data[key].length <= firstEmpty) {
//            data[key] = pdc.getUCtx().getMem().ensureCapacity(data[key], firstEmpty + firstEmpty / 2);
//         }
//         return firstEmpty;
//      }
//   }
//
//   public IPowerEnum getEnu(int type, Object param) {
//      if (type == 0) {
//         return new IPowerEnum() {
//
//            public Object getNext() {
//               // TODO Auto-generated method stub
//               return null;
//            }
//
//            public boolean hasNext() {
//               // TODO Auto-generated method stub
//               return false;
//            }
//         };
//      }
//      return null;
//   }
//
//   /**
//    * This class is good for building a huge index base and then morph it quickly in a fast read
//    * low memory version with its custom morph algo.
//    * 
//    */
//   public Object getMorph(MorphParams p) {
//      if (p.type == MODE_2_RUN) {
//         int size = getSize();
//         //ask factory to compress ? we don't want to know which
//         //run implementation to use. its the job of the factory
//         return pdc.getTechFactory().getMorpher().createPowerIntToIntsRun(data, firstEmpties, 0, size);
//      } else {
//         return this;
//      }
//   }
//
//   private void initEmptyConstructor() {
//      super.initEmpty();
//      int irow = get2(ITIS_OFFSET_04_ROW_INIT_SIZE2);
//      data = new int[irow][];
//      if (hasFlag(ITIS_BUILD_OFFSET_01_FLAG1, ITIS_BUILD_FLAG_1_EMPTIES)) {
//         firstEmpties = new int[irow];
//      }
//   }
//
//   protected void insideAddValuesToKey(int[] value, int key) {
//      manageArrayP(key);
//      for (int i = 0; i < value.length; i++) {
//         addValueToArrayEm(value[i], key);
//      }
//   }
//
//   /**
//    * Add the data for that pointer
//    * @param _arrayData
//    * @param key pointer range is [0, Integer.MAX_VALUE]
//    */
//   protected void insideAddValueToKey(int value, int key) {
//      manageArrayP(key);
//      checkNullArrayForPointer(key);
//      addValueToArrayEm(value, key);
//   }
//
//   protected int insideGetIndexOfValueFromKey(int value, int key) {
//      checkGet(key);
//      if (data[key] == null) {
//         return -1;
//      }
//      return data[key].getFirstIndexOf(value);
//   }
//
//   protected int insideGetKeyData(int key) {
//      checkGet(key);
//      if (data[key] == null || data[key].getSize() == 0) {
//         return get4(ITI_OFFSET_02_DEF_VALUE4);
//      }
//      return data[key].get(0);
//   }
//
//   protected int[] insideGetKeysValues(int[] keys) {
//      int size = 0;
//      for (int i = 0; i < keys.length; i++) {
//         int key = keys[i];
//         int len = data[key].getSize();
//         size += len;
//      }
//      int[] d = new int[size];
//      int count = 0;
//      for (int i = 0; i < keys.length; i++) {
//         int key = keys[i];
//         data[key].appendBufferToArrayAt(d, count);
//      }
//      return d;
//   }
//
//   protected int insideGetKeyValue(int key, int index) {
//      checkGet(key);
//      return data[key].get(index);
//   }
//
//   /**
//    * The method to retrieve values of a pointer
//    * @param pointer must be a valid pointer i.e. >= 0
//    * @return
//    */
//   protected int[] insideGetKeyValues(int key) {
//      checkGet(key);
//      if (data[key] == null) {
//         return null;
//      }
//      return data[key].getIntsClonedTrimmed();
//   }
//
//   protected int insideGetNumValuesFromKey(int key) {
//      checkGet(key);
//      if (data[key] == null) {
//         return 0;
//      }
//      if (firstEmpties == null) {
//         return data[key].length;
//      } else {
//         return firstEmpties[key];
//      }
//   }
//
//   /**
//    * Removes all values for pointer.
//    * 
//    * @param pointer
//    */
//   protected int insideRemoveKeyValues(int key) {
//      checkGet(key);
//      int size = 0;
//      if (data[key] != null) {
//         size = data[key].length;
//         if (firstEmpties != null) {
//            size = firstEmpties[key];
//            firstEmpties[key] = 0;
//         }
//      }
//      data[key] = null;
//      return size;
//   }
//
//   protected void insideRemoveValueFromKey(int value, int key) {
//      checkGet(key);
//      int index = insideGetIndexOfValueFromKey(value, key);
//      if (index != -1) {
//         int[] vals = insideGetKeyValues(key);
//         vals = MUtils.removeIndex(vals, index);
//         insideSetValuesForKey(vals, key);
//      }
//   }
//
//   /**
//    * 
//    */
//   protected void insideSetValuesForKey(int[] values, int key) {
//      manageArrayP(key);
//      this.data[key] = MUtils.clone(values);
//      if (firstEmpties != null) {
//         firstEmpties[key] = values.length;
//      }
//   }
//
//   protected void insideSetKeyData(int key, int data) {
//      manageArrayP(key);
//      this.data[key].clear();
//      this.data[key].addInt(data);
//   }
//
//   /**
//    * After calling this method, data and firstEmpties accept the key
//    * @param key
//    */
//   private void manageArrayP(int key) {
//      if (key < pointerStart) {
//         data = checkPointersBelow(key, data);
//         if (firstEmpties != null) {
//            firstEmpties = checkPointersBelow(key, 0, firstEmpties);
//         }
//         updateStartPointer(key);
//      } else if (key >= pointerLast) {
//         data = checkPointersTop(key, data);
//         if (firstEmpties != null) {
//            firstEmpties = checkPointersTop(key, 0, firstEmpties);
//         }
//         updateLastPointer(key);
//      }
//   }
//
//   private void serialeRevRaw() {
//      BADataIS dis = getDataInputStream();
//      int size = dis.readInt();
//      removeData();
//   }
//
//   /**
//    * @return
//    */
//   public byte[] serializeRaw() {
//      BADataOS dos = new BADataOS(pdc.getUCtx());
//      int size = getSize();
//      return serializeRawHelper(dos.getOut().toByteArray());
//   }
//
//   public void serializeReverse() {
//      if (hasData()) {
//         super.initEmpty();
//         serialeRevRaw();
//      } else {
//         initEmptyConstructor();
//      }
//   }
//
//   /**
//    * This class serialize to a Run
//    */
//   public ByteObjectManaged serializeTo(ByteController bc) {
//      return bc.serializeToUpdateAgentData(serializeRaw());
//
//   }
//
//   //#mdebug
//
//   public void toString1Line(Dctx dc) {
//      dc.root(this, "PowerIntToIntsBuild");
//   }
//   //#enddebug
//
//   public void toString(Dctx sb) {
//      sb.root(this, "PowerIntToIntsBuild");
//      sb.appendVarWithSpace("Size", getSize());
//      pdc.getTechFactory().toStringPowerIntToIntsBuildTech(sb.nLevel(), this);
//      super.toString(sb.nLevel());
//
//      int size = getSize();
//   }
//
//   public void updatePointers(Object struct, Object mapping) {
//      if (struct instanceof IPowerLinkIntToInt) {
//         IPowerLinkIntToInt pl = (IPowerLinkIntToInt) struct;
//         int start = pl.getTech().get4(PS_OFFSET_04_START_POINTER4);
//         int end = pl.getTech().get4(PS_OFFSET_05_END_POINTER4);
//         int[][] newData = new int[data.length][];
//         int[] newF = null;
//         if (firstEmpties != null) {
//            newF = new int[firstEmpties.length];
//         }
//         //can pointer update change the start and end pointers? Yes possibly TODO
//         //iterate on outside pointer
//         for (int i = start; i < end; i++) {
//            int newPointer = pl.getKeyData(i);
//            int insideI = getInsidePointer(i);
//            int insideNew = getInsidePointer(newPointer);
//            newData[insideNew] = data[insideI];
//            if (newF != null) {
//               newF[insideNew] = firstEmpties[insideI];
//            }
//         }
//         data = newData;
//         firstEmpties = newF;
//      }
//   }
//}
