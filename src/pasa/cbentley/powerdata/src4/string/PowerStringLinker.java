package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechStringLinker;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public abstract class PowerStringLinker extends PowerBuildBase implements IPointerUser, ITechStringLinker {

   /**
    * Initialized during the constructor
    */
   protected IPowerCharCollector charco;

   protected int                 pointerOffset;

   public PowerStringLinker(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public IPowerEnum getEnumOnLinkStringToInt(int type, Object param) {
      return charco.getEnumOnCharCol(type, param);
   }

   /**
    * Maps/Normalize the {@link IPowerCharCollector} pointer to the 
    * @param pointer
    * @return
    */
   protected int getInsidePointer(int pointer) {
      return pointer - pointerOffset;
   }

   public int getIntFromKeyString(char[] c, int offset, int len) {
      int insidePointer = insideGetInsidePointerNoCreateEx(c, offset, len);
      return insideGetIntFromPointer(insidePointer);
   }

   public int getIntFromKeyString(String s) {
      return getIntFromKeyString(s.toCharArray(), 0, s.length());
   }

   public int getIntFromPointer(int outsidePointer) {
      int insidePointer = getInsidePointer(outsidePointer);
      return insideGetIntFromPointer(insidePointer);
   }

   public String getKeyStringFromPointer(int pointer) {
      return charco.getKeyStringFromPointer(pointer);
   }

   protected int getOutsidePointer(int insidePointer) {
      return insidePointer + pointerOffset;
   }

   public int getPointerFromKeyString(String str) {
      return charco.getPointer(str);
   }

   public int getSize() {
      return charco.getSize();
   }

   public Object getStringStructure() {
      return charco;
   }

   public boolean hasKeyString(String str) {
      return charco.hasChars(str);
   }

   public abstract int incrementIntFromKeyString(int incr, char[] cs, int offset, int len);

   public int incrementIntFromKeyString(int incr, String s) {
      return incrementIntFromKeyString(incr, s.toCharArray(), 0, s.length());
   }

   protected void initCharCo() {
      int charcoRef = this.get2(LINKER_OFFSET_03_REF_CHARCO2);
      charco = (IPowerCharCollector) byteCon.getAgentFromRefOrCreate(charcoRef, IPowerCharCollector.INT_ID);
      if (!charco.getTech().hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE)) {
         charco.addPointerUser(this);
      }
      pointerOffset = charco.getTech().get4(PS_OFFSET_04_START_POINTER4);
   }

   /**
    * Returns {@link IPowerCharCollector} pointer
    * @param c
    * @param offset
    * @param len
    * @return
    */
   protected int insideGetInsidePointerNoCreateEx(char[] c, int offset, int len) {
      int pointer = charco.find(c, offset, len);
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         throw new IllegalArgumentException(new String(c, offset, len));
      }
      return getInsidePointer(pointer);
   }

   protected int insideGetInsidePointerNoEx(char[] cs, int offset, int len) {
      int pointer = charco.find(cs, offset, len);
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         return pointer;
      }
      return getInsidePointer(pointer);
   }

   protected abstract int insideGetIntFromPointer(int insidePointer);

   protected int insideGetOrCreateInsidePointer(char[] cs, int offset, int len) {
      int pointer = charco.find(cs, offset, len);
      if (pointer == IPowerCharCollector.CHARS_NOT_FOUND) {
         pointer = charco.addChars(cs, offset, len);
         //adding a chars changed the pointers if unstable

      }
      return getInsidePointer(pointer);
   }

   //#mdebug
   
   public void toString(Dctx dc) {
      dc.root(this, "PowerStringLinker");
      dc.appendVarWithSpace("pointerOffset",pointerOffset);
      super.toString(dc.nLevel());
      dc.nlLvl(charco);
   }

   
   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerStringLinker");
   }
   //#enddebug
}
