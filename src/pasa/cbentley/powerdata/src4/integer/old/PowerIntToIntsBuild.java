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
//public class PowerIntToIntsBuild extends PowerIntToInts implements IPowerLinkIntToInts, ITechIntToIntsBuild {
//
//   /**
//    * The data is actually a buffer, the first 
//    * <br>
//    * When flag {@link ITechPointerStruct#PS_FLAG_6_REFERENCE}
//    * <br>
//    * We return references
//    */
//   private int[][] data;
//
//   /**
//    * null when data has full references. after a deserialization for instance.
//    * <br>
//    * In build mode with many integer, it is good to use a buffer.
//    * contain the lastused integer for that index
//    */
//   private int[]   firstEmpties;
//
//   public PowerIntToIntsBuild(PDCtx pdc) {
//      this(pdc, pdc.getTechFactory().getPowerIntToIntsBuildRootTech());
//   }
//
//   public PowerIntToIntsBuild(PDCtx pdc, ByteObjectManaged tech) {
//      super(pdc, tech);
//      initEmptyConstructor();
//   }
//
//   private void addEmptiesAppendPrefix(int value, int key) {
//      if (appendEmpties(value, key) != -1) {
//         data[key] = IntUtils.writeIntBEPrefix(data[key], value);
//      }
//   }
//
//   private void addEmptiesAppendSuffix(int value, int key) {
//      int firstEmpty = appendEmpties(value, key);
//      if (firstEmpty != -1) {
//         data[key][firstEmpty] = value;
//      }
//   }
//
//   private void addEmptiesOrderAsc(int value, int key) {
//      boolean isUnique = isUnique();
//      int firstEmpty = ensureNextEmpty(key);
//      boolean b = MUtils.addOrderedAscIntLognBuffer(data[key], value, isUnique, 0, firstEmpty);
//      if (b) {
//         firstEmpties[key]++;
//      }
//   }
//
//   /**
//    * When adding 0, we have an issue since all values are zeros in the buffer
//    * @param value
//    * @param key
//    */
//   private void addEmptiesOrderDesc(int value, int key) {
//      boolean isUnique = isUnique();
//      int firstEmpty = ensureNextEmpty(key);
//      boolean b = MUtils.addOrderedDescIntLognBuffer(data[key], value, isUnique, 0, firstEmpty);
//      if (b) {
//         firstEmpties[key]++;
//      }
//   }
//
//   /**
//    * If using
//    * @param value
//    * @param key
//    * @return
//    */
//   private void addValueToArrayEm(int value, int key) {
//      if (firstEmpties == null) {
//         data[key] = super.addValueToArray(value, data[key]);
//      } else {
//         data[key] = super.addValueToArray(value, data[key]);
//      }
//   }
//
//   private int appendEmpties(int value, int key) {
//      int firstEmpty = ensureNextEmpty(key);
//      if (isUnique()) {
//         if (MUtils.contains(data[key], value, 0, firstEmpty)) {
//            return -1;
//         }
//      }
//      firstEmpties[key]++;
//      return firstEmpty;
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
//         if (hasFlag(ITIS_BUILD_OFFSET_01_FLAG1, ITIS_BUILD_FLAG_1_EMPTIES)) {
//            data[pointer] = new int[get2(ITIS_OFFSET_03_COL_INIT_SIZE2)];
//         } else {
//            data[pointer] = new int[0];
//         }
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
//   
//   public IPowerEnum getEnu(int type, Object param) {
//      if (type == 0) {
//         return new IPowerEnum() {
//
//            
//            public Object getNext() {
//               // TODO Auto-generated method stub
//               return null;
//            }
//
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
//   
//   protected int insideGetIndexOfValueFromKey(int value, int key) {
//      checkGet(key);
//      if (data[key] == null) {
//         return -1;
//      }
//      int len = data[key].length;
//      if (firstEmpties != null) {
//         len = firstEmpties[key];
//      }
//      int type = get1(ITIS_OFFSET_02_ORDER_TYPE1);
//      if (type == ITIS_ARRAY_ORDER_2_ASC) {
//         return MUtils.findOrderIndexAsc(value, data[key], 0, len);
//      } else if (type == ITIS_ARRAY_ORDER_3_DESC) {
//         return MUtils.findOrderIndexDesc(value, data[key], 0, len);
//      } else {
//         return MUtils.findValue(value, data[key], 0, len);
//      }
//   }
//
//   protected int insideGetKeyData(int key) {
//      checkGet(key);
//      if (data[key] == null || data[key].length == 0) {
//         return get4(ITI_OFFSET_02_DEF_VALUE4);
//      }
//      return data[key][0];
//   }
//
//   protected int[] insideGetKeysValues(int[] keys) {
//      int size = 0;
//      for (int i = 0; i < keys.length; i++) {
//         int key = keys[i];
//         int len = data[key].length;
//         size += len;
//      }
//      int[] d = new int[size];
//      int count = 0;
//      for (int i = 0; i < keys.length; i++) {
//         int key = keys[i];
//         int len = data[key].length;
//         for (int j = 0; j < len; j++) {
//            d[count] = data[key][j];
//            count++;
//         }
//      }
//      return d;
//   }
//
//   protected int insideGetKeyValue(int key, int index) {
//      checkGet(key);
//      if (firstEmpties != null) {
//         if (index >= firstEmpties[key]) {
//            throw new IllegalArgumentException();
//         }
//      }
//      return data[key][index];
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
//      if (firstEmpties == null) {
//         if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_7_COPIES)) {
//            int[] d = new int[data[key].length];
//            for (int i = 0; i < data[key].length; i++) {
//               d[i] = data[key][i];
//            }
//            return d;
//         } else {
//            //case where we are able to return a direct reference
//            return data[key];
//         }
//      } else {
//         int[] d = new int[firstEmpties[key]];
//         for (int i = 0; i < d.length; i++) {
//            d[i] = data[key][i];
//         }
//         return d;
//      }
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
//      this.data[key] = new int[] { data };
//      if (firstEmpties != null) {
//         firstEmpties[key] = 1;
//      }
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
//      data = new int[size][];
//      if (hasFlag(ITIS_BUILD_OFFSET_01_FLAG1, ITIS_BUILD_FLAG_1_EMPTIES)) {
//         firstEmpties = new int[size];
//      }
//      for (int i = 0; i < size; i++) {
//         int len = dis.readInt();
//         if (len != -1) {
//            if (firstEmpties != null) {
//               firstEmpties[i] = len;
//            }
//            int[] ar = new int[len];
//            for (int j = 0; j < len; j++) {
//               ar[j] = dis.readInt();
//            }
//            data[i] = ar;
//         }
//      }
//      removeData();
//   }
//
//   /**
//    * @return
//    */
//   public byte[] serializeRaw() {
//      BADataOS dos = new BADataOS(pdc.getUCtx());
//      int size = getSize();
//      dos.writeInt(size);
//      for (int i = 0; i < size; i++) {
//         int len = -1;
//         if (data[i] != null) {
//            if (firstEmpties != null) {
//               len = firstEmpties[i];
//            } else {
//               len = data[i].length;
//            }
//         }
//         dos.writeInt(len);
//         for (int j = 0; j < len; j++) {
//            dos.writeInt(data[i][j]);
//         }
//      }
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
//   
//   public void toString1Line(Dctx dc) {
//      dc.root(this, "PowerIntToIntsBuild");
//   }
//   //#enddebug
//
//   
//   public void toString(Dctx sb) {
//      sb.root(this, "PowerIntToIntsBuild");
//      sb.appendVarWithSpace("Size", getSize());
//      pdc.getTechFactory().toStringPowerIntToIntsBuildTech(sb.nLevel(), this);
//      super.toString(sb.nLevel());
//
//      int size = getSize();
//      for (int i = 0; i < size; i++) {
//         sb.nl();
//         if (data[i] == null) {
//            sb.append(i + "=null");
//         } else {
//            sb.append(i + "=");
//            if (firstEmpties != null) {
//               sb.append("[" + firstEmpties[i]);
//               sb.append("]");
//            }
//            int len = 0;
//            if (firstEmpties != null) {
//               len = firstEmpties[i];
//            } else {
//               len = data[i].length;
//            }
//            for (int j = 0; j < len; j++) {
//               sb.append(data[i][j]);
//               if (j + 1 != len) {
//                  sb.append(',');
//               }
//            }
//         }
//      }
//      sb.nl();
//      if (firstEmpties == null) {
//         sb.append("firstEmpties[] is null");
//      } else {
//         sb.append("firstEmpties[] length is " + firstEmpties.length);
//      }
//      sb.nl();
//      if (data == null) {
//         sb.append("data[][] is null");
//      } else {
//         sb.append("data[] length is " + data.length);
//      }
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
