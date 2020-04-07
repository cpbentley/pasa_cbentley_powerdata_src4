package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechStringLinker;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToIntArray;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Uses a {@link IPowerCharCollector} (Trie or otherwise) to link Strings to data items.
 * <br>
 * <br>
 * In the very beginning this class functionality was inside the CharTrie. This was a bad design.
 * <br>
 * <br>
 * 
 * @author Charles-Philip
 *
 */
public class PowerLinkStringToIntArray extends PowerStringLinker implements IPointerUser, IPowerLinkStringToIntArray, ITechStringLinker {



   private IPowerLinkIntToInts doubleArray;

   /**
    * 
    * @param mod
    */
   public PowerLinkStringToIntArray(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerLinkStringToIntArrayTechDefault());
   }

   public PowerLinkStringToIntArray(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmptyConstructorArray();
   }

   
   public void addIntToKeyString(int data, char[] chars, int offset, int len) {
      int insidePointer = insideGetOrCreateInsidePointer(chars, offset, len);
      doubleArray.addValueToKey(data, insidePointer);
   }

   
   public void addIntToKeyString(int data, String str) {
      this.addIntToKeyString(data, str.toCharArray(), 0, str.length());
   }

   public int getIntFromKeyString(char[] c, int offset, int len) {
      int insidePointer = insideGetInsidePointerNoCreateEx(c, offset, len);
      return doubleArray.getKeyValue(insidePointer, 0);
   }

   protected int insideGetIntFromPointer(int insidePointer) {
      return doubleArray.getKeyValue(insidePointer, 0);
   }

   
   public int[] getIntsFromKeyString(String str) {
      int insidePointer = insideGetInsidePointerNoCreateEx(str.toCharArray(), 0, str.length());
      return doubleArray.getKeyValues(insidePointer);
   }

   /**
    * 
    */
   public Object getMorph(MorphParams p) {
      if (p.tryInside) {
         //the morphing changes the pointers.. before pointer 123 would link to Bonjour. now pointer is 145 for Bonjour
         charco = (IPowerCharCollector) charco.getMorph(p);
         doubleArray = (IPowerLinkIntToInts) charco.getMorph(p);
         p.insideSuccess = true;
         return this;
      } else {
         ByteObjectManaged newTech = this.getTech();
         PowerLinkStringToIntArray sa = null;
         return sa;
      }
   }

   public int incrementIntFromKeyString(int incr, char[] c, int offset, int len) {
      int ip = insideGetInsidePointerNoCreateEx(c, offset, len);
      int[] vals = doubleArray.getKeyValues(ip);
      if (vals != null && vals.length > 0) {
         vals[0] += incr;
      }
      doubleArray.setValuesForKey(vals, ip);
      return getOutsidePointer(ip);
   }

   protected void initEmptyConstructorArray() {
      ByteController bc = getByteControllerCreateIfNull();
      int refCharCo = this.get2(LINKER_OFFSET_03_REF_CHARCO2);
      int refArray = this.get2(LINKER_OFFSET_04_REF_POINTERS2);
      doubleArray = (IPowerLinkIntToInts) bc.getAgentFromRefOrCreate(refArray, IPowerLinkIntToInts.INT_ID);
      charco = (IPowerCharCollector) bc.getAgentFromRefOrCreate(refCharCo, IPowerCharCollector.INT_ID);
      //when charcollector pointers are not stable,
      if (!charco.getTech().hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE)) {
         charco.addPointerUser(this);
      }
   }

   
   public boolean isIntLinkedToKeyString(int data, String str) {
      int key = insideGetInsidePointerNoCreateEx(str.toCharArray(), 0, str.length());
      int index = doubleArray.getIndexOfValueFromKey(data, key);
      if (index != -1) {
         return true;
      }
      return false;
   }

   public int linkIntToKeyString(int data, char[] cs, int offset, int len) {
      int key = insideGetOrCreateInsidePointer(cs, offset, len);
      doubleArray.setKeyData(key, data);
      return getOutsidePointer(key);
   }

   public int linkIntToKeyString(int data, String s) {
      return linkIntToKeyString(data, s.toCharArray(), 0, s.length());
   }

   /**
    * Swap the values from oldPointer and NewPointers
    */
   public void pointerSwap(Object struct, int newPointer, int oldPointer) {
      int[] oldVals = doubleArray.getKeyValues(oldPointer);
      doubleArray.removeKeyValues(oldPointer);
      int[] newVals = doubleArray.getKeyValues(newPointer);
      doubleArray.removeKeyValues(newPointer);
      doubleArray.addValuesToKey(oldVals, newPointer);
      doubleArray.addValuesToKey(newVals, oldPointer);
   }

   
   public void removeKeyString(String str) {
      int pointer = charco.getPointer(str);
      if (pointer != IPowerCharCollector.CHARS_NOT_FOUND) {
         charco.remove(pointer, false);
         doubleArray.removeKeyValues(getInsidePointer(pointer));
      }
   }

   public byte[] serializeRaw() {
      return getTech().toByteArray();
   }

   public void serializeReverse() {
      int indexRef = this.get2(LINKER_OFFSET_03_REF_CHARCO2);
      charco = (IPowerCharCollector) byteCon.getAgentFromRef(indexRef);
      indexRef = this.get2(LINKER_OFFSET_04_REF_POINTERS2);
      doubleArray = (IPowerLinkIntToInts) byteCon.getAgentFromRef(indexRef);
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      byte[] data = serializeRaw();
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(data);
      charco.serializeTo(bc);
      bc.setRefFromTo(charco.getTech(), bom, LINKER_OFFSET_03_REF_CHARCO2);
      doubleArray.serializeTo(bc);
      bc.setRefFromTo(doubleArray.getTech(), bom, LINKER_OFFSET_04_REF_POINTERS2);
      return bom;
   }

   /**
    * 
    */
   public void updatePointers(Object struct, Object mapping) {
      if (struct instanceof IPowerLinkIntToInt) {
         IPowerLinkIntToInt pl = (IPowerLinkIntToInt) struct;
         IPowerLinkIntToInts ne = (IPowerLinkIntToInts) doubleArray.getMorph(new MorphParams());

      }
   }

   
   public int[] getIntsFromPointer(int pointer) {
      return doubleArray.getKeyValues(pointer);
   }
   //#mdebug
   
   public void toString(Dctx dc) {
      dc.root(this, "PowerLinkStringToIntArray");
      pdc.getTechFactory().toStringPowerStringLinkerTech(dc.nLevel(), this);
      super.toString(dc.nLevel());
      doubleArray.toString(dc.nLevel());
   }

   
   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerLinkStringToIntArray");
   }
   //#enddebug
}
