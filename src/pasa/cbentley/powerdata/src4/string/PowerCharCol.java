package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.guicontrols.IPrefixSearchSession;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchChar;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToBytes;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Pointer stability can be created in Run configs by using an int[] array
 * to store changes in pointers.
 * <br>
 * @author Charles Bentley
 *
 */
public abstract class PowerCharCol extends PowerBuildBase implements ITechCharCol, IPowerCharCollector {

   /**
    * Maps a String to its position in this structure.
    * <br>
    * <br>
    * 
    */
   protected IPowerLinkStringToBytes indexString;

   /**
    * What the world sees as the first valid pointer.
    * Can be 
    * <li>-1
    * <li>0
    * <li>1
    * {@link ITechCharCol#PS_OFFSET_04_START_POINTER4}
    * 
    * Offset to all API functions
    */
   protected int                     startPointer;

   public PowerCharCol(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public int getBiggestWordSize() {
      return get1(CHARCOL_OFFSET_04_BIGGEST_WORD_SIZE1);
   }

   /**
    * Add string and return an ID to retrieve that string
    * if allowDuplicate field is true, method does not look for strings. it just
    * adds it.
    * <br>
    * <br>
    * The char data is fully copy. The reference array is not used.
    * <br>
    * <br>
    * API
    */
   public int addChars(char[] c, int offset, int len) {
      methodStarts();
      try {
         int val = insideAddChars(c, offset, len);
         return getOutsidePointer(val);
      } finally {
         methodEnds();
      }
   }

   protected void initEmptyConstructor() {
      startPointer = this.get4(PS_OFFSET_04_START_POINTER4);
   }

   /**
    * No DCR
    */
   protected int insideGetSize() {
      return get4(PS_OFFSET_06_NUM_POINTER4);
   }

   public int addChars(String s) {
      return addChars(s.toCharArray(), 0, s.length());
   }

   public void appendChars(int pointer, StringBBuilder sb) {
      methodStarts();
      try {
         insideAppendChars(getInsidePointer(pointer), sb);
      } finally {
         methodEnds();
      }
   }

   /**
    * Valid for contiguous pointer structures
    * <br>
    * What happens if the structure is concurrently modified?
    * <br>
    * <br>
    * 
    * @return
    */
   public IPowerEnum getEnumOnCharCol(final int type, final Object param) {
      return new IPowerEnum() {

         int counter   = startPointer;

         int lastCount = startPointer + insideGetSize();

         public Object getNext() {
            int insidePointer = getInsidePointer(counter);
            char[] c = insideGetChars(insidePointer);

            counter++;
            switch (type) {
               case 0:
                  return new String(c);
               case ENUM_TYPE_1_POINTER:
                  return new Integer(counter - 1);
               case ENUM_TYPE_2_CHARS:
                  return c;
               case ENUM_TYPE_3_INTSTRINGS: {
                  IntToStrings its = (IntToStrings) param;
                  its.strings[0] = new String(c);
                  its.ints[0] = counter;
                  return its;
               }
               default:
                  throw new IllegalArgumentException();
            }
         }

         public boolean hasNext() {
            return counter < lastCount;
         }
      };
   }

   /**
    * Does not need ByteController since it builds from scratch from Tech Parameters of Collector.
    */
   public void buildIndex() {
      if (this.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_2_FAST_STRING_INDEX)) {
         int indexRef = this.get2(CHARCOL_OFFSET_05_INDEX_REF2);
         indexString = (IPowerLinkStringToBytes) byteCon.getAgentFromRefOrCreate(indexRef, IPowerLinkStringToBytes.INT_ID);
         //index was created. if data, data is loaded it is empty if.
         //enumerate on the pointers
         IPowerEnum pe = this.getEnumOnCharCol(IPowerCharCollector.ENUM_TYPE_1_POINTER, null);
         while (pe.hasNext()) {
            Integer outsidePointer = (Integer) pe.getNext();
            int p = outsidePointer.intValue();
            String str = getKeyStringFromPointer(p);
            indexString.insertWord(str.toCharArray(), 0, str.length(), p);
         }
      } else {
         //don't use index. make sure it is null.
         indexString = null;
      }
   }

   public int copyChars(int pointer, char[] c, int offset) {
      methodStarts();
      try {
         return insideCopyChars(getInsidePointer(pointer), c, offset);
      } finally {
         methodEnds();
      }
   }

   /**
    * Careful is accent ironing.
    * <br>
    * <br>
    * <br>
    * 
    * 
    * @return 
    */
   public int find(char[] str, int offset, int len) {
      methodStarts();
      try {
         int value = insideFind(str, offset, len);
         return getOutsidePointer(value);
      } finally {
         methodEnds();
      }
   }

   /**
    * 
    */
   public char getChar(int pointer) {
      //argument validation
      methodStarts();
      try {
         return insideGetChar(getInsidePointer(pointer));
      } finally {
         methodEnds();
      }
   }

   public String getKeyStringFromPointer(int pointer) {
      methodStarts();
      try {
         return insideGetKeyStringFromPointer(getInsidePointer(pointer));
      } finally {
         methodEnds();
      }
   }

   protected abstract String insideGetKeyStringFromPointer(int pointer);

