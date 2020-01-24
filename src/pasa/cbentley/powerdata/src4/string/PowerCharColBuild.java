package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechMorph;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToBytes;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * String handles are stable.
 * <br>
 * <br>
 * Searching without an index is O(n)
 * <br>
 * <br>
 * Use the {@link ByteObjectManaged} just for its header which will never be expanded.
 * 
 * @author Charles Bentley
 *
 */
public class PowerCharColBuild extends PowerCharCol implements IPowerCharCollector, ITechCharColBuild {

   /**
    * 
    */
   char[][]    charData;

   /**
    * Table to match old pointer to new pointers.
    * <br>
    * <br>
    * When {@link PowerCharColBuild} datas are shuffles, Users hold the previous pointer.
    * They need to get the new pointer.
    */
   int[]       conversionTable;

   /**
    * Last accepted pointer.
    * Int value
    */
   int         lastused     = -1;

   /**
    * First valid index for the char collection.
    */
   int         start;

   private int statFound    = 0;

   /**
    * Statistic for index fails
    */
   private int statNotFound = 0;

   /**
    * 
    * @param mod
    */
   public PowerCharColBuild(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerCharColBuildTechRoot());
   }

   /**
    * Creates a freshly new instance with the Tech params as defined in {@link ITechCharColBuild}.
    * <br>
    * <br>
    * Being a
    * The {@link ByteObjectManaged} has 3 possibilities
    * <li> Definition (no data). With no {@link ByteController} the data part is ignored
    * <li> Definition (no data) with {@link ByteController}
    * <li> Defintion and Data with {@link ByteController} (Dezeriali
    * <br>
    * <br>
    * 1st case  occurs in a basic build set up.
    * 
    * @param tech
    * 
    */
   public PowerCharColBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      setFlag(AGENT_OFFSET_01_FLAG_1, AGENT_FLAG_CTRL_7_DATA_UNLOADED, true);
   }

   /**
    * Add all characters
    * <br>
    * <br>
    * @param another
    * @param pointers function for matching a pointer of here to a pointer 
    */
   public void addAllData(PowerCharColBuild another, int[] pointers) {
      for (int pointerHere = start; pointerHere <= lastused; pointerHere++) {
         int pointerOfAnother = another.addChars(charData[pointerHere], 0, charData[pointerHere].length);
         pointers[pointerHere] = pointerOfAnother;
      }
   }

   /**
    * Supposed to Notify {@link IPointerUser}. This never happens since this class has pointer stability
    * <br>
    * Are we always stable? Add is stable but remove is not
    */
   public void addPointerUser(IPointerUser pointerUser) {
      //nothing to do here because we are stable.
   }

   void getAlphaInfo(int[] plane, int[] startLetterMapping, int[] allLetterMapping) {
      plane[0] = (charData[start][0] >>> 8) & 0xFF;

      for (int i = start; i <= lastused; i++) {
         int len = charData[i].length;
         for (int j = 0; j < len; j++) {
            int lplane = (charData[i][j] >>> 8) & 0xFF;
            int index = (charData[i][j] >>> 0) & 0xFF;
            if (j == 0) {
               startLetterMapping[index]++;
            }
            if (lplane != plane[0]) {
               System.out.println("#CharPowerColBuild#getAlpahInfo Warning Loss of Plane Info for " + new String(charData[i]) + " Different planes " + lplane + "!=" + plane);
            }
            allLetterMapping[index]++;
         }
      }
   }

   public int getBiggestWordSize() {
      methodStarts();
      int max = 0;
      for (int i = 0; i < charData.length; i++) {
         if (charData[i].length > max) {
            max = charData[i].length;
         }
      }
      set1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1, max);
      methodEnds();
      return max;
   }

   /**
    * When morphing, the char pointer have changed so the method {@link IPowerCharCollector#getNewPointers()}
    * must be called on stored char pointers for the old version so they are valid for the new structure.
    * TODO change pointer matching into an interface. some structure will have random char pointers.
    * The conversion table from the morph is given in the object params.
    * <br>
    * <br>
    * {@link IPowerLinkIntToInt} is set in params at index 1 in order to do the conversion.
    * @param type
    * @param params
    * @param mc
    * @return
    */
   public Object getMorph(MorphParams p) {
      if (p.cl == IntToStrings.class) {
         IntToStrings its = new IntToStrings(getUCtx(), insideGetSize());
         for (int insidePointer = start; insidePointer <= lastused; insidePointer++) {
            its.add(getOutsidePointer(insidePointer), new String(charData[insidePointer]));
         }
         return its;
      }
      if (p.type == ITechMorph.MORPH_FLAG_1_RUN) {
         BuildToRunConverter conv = new BuildToRunConverter(pdc);
         return conv.getCharRun(this);
      }
      return this;
   }

   /**
    * Pointer index table to update after a BuildToRun Conversion.
    */
   public int[] getNewPointers() {
      return conversionTable;
   }

   public int getStatFound() {
      return statFound;
   }

   public int getStatNotFound() {
      return statNotFound;
   }

   /**
    * No Data Constructor
    */
   protected void initEmptyConstructor() {
      super.initEmptyConstructor();
      start = 0;
      charData = new char[start + 10][];
      lastused = start - 1;
      buildIndex();
   }

   /**
    * Returns outsidePointer
    */
   protected int insideAddChars(char[] c, int offset, int len) {
      //check if value is already there
      if (!isDuplicateAllowed()) {
         int insidePointer = insideFind(c, offset, len);
         if (insidePointer != IPowerCharCollector.CHARS_NOT_FOUND)
            return insidePointer;
      }
      //check for data
      if (lastused + 1 >= charData.length) {
         char[][] old = charData;
         charData = new char[(old.length + 1) * 2][];
         for (int i = 0; i < old.length; i++) {
            charData[i] = old[i];
         }
      }
      //add string
      char[] ar = new char[len];
      for (int i = 0; i < ar.length; i++) {
         ar[i] = c[offset + i];
      }
      charData[lastused + 1] = ar;

      //insert word in the index
      if (indexString != null) {
         indexString.insertWord(c, offset, len, lastused + 1);
         //validation
         byte[] data = indexString.find(c, offset, len);
         if (data.length == 0) {
            throw new RuntimeException(new String(c, offset, len));
         }
         if (IntUtils.readIntBE(data, 0) != lastused + 1) {
            throw new RuntimeException();
         }
      }
      lastused++;
      increment(PS_OFFSET_06_NUM_POINTER4, 4, 1);
      if (len > get1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1)) {
         set1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1, len);
      }
      return lastused;
   }

   protected void insideAppendChars(int insidePointer, StringBBuilder sb) {
      if (charData[insidePointer] != null) {
         sb.append(charData[insidePointer]);
      }
   }

   protected int insideCopyChars(int insidePointer, char[] c, int offset) {
      char[] cc = charData[insidePointer];
      for (int i = 0; i < cc.length; i++) {
         c[offset + i] = cc[i];
      }
      int val = cc.length;
      return val;
   }

   /**
    * Returns insidePointer
    */
   protected int insideFind(char[] str, int offset, int len) {
      int value = IPowerCharCollector.CHARS_NOT_FOUND;
      if (indexString != null) {
         byte[] d = indexString.find(str, offset, len);
         if (d.length == 0) {
            statNotFound++;
            value = IPowerCharCollector.CHARS_NOT_FOUND;
         } else {
            statFound++;
            value = IntUtils.readIntBE(d, 0);
         }
      } else {
         for (int i = start; i <= lastused; i++) {
            if (charData[i] == null || charData[i].length != len) {
               continue;
            }
            int count = 0;
            boolean right = true;
            for (int j = offset; j < offset + len; j++) {
               if (str[j] != charData[i][count]) {
                  right = false;
                  break;
               }
               count++;
            }
            if (right) {
               value = i;
               break;
            }
         }
      }
      return value;
   }

   protected char insideGetChar(int insidePointer) {
      if (insidePointer < 0 || charData.length <= insidePointer) {
         throw new IllegalArgumentException("" + insidePointer);
      }
      char c = 0;
      if (charData[insidePointer] != null && charData[insidePointer].length != 0) {
         //synchronized?
         c = charData[insidePointer][0];
      }
      return c;
   }

   /**
    * 
    */
   protected char[] insideGetChars(int insidePointer) {
      if (charData[insidePointer] != null)
         return charData[insidePointer];
      return new char[0];
   }

   public String insideGetKeyStringFromPointer(int insidePointer) {
      char[] c = insideGetChars(insidePointer);
      return new String(c);
   }

   protected int insideGetLen(int insidePointer) {
      int val = 0;
      if (charData[insidePointer] != null)
         val = charData[insidePointer].length;
      return val;
   }

   protected int insideGetPointer(char[] chars, int offset, int len) {
      return insideFind(chars, 0, chars.length);
   }

   /**
    * An array describing the different sizes of strings in this {@link PowerCharColBuild}.
    * <br>
    * 
    * <li>int[1][0] gives you the number of 1 sized strings
    * <li>int[1][1...k1] contains an array with pointers to all 1 sized strings, except for the first value.
    * <li>...
    * <li>int[n][0] gives you the number of n sized strings
    * <li>int[n][1...kn] contains an array with pointers to all n sized strings, except for the first value.
    * 
    * <br>
    * <br>
    * When the {@link PowerCharColBuild} is empty, returns a zero sized array.
    * <br>
    * <br>
    * The length of int[] is the maximum string length.
    * <br>
    * <br>
    * int[0][0] gives the maximum amplitude of any length
    * 
    * @returns an array of sizes to pointers in the main char array
    */
   protected int[][] insideGetSizes() {
      if (insideGetSize() == 0) {
         return new int[1][];
      }
      int startsize = 10;
      // index for word length, contains all pointers to words of that length
      // the +1 is for all strings bigger than maxlen
      int[][] sizes = new int[1][startsize];
      //initialize the sizes array with the 0 index as the counter
      for (int i = 0; i < sizes.length; i++) {
         sizes[i][0] = 0;
      }
      int max = 1;
      //for all char arrays stored
      for (int i = start; i <= lastused; i++) {
         int len = charData[i].length;
         if (len >= sizes.length) {
            //increase the number of rows
            sizes = pdc.getUCtx().getMem().increaseCapacityNonEmpty(sizes, len - sizes.length + 1, startsize);
            sizes[len][0] = 1;
            sizes[len][1] = i;
         } else {
            int position = sizes[len][0] + 1;
            if (position >= sizes[len].length) {
               sizes[len] = pdc.getUCtx().getMem().increaseCapacity(sizes[len], sizes[len].length);
            }
            sizes[len][position] = i;
            sizes[len][0]++;
            if (sizes[len][0] > max) {
               max = sizes[len][0];
            }
         }
      }
      sizes[0][0] = max;
      sizes[0][1] = getSize();
      return sizes;
   }

   protected boolean insideHasChars(char[] c, int offset, int len) {
      return IPowerCharCollector.CHARS_NOT_FOUND != insideFind(c, offset, len);
   }

   /**
    * If use force, the pointer will be used for upper data, in effect shifting all pointers down.
    */
   protected int insideRemove(int insidePointer, boolean useForce) {
      if (indexString != null) {
         indexString.removeWord(charData[insidePointer]);
      }
      charData[insidePointer] = null;
      if (useForce) {
         //copy the char below
         for (int i = insidePointer; i <= lastused; i++) {
            charData[i] = charData[i + 1];
         }
         //update pointers!!

      }
      increment(PS_OFFSET_06_NUM_POINTER4, 4, -1);
      return insidePointer;
   }

   protected int insideSetChars(int insidePointer, char[] d, int offset, int len) {
      char[] cc = new char[len];
      for (int i = 0; i < cc.length; i++) {
         cc[i] = d[offset + i];
      }
      charData[insidePointer] = cc;
      return insidePointer;
   }

   public boolean isDuplicateAllowed() {
      return hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_3_DUPLICATES);
   }

   //#enddebug

   /**
    * Checks the validity of the pointer to a non null 
    */
   public boolean isValid(int outsidePointer) {
      int pointerInside = getInsidePointer(outsidePointer);
      boolean b = false;
      if (pointerInside >= start && pointerInside <= lastused && charData[pointerInside] != null) {
         b = true;
      }
      return b;
   }

   protected void memoryClearSub() {
      charData = null;
      lastused = -1;
   }

   public void searchIndex(CharSearchSession css) {

   }

   /**
    * Only serialize class data. other ByteObjectManaged are not serialized here.
    * <br>
    * Must be called within a lock
    * @return
    */
   private byte[] serializeRaw() {
      methodStarts();
      //returns the ByteObjectManaged empty of data with headers and trailers
      byte[] header = toByteArray();

      //#debug
      printDataStruct("CharPowerColBuild#serializePack Header Length=" + header.length);

      //encapsulates the header for easier manipulation.
      ByteObjectManaged tr = new ByteObjectManaged(getBOC(), header);
      tr.set1(AGENT_OFFSET_03_FLAGZ_1, 0);
      //int trailerSize = tr.getTrailerSize();
      //by default sets flags for root
      //BitMask.setFlag(header, AGENT_OFFSET_07_FLAG_CTRL_1, AGENT_FLAG_CTRL_4_PACKED, true);
      //BitMask.setFlag(header, AGENT_OFFSET_07_FLAG_CTRL_1, AGENT_FLAG_CTRL_5_ROOT, true);

      //compute data size
      int totalDataSize = 0; //+3 for len and base plane
      totalDataSize += 1; //1 byte for basePlane
      totalDataSize += 4; //4 bytes for lastused
      int basePlane = 0;
      byte[][] byteChars = null;
      if (lastused != start - 1) {
         byteChars = new byte[lastused + 1][];
         //identifies plane properties
         char baseChar = charData[start][0];
         basePlane = (byte) ((baseChar >>> 8) & 0xFF);
         for (int i = start; i <= lastused; i++) {
            char[] ar = charData[i];
            byte[] charByteArray = StringUtils.getCharByteArrayPlane(ar, 0, ar.length, basePlane);
            byteChars[i] = charByteArray;
            totalDataSize += charByteArray.length;

         }
      }
      //copy the header
      tr.expandResetArrayData(totalDataSize); //this manages 

      byte[] data = tr.getByteObjectData();
      int offsetData = tr.getDataOffsetStartLoaded();
      tr.set4(offsetData, lastused);
      offsetData += 4;
      tr.set1(offsetData, basePlane);
      offsetData += 1;
      if (lastused != start - 1) {
         for (int i = start; i <= lastused; i++) {
            //#debug
            printDataStruct("PowerCharColBuild#serializeRaw " + i + " \t = " + byteChars[i].length);
            System.arraycopy(byteChars[i], 0, data, offsetData, byteChars[i].length);
            offsetData += byteChars[i].length;
         }
      }
      methodEnds();
      return data;
   }

   /**
    * Reads the data part of the {@link ByteObjectManaged}, builds
    * <br>
    * Does nothing if data region is empty.
    */
   private void serializeRawReverse() {
      //we don't have to reload since
      dataLock();

      //#debug
      printDataStruct("#CharPowerColBuild#serializeRawReverse " + this.toStringInside());

      int soff = getDataOffsetStartLoaded();
      start = 0;

      //#debug
      printDataStruct("#CharPowerColBuild Rebuilding Starting Data at offset " + soff);

      //reads data header
      lastused = IntUtils.readIntBE(data, soff);
      soff += 4;
      //plane data is written unless several planes are used.
      int plane = data[soff];
      soff += 1;
      //#debug
      printDataStruct("#CharPowerColBuild Rebuilding " + (lastused + 1 - start) + " strings " + " start=" + start + " basePlane=" + plane);

      //heap space allocation that is risky. we cannot trust the serialization. could be a data attack.
      charData = pdc.getUCtx().getMem().createCharArrayDouble(lastused + 1);
      if (charData == null) {
         //#debug
         printDataStruct("#CharPowerColBuild Could not Create Char[][] of size " + (lastused + 1));
         charData = new char[0][];
         lastused = -1;
         //data corrupted. stop reverse serialization and inform bytecontroller.
         byteCon.dataCorrupted(this);
      } else {
         //continue reading the 
         int[] off = new int[1];
         for (int i = start; i <= lastused; i++) {
            charData[i] = StringUtils.getCharArrayPlane(data, soff, plane, off);
            soff += off[0];
         }

         //remove all data by asking
         removeData();
      }
      methodEnds();
   }

   /**
    * After it is called
    */
   public void serializeReverse() {
      //case where only the header, so we do a def init
      if (hasData()) {
         super.initEmptyConstructor();
         serializeRawReverse();
         buildIndex();
      } else {
         initEmptyConstructor();
      }

   }

   public void serializeReverseIndex() {
      if (hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_2_FAST_STRING_INDEX)) {
         if (this.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_4_INDEX_SERIALIZED)) {
            int indexRef = this.get2(CHARCOL_OFFSET_05_INDEX_REF2);
            indexString = (IPowerLinkStringToBytes) byteCon.getAgentFromRef(indexRef);
            if (indexString == null) {
               buildIndex();
            }
         } else {
            buildIndex();//rebuild index because it was not serialized
         }
      } else {
         indexString = null;
      }
   }

   /**
    */
   public ByteObjectManaged serializeTo(ByteController byteCon) {
      //first check if this object has already been serialized to the ByteController

      //if not serialize it.
      ByteObjectManaged bom = byteCon.serializeToUpdateAgentData(serializeRaw());
      if (indexString != null) {
         if (this.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_4_INDEX_SERIALIZED)) {
            //make sure the ref is written
            indexString.serializeTo(byteCon);
            bom.set2(CHARCOL_OFFSET_05_INDEX_REF2, indexString.getTech().getIDRef());
         }
      }
      return bom;
   }

   public void toString(Dctx dc) {
      //we modify the state. we cannot do that for debug purpose
      //methodStarts();
      toStringInside(dc);
      //methodEnds();
   }

   private String toStringInside() {
      Dctx d = new Dctx(pdc.getUCtx(), "\n\t");
      toStringInside(d);
      return d.toString();
   }

   public void toStringInside(Dctx sb) {
      sb.root(this, "PowerCharColBuild ");
      sb.nl();
      pdc.getTechFactory().toStringPowerCharColBuildTech(sb, this);
      sb.nl();
      sb.append("Size=" + insideGetSize());
      sb.append(" Start=" + start + " lastused=" + lastused);
      if (charData != null) {
         for (int i = start; i <= lastused; i++) {
            if (i % 10 == 0) {
               sb.nl();
            } else {
               sb.append(',');
            }
            if (charData[i] == null) {
               sb.append("null");
            } else {
               sb.append(charData[i]);
            }
         }
      } else {
         sb.nl();
         sb.append("charData is null");
      }
      sb.nl();
      sb.nlLvl("Index", indexString);
   }

}
