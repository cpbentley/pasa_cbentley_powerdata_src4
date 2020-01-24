package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.CharUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkOrderedIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColRun;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Primary aim of this class is to retrieve a string with an integer. It is optimized for a small memory footprint and fast reads.
 * <br>
 * <br>
 * What are his {@link IPowerCharCollector} genes?
 * <br>
 * <br>
 * 
 * {@link MemAgent} collecting char for {@link IPowerCharCollector}.
 * <br>
 * <br>
 * Difference with RunEx?
 * <br>
 * For use of add methods, it is recommended to first add all the alphabet letters before using the class.
 * The class must make costly reorganizing work when adding new letters
 * <br><br>
 * Index may be later rebuilt but IDs will change
 * <br><br>
 * ID => look up in ordered ID header for the length table.<br>
 * for example a sequence of 0-13-34-56-78-567-1245 numbers <br>
 * This gives a chunk #. ID 57 gives chunk #4 and its position P4
 * <br>
 * at position P4, we have the Chunk #4 header. It gives us the length of the word 
 * and the ordered ID header for the first letter.
 * Sequence of 56-59-63-76<br>
 * We now know the part#1  with the relative position to P4 for the 
 * Once you have that
 * read the letter and the correct part for the rest of the word<br>
 * <br>
 * Are words ordered? within a [length, first letter] chunk
 * <br>
 * <br>
 * [index offset] [seq offset]<br>
 * <br>
 * (header)<br>
 * [plane 1 byte]<br>
 * [numLengthBits 1 byte]<br>
 * [numLetterbits 1 byte]<br>
 * [max length 2 bytes]<br>
 * [numWords 3 bytes]<br>
 * [data offset 4 bytes]<br>
 * [start letters IntOByteSequence]<br>
 * [all letters IntOByteSequence]
 * <br>
 * <br>
 * [1 char big length index value] [ (1st letter index value) (2nd letter index value) ... ]<br>
 * [2 char big length index value] [ (1st letter index value) (2nd letter index value) ... ]<br>
 * <br>
 * (comes the char data packed together<br>
 * [data] [data] ... <br>
 * <br>
 * @author Charles Bentley
 *
 */
public class CharCollectorRunLight extends PowerCharCol implements IPowerCharCollector, ITechCharColRun {

   /**
    * the bit size of one letter in the data chunk
    */
   private int alphaBitSize;

   /**
    * loaded
    */
   private int                      dataOffSet;

   /**
    * loaded Offset in byte array
    */
   private int                      headerTableOffset;

   /**
    * Tracks the letters used.
    * <br>
    * <br>
    * When adding a word, each of its letters is checked to belong to this.
    * <br>
    * How is the array expansion dealt with? A {@link ByteController} is created if null
    * for methods that may require expansion. Because
    */
   public IPowerLinkOrderedIntToInt lettersAll;

   /**
    * Tracks the first letters.
    * <br>
    * <br>
    * 
    */
   public IPowerLinkOrderedIntToInt lettersFirst;

   /**
    * The bit size of a row of numIndex for any length
    */
   private int                      nonLengthbitSize;

   private int                      plane;

   private ByteObject               tech;

   /**
    * Empty collector.
    */
   public CharCollectorRunLight(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getCharCollectorRunLightTechDefault());
   }

   public CharCollectorRunLight(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   /**
    * Adding a char creates a build v the chars and return a handle
    * search if already here
    */
   public int addChars(char[] c, int offset, int len) {
      //first add first letters and normal letters
      for (int i = 0; i < len; i++) {
         if (i == 0) {
            if (lettersFirst.hasValue(c[0] & 0xFF)) {
               lettersFirst.addValue(c[0] & 0xFF);
               //we also have to create a new column

               //and update all value bigger or equal to position of new letter
            }
         }
         if (lettersAll.hasValue(c[i + offset] & 0xFF)) {
            lettersAll.addValue(c[i + offset] & 0xFF);

            //and update all value bigger or equal to position of new letter

         }
      }
      //get max len
      if (len > getMaxLength()) {
         int diff = len - getMaxLength();
         //increase
         super.expandDataArray(offset, len);
      }
      return 0;
   }

   /**
    * @see#CharCollectorRunLight setChars
    */
   public int addChars(char[] c, int offset, int len, int pointer) {
      return setChars(pointer, c, offset, len);
   }

   public int addChars(String s) {
      return addChars(s.toCharArray(), 0, s.length());
   }

   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub

   }

   /**
    * Find the {@link IPowerLinkOrderedIntToInt} for this {@link PowerCharColRun}.
    * <br>
    * <br>
    * Implementation is unknown. Use Factory class with genetics code. Where do we get the genes?
    * Implementation details of {@link PowerCharColRun}.
    * <br>
    * <br>
    * Is the serialization always the same?
    */
   private void aInitCCC() {
      dataOffSet = get2(CHAR_RUN_OFFSET_08_DATA_OFFSET2);
      //create the start letter struct

      ByteObjectManaged tech = this.getTechSub(0);
      ByteController bc = getByteControllerCreateIfNull();
      int lfirstRef = get2(CHAR_RUN_OFFSET_05_LETTER_FIRST2);
      int lallRef = get2(CHAR_RUN_OFFSET_06_LETTER_ALL2);

      //good enough for read only uses.
      //no use of byte controller. data is inside the header. but what happens during data expansion?
      lettersFirst = (IPowerLinkOrderedIntToInt) bc.getAgentFromRefOrCreate(lfirstRef, tech, IPowerLinkOrderedIntToInt.INT_ID);
      //now the all letters struct
      lettersAll = (IPowerLinkOrderedIntToInt) bc.getAgentFromRefOrCreate(lallRef, tech, IPowerLinkOrderedIntToInt.INT_ID);
      //the table header
      headerTableOffset = index + get2(CHAR_RUN_OFFSET_07_LENAZTABLE2);

      nonLengthbitSize = lettersFirst.getSize() * getLetterNumBits();
      alphaBitSize = BitUtils.widthInBits(lettersAll.getSize());
      plane = get1(CHAR_RUN_OFFSET_00_PLANE1);
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   /**
    * Copy the chars at pointer in the array starting at offset
    * return the number of chars copied.
    * <br>
    * <br>
    * 
    */
   public int copyChars(int pointer, char[] cars, int offset) {
      int len = getLen(pointer);
      if (len == -1)
         return 0;
      int[] vars = new int[3];
      getPositionVariables(len, vars);
      int numIndex = vars[0];
      BitCoordinate c = createBitCoordinate();
      c.map(vars[1] + getLengthNumBits());
      int alphaStartSize = lettersFirst.getSize();
      int firstLetterPosition = 0;
      int passed = 0;
      for (int i = 1; i <= alphaStartSize; i++) {
         firstLetterPosition++;
         int num = BitUtils.readBits(this.data, c, getLetterNumBits());
         numIndex += num;
         if (pointer <= numIndex) {
            int forw = alphaBitSize * (len - 1) * (num - (numIndex - pointer) - 1);
            forw += alphaBitSize * (len - 1) * passed;
            c.map((dataOffSet * 8) + vars[2] + forw);
            break;
         }
         passed += num;
      }
      copyMyChars(cars, offset, len, c, firstLetterPosition);
      return len;
   }

   private void copyMyChars(char[] cars, int offset, int len, BitCoordinate c, int firstLetterPosition) {
      cars[offset] = CharUtils.buildCharFromLowByte(lettersFirst.getValueFromPosition(firstLetterPosition), this.data[index + CHAR_RUN_OFFSET_00_PLANE1]);
      for (int i = 1; i < len; i++) {
         int position = BitUtils.readBits(this.data, c, alphaBitSize);
         int value = lettersAll.getValueFromPosition(position);
         //System.out.println("value="+value + " from position:"+position);
         cars[offset + i] = CharUtils.buildCharFromLowByte(value, plane);
      }
   }

   /**
    * @return  if not found
    */
   public int find(char[] str, int offset, int len) {
      if (len > getMaxLength()) {
         return -1;
      }
      //get the length of our word
      int[] vars = new int[3];
      getPositionVariables(len, vars);
      BitCoordinate c = createBitCoordinate();
      c.map(vars[1]);
      int firstLetterValue = str[offset] & 0xFF;
      int firstLetterPosition = lettersFirst.getValueOrderCount(firstLetterValue);
      if (firstLetterPosition == 0)
         return -1;
      //get small_index value
      int smallIndexBits = getLetterNumBits();
      //read the number of words of length starting with ith letter
      int num = BitUtils.readBits(this.data, c, getLengthNumBits());
      //compute number of words below that letter that have the same length
      int numWordsOfLengthLenBelowLetter = 0;
      for (int i = 1; i < firstLetterPosition; i++) {
         numWordsOfLengthLenBelowLetter += BitUtils.readBits(this.data, c, smallIndexBits);
      }
      int pointer = vars[0] + numWordsOfLengthLenBelowLetter;
      int forw = (alphaBitSize * numWordsOfLengthLenBelowLetter * (len - 1));
      c.map((dataOffSet * 8) + vars[2] + forw);

      //iterate over the number of words starting with that letter
      for (int j = 0; j < num; j++) {
         char[] cr = getMyChars(len, c, firstLetterPosition);
         boolean found = true;
         for (int i = 0; i < cr.length; i++) {
            if (str[offset + i] != cr[i]) {
               found = false;
               break;
            }
         }
         pointer++;
         if (found)
            return pointer;
      }
      return IPowerCharCollector.CHARS_NOT_FOUND;
   }

   public int getBiggestWordSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * 
    * return ' '
    */
   public char getChar(int pointer) {
      int len = getLen(pointer);
      if (len == -1)
         return ' ';
      int[] vars = new int[3];
      getPositionVariables(len, vars);
      int numIndex = vars[0];
      BitCoordinate c = createBitCoordinate();
      c.map(vars[1] + getLengthNumBits());
      int alphaStartSize = lettersFirst.getSize();
      int firstLetterPosition = 0;
      for (int i = 1; i <= alphaStartSize; i++) {
         firstLetterPosition++;
         numIndex += BitUtils.readBits(this.data, c, getLetterNumBits());
         if (pointer <= numIndex) {
            break;
         }
      }
      return CharUtils.buildCharFromLowByte(lettersFirst.getValueFromPosition(firstLetterPosition), this.data[index + CHAR_RUN_OFFSET_00_PLANE1]);
   }

   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getCharEnum(Object param) {
      return null;
   }

   /**
    * From the header, get the length chunk for pointer
    * From the byte array of given length, learn the first letter x
    * From last num of previous length chunk, is numa + lastnum len-1 > pointer
    * if it is smaller, word starts with an a. else go compare with next letter
    * (from last num of previous le
    * [maxlen] [[len1offset [start num] ... len30offset[num] [[numa]...[numz] [...]]
    * for one value with pointer: read len num offset, then
    * 
    * @return
    */
   public char[] getChars(int pointer) {
      if (pointer <= 0)
         return "".toCharArray();
      int len = getLen(pointer);
      if (len == -1)
         return "".toCharArray();
      int[] vars = new int[3];
      getPositionVariables(len, vars);
      int numIndex = vars[0];
      BitCoordinate c = createBitCoordinate();
      c.map(vars[1] + getLengthNumBits());
      int alphaStartSize = lettersFirst.getSize();
      int firstLetterPosition = 0;
      int passed = 0;
      for (int i = 1; i <= alphaStartSize; i++) {
         firstLetterPosition++;
         int num = BitUtils.readBits(this.data, c, getLetterNumBits());
         numIndex += num;
         if (pointer <= numIndex) {
            int forw = alphaBitSize * (len - 1) * (num - (numIndex - pointer) - 1);
            forw += alphaBitSize * (len - 1) * passed;
            c.map((dataOffSet * 8) + vars[2] + forw);
            break;
         }
         passed += num;
      }
      return getMyChars(len, c, firstLetterPosition);
   }

   /**
    * Return the concatenated 0,1,2,3 chars
    * @param pointers
    * @return
    */
   public char[] getChars(int[] charp) {
      int size = 0;
      for (int i = 0; i < charp.length; i++) {
         size += getLen(charp[i]);
      }
      char[] val = new char[size];
      int off = 0;
      for (int i = 0; i < charp.length; i++) {
         off += copyChars(charp[i], val, off);
      }
      return val;
   }

   public String getKeyStringFromPointer(int pointer) {
      return new String(this.getChars(pointer));
   }

   /**
    */
   public int getLen(int pointer) {
      if (pointer <= 0)
         return -1;
      int sum = 0;
      int maxl = getMaxLength();
      for (int i = 1; i <= maxl; i++) {
         sum += getLengthNumIndex(i);
         if (pointer <= sum) {
            return i;
         }
      }
      return -1;
   }

   /**
    * get the coordinate for reading the length index
    * @param length
    * @param c
    */
   private void getLengthHeaderPosition(int length, BitCoordinate c) {
      int bits = (headerTableOffset) * 8 + ((length - 1) * (getLengthNumBits() + nonLengthbitSize));
      c.map(bits);
   }

   private int getLengthNumBits() {
      return this.data[index + CHAR_RUN_OFFSET_01_LENGTH_BIT1];
   }

   /**
    * 
    * @param length
    * @return
    */
   public int getLengthNumIndex(int length) {
      BitCoordinate c = createBitCoordinate();
      getLengthHeaderPosition(length, c);
      return BitUtils.readBits(this.data, c, getLengthNumBits());
   }

   private int getLetterNumBits() {
      return get1(CHAR_RUN_OFFSET_02_LETTER_BIT1);
   }

   public int getLoadedDataBitOffset() {
      return index * 8 + get2(CHAR_RUN_OFFSET_08_DATA_OFFSET2);
   }

   public int getMaxLength() {
      return get2(CHAR_RUN_OFFSET_03_MAX_LENGTH2);
   }

   public Object getMorph(MorphParams p) {
      //we need to use the same tech that match the transfer. this class is no double.
      //we don't need an index at least during the build. Final user will check meta data and
      //ask for an index if he wants one.
      PowerCharColBuild cpc = new PowerCharColBuild(pdc);
      int pointerFirst = 0;
      int numWords = get3(CHAR_RUN_OFFSET_04_NUM_WORD3);
      for (int i = pointerFirst; i < pointerFirst + numWords; i++) {
         char[] cs = getChars(i);
         cpc.addChars(cs, 0, cs.length);
      }
      return cpc;
   }

   /**
    * Creates a char[] array
    * <br>
    * <br>
    * @param len
    * @param c
    * @param firstLetterPosition
    * @return
    */
   private char[] getMyChars(int len, BitCoordinate c, int firstLetterPosition) {
      char[] ca = new char[len];
      copyMyChars(ca, 0, len, c, firstLetterPosition);
      return ca;
   }

   public int[] getNewPointers() {
      return null;
   }

   public int getPointer(char[] chars) {
      return find(chars, 0, chars.length);
   }

   public int getPointer(char[] chars, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getPointer(String str) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * 
    * [0] = numBelow (number of words) with a length < len in parameter (same as getNumBelow function)
    * [1] = loaded bitPosition of length header for the len value in parameter
    * [2] = bits consumed by the words below that length
    * @param length
    * @param vars
    */
   private void getPositionVariables(int length, int[] vars) {
      int val = 0;
      int bitsConsumed = 0;
      for (int i = 1; i < length; i++) {
         int numIndex = getLengthNumIndex(i);
         bitsConsumed += ((i - 1) * alphaBitSize * numIndex);
         val += numIndex;

      }
      vars[0] = val;
      vars[1] = (headerTableOffset) * 8 + ((length - 1) * (getLengthNumBits() + nonLengthbitSize));
      vars[2] = bitsConsumed;
      //System.out.println(bitsConsumed + " for len =" + length);
   }

   /**
    * 
    */
   public int getSize() {
      return IntUtils.readInt24BE(this.data, index + CHAR_RUN_OFFSET_04_NUM_WORD3);
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public boolean hasChars(char[] chars, int offset, int len) {
      return IPowerCharCollector.CHARS_NOT_FOUND != find(chars, offset, len);
   }

   public boolean hasChars(String str) {
      // TODO Auto-generated method stub
      return false;
   }

   protected int insideAddChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected void insideAppendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   protected int insideCopyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideFind(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected char insideGetChar(int outsidePointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected char[] insideGetChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   protected char[] insideGetChars(int[] charp) {
      // TODO Auto-generated method stub
      return null;
   }

   protected String insideGetKeyStringFromPointer(int pointer) {
      return new String(this.insideGetChars(pointer));
   }

   protected int insideGetLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideGetPointer(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideGetSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int[][] insideGetSizes() {
      // TODO Auto-generated method stub
      return null;
   }

   protected boolean insideHasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
   }

   protected int insideRemove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   protected int insideSetChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * <br>
    * <br>
    * 
    */
   public boolean isValid(int pointer) {
      return false;
   }

   /**
    * 
    */
   public int remove(int pointer, boolean useForce) {
      //TODO implement it
      return 0;
   }

   public void search(CharSearchSession css) {
      // TODO Auto-generated method stub

   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   /**
    * Special Add trying to reuse the slot pointer
    */
   public int setChars(int pointer, char[] d, int offset, int len) {
      return 0;
   }


   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "CharCollectorRunLight");
      toStringPrivate(dc);
      super.toString(dc.sup());
      
   }

   private void toStringPrivate(Dctx sb) {
      sb.append("\n Struct Size:" + getLength() + " bytes. Header Size:" + (dataOffSet - index) + " ");
      sb.append("DataOffset:" + dataOffSet + " Offset:" + index);
      sb.append(" MaxLength:" + getMaxLength() + " NumWord:" + getSize() + " Plane:" + this.data[index + CHAR_RUN_OFFSET_00_PLANE1]);
      sb.append("\n First Letters:\t");
      int[] vals = lettersFirst.getValues();
      for (int i = 0; i < vals.length; i++) {
         sb.append(CharUtils.buildCharFromLowByte(vals[i], this.data[index + CHAR_RUN_OFFSET_00_PLANE1]));
         sb.append(',');
      }
      sb.append("\n All Letters:\t");
      vals = lettersAll.getValues();
      for (int i = 0; i < vals.length; i++) {
         sb.append(CharUtils.buildCharFromLowByte(vals[i], this.data[index + CHAR_RUN_OFFSET_00_PLANE1]));
         sb.append(',');
      }
      //now prints the header table
      BitCoordinate c = new BitCoordinate(pdc.getUCtx(), headerTableOffset, 0);
      int alphaStartSize = lettersFirst.getSize();
      int alphaBitSize = BitUtils.widthInBits(lettersAll.getSize());
      sb.append("\n Alpha Start Bitsize:" + alphaStartSize + "\t Full Alpha Bitsize :" + alphaBitSize);
      sb.append("\t lengthNumBits:" + getLengthNumBits() + "\t letterNumBits :" + getLetterNumBits());
      for (int i = 1; i <= getMaxLength(); i++) {
         int num = BitUtils.readBits(this.data, c, getLengthNumBits());
         sb.append("\n Length " + i + "\t Num=" + num);
         int firstLettersNum = lettersFirst.getSize();
         for (int j = 1; j <= firstLettersNum; j++) {
            num = BitUtils.readBits(this.data, c, getLetterNumBits());
            //c.forward(wordPointerBits);
            char mc = CharUtils.buildCharFromLowByte(lettersFirst.getValueFromPosition(j), this.data[index + CHAR_RUN_OFFSET_00_PLANE1]);
            sb.append("\t position " + j + "(" + mc + ")" + " = " + num);
         }
         //c.forward(skipBitsSize);
      }
      sb.nl();
      for (int i = 1; i <= getSize(); i++) {
         sb.append(new String(getChars(i)) + ",");
         if (i % 10 == 0)
            sb.nl();
      }
      sb.nl();
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "CharCollectorRunLight");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