   /**
    * 
    */
   public char[] getChars(int pointer) {
      methodStarts();
      try {
         return insideGetChars(getInsidePointer(pointer));
      } finally {
         methodEnds();
      }
   }

   /**
    * 
    */
   public char[] getChars(int[] charp) {
      return insideGetChars(charp);
   }

   public IPowerLinkStringToBytes getIndex() {
      return indexString;
   }

   protected int getInsidePointer(int outsidePointer) {
      return outsidePointer - startPointer;
   }

   /**
    * 
    * Opens a Search of type {@link ITechCharCol#SEARCH_1_INDEXOF}
    * <br>
    * Convenience API method.
    * @param frame size of results per search. 0 for everything
    * @return
    */
   public ISearchSession searchIndexOf(int frame) {
      ByteObject tech = pdc.getTechFactory().getCharSearchSessionTechDefault();
      tech.set2(ITechSearchChar.SEARCH_CHAR_OFFSET_02_FRAME_SIZE2, frame);
      tech.set2(ITechSearchChar.SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1, ITechCharCol.SEARCH_1_INDEXOF);
      return search(tech);
   }

   /**
    * Opens a Search of type {@link ITechCharCol#SEARCH_0_PREFIX}
    * <br>
    * Convenience API method.
    * <br>
    * Some {@link IPowerCharCollector} will be optimized for fast prefix searches.
    * <br>
    * @param frame size of results per search. 0 for everything
    * @return
    */
   public IPrefixSearchSession searchPrefix(int frame) {
      ByteObject tech = pdc.getTechFactory().getCharSearchSessionTechDefault();
      tech.set2(ITechSearchChar.SEARCH_CHAR_OFFSET_02_FRAME_SIZE2, frame);
      tech.set2(ITechSearchChar.SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1, ITechCharCol.SEARCH_0_PREFIX);
      CharSearchSession tss = new CharSearchSession(pdc, this, tech);
      return tss;
   }

   /**
    * Generic Open Search
    * @param param {@link ITechSearchChar}
    * @return
    */
   public ISearchSession search(ByteObject param) {
      CharSearchSession tss = new CharSearchSession(pdc, this, param);
      return tss;
   }

   /**
    * API method
    */
   public int getLen(int pointer) {
      methodStarts();
      int val = insideGetLen(getInsidePointer(pointer));
      methodEnds();
      return val;
   }

   /**
    * outside = inside + start 
    * @param insidePointer
    * @return
    */
   protected int getOutsidePointer(int insidePointer) {
      if (insidePointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         return IPowerCharCollector.CHARS_NOT_FOUND;
      }
      return insidePointer + startPointer;
   }

   public int getPointer(char[] chars) {
      return getPointer(chars, 0, chars.length);
   }

   public int getPointer(char[] chars, int offset, int len) {
      methodStarts();
      try {
         int value = insideGetPointer(chars, offset, len);
         return getOutsidePointer(value);
      } finally {
         methodEnds();
      }
   }

   public int getPointer(String str) {
      return getPointer(str.toCharArray());
   }

   public int getSize() {
      methodStarts();
      try {
         return insideGetSize();
      } finally {
         methodEnds();
      }
   }

   public int[][] getSizes() {
      methodStarts();
      try {
         return insideGetSizes();
      } finally {
         methodEnds();
      }
   }

   public boolean hasChars(char[] c, int offset, int len) {
      methodStarts();
      try {
         return insideHasChars(c, offset, len);
      } finally {
         methodEnds();
      }
   }

   public boolean hasChars(String s) {
      return hasChars(s.toCharArray(), 0, s.length());
   }

   protected abstract int insideAddChars(char[] c, int offset, int len);

   protected abstract void insideAppendChars(int pointer, StringBBuilder sb);

   protected abstract int insideCopyChars(int pointer, char[] c, int offset);

   protected abstract int insideFind(char[] c, int offset, int len);

   protected abstract char insideGetChar(int outsidePointer);

   protected abstract char[] insideGetChars(int pointer);

   protected char[] insideGetChars(int[] charp) {
      return CharColUtilz.get(this, charp);
   }

   protected abstract int insideGetLen(int pointer);

   protected abstract int insideGetPointer(char[] c, int offset, int len);

   protected abstract int[][] insideGetSizes();

   protected abstract boolean insideHasChars(char[] c, int offset, int len);

   protected abstract int insideRemove(int pointer, boolean useForce);

   protected abstract int insideSetChars(int pointer, char[] d, int offset, int len);

   public int remove(int pointer, boolean useForce) {
      methodStarts();
      try {
         int v = insideRemove(getInsidePointer(pointer), useForce);
         return getOutsidePointer(v);
      } finally {
         methodEnds();
      }
   }

   /**
    * No lock
    */
   public int setChars(int pointer, char[] d, int offset, int len) {
      methodStartsWrite();
      try {
         return insideSetChars(getInsidePointer(pointer), d, offset, len);
      } finally {
         methodEndsWrite();
      }
   }

   //#mdebug

   public void toString(Dctx dc) {
      dc.root(this, "PowerCharCol");
      super.toString(dc.nLevel());
   }

   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerCharCol");
   }
   //#enddebug
}
