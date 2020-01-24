package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.powerdata.spec.src4.guicontrols.ISearchSession;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharAggregate;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchChar;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Types can be enabled or disable.
 * 
 * Pointers are contextually valid only to the caller.
 * <br>
 * When the number of {@link IPowerCharCollector} is fixed.
 * 
 * But each {@link IPowerCharCollector} can be actived and desactivated
 * {@link PowerCharColAggregate#activateTrue(int)}
 * {@link PowerCharColAggregate#activateFalse(int)}
 * <br>
 * Deactivated {@link IPowerCharCollector} are ignored in all requests.
 * Word addresses are stored on an integer.
 * This class
 * @author Charles Bentley
 *
 */
public class PowerCharColAggregate extends PowerCharCol implements IPowerCharCollector, ITechCharAggregate {


   /**
    * Types used in the 
    */
   protected int[]                 acceptedTypes;

   int                             nextIndex = 0;

   /**
    * 
    */
   protected String[]              typeNames;

   /**
    * New Types can be added if the flag dynamic is set.
    * 
    */
   protected IPowerCharCollector[] types     = new IPowerCharCollector[0];

   public PowerCharColAggregate(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public PowerCharColAggregate(PDCtx pdc, ByteObjectManaged tech, IPowerCharCollector[] cols) {
      super(pdc, pdc.getTechFactory().getPowerCharColAggregateTechRoot());
      this.types = cols;
   }

   public PowerCharColAggregate(PDCtx pdc, IPowerCharCollector[] cols) {
      super(pdc, pdc.getTechFactory().getPowerCharColAggregateTechRoot());
      this.types = cols;
   }

   public void activateFalse(int index) {

   }

   public void activateTrue(int index) {

   }

   public void addCharCol(IPowerCharCollector cc) {

   }

   public int addChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int addChars(String s) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * 
    * @param cc
    * @return
    */
   public int addNewType(IPowerCharCollector cc) {
      if (hasFlag(CHAR_AGGRE_OFFSET_01_FLAG1, CHAR_AGGRE_FLAG_1_DYNAMIC)) {
         IPowerCharCollector[] old = types;
         types = new IPowerCharCollector[old.length + 1];
         for (int i = 0; i < old.length; i++) {
            types[i] = old[i];
         }
         types[old.length] = cc;
         return old.length;
      } else {
         throw new IllegalStateException();
      }
   }

   public void addPointerUser(IPointerUser pointerUser) {
      // TODO Auto-generated method stub

   }

   public void appendChars(int pointer, StringBBuilder sb) {
      // TODO Auto-generated method stub

   }

   public int copyChars(int pointer, char[] c, int offset) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int find(char[] str, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getBiggestWordSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public char getChar(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getCharEnum(Object param) {
      return null;
   }

   public char[] getChars(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public char[] getChars(int[] pointers) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * TODO our own enumeration since we are an aggregate.
    */
   public IPowerEnum getEnumOnCharCol(final int type, final Object param) {
      return null;
   }

   public String getKeyStringFromPointer(int pointer) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getLen(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getPointer(char[] chars, int offset, int len) {
      for (int i = 0; i < types.length; i++) {
         if (types[i] != null) {
            if (acceptedTypes[i] != 1) {
               int pointer = types[i].getPointer(chars, offset, len);
               if (pointer != CHARS_NOT_FOUND) {

               }
            }
         }
      }
      return CHARS_NOT_FOUND;
   }

   public int getPointer(String str) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public boolean hasChars(char[] c, int offset, int len) {
      // TODO Auto-generated method stub
      return false;
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

   public boolean isValid(int pointer) {
      // TODO Auto-generated method stub
      return false;
   }

   public int remove(int pointer, boolean useForce) {
      // TODO Auto-generated method stub
      return 0;
   }

   /**
    * Generic Open Search
    * @param param {@link ITechSearchChar}
    * @return
    */
   public ISearchSession search(ByteObject param) {
      CharAggregateSearch tss = new CharAggregateSearch(pdc, this, param);
      return tss;
   }

   /**
    * Like all aggregates, THere is the order of search and the pattern of search results.
    * number of results deep first with a given order
    * @param cas
    */
   public void search(CharAggregateSearch cas) {

   }

   public byte[] serializePack() {
      // TODO Auto-generated method stub
      return null;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      // TODO Auto-generated method stub
      return null;
   }

   public int setChars(int pointer, char[] d, int offset, int len) {
      // TODO Auto-generated method stub
      return 0;
   }

   public String toString(String nl) {
      // TODO Auto-generated method stub
      return null;
   }

}
