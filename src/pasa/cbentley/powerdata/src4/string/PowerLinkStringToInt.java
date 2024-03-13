package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechStringLinker;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToInt;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Uses a {@link IPowerCharCollector} (Trie or otherwise) to link Strings to Integer.
 * <br>
 * <br>
 * In the very beginning this class functionality was inside the CharTrie. This was a bad design.
 * <br>
 * <br>
 * 
 * 
 * @author Charles-Philip
 *
 */
public class PowerLinkStringToInt extends PowerStringLinker implements IPointerUser, IPowerLinkStringToInt, ITechStringLinker {

   /**
    * Store the Integer datas
    */
   protected int[] ints;

   private int     nextEmpty;

   public PowerLinkStringToInt(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerLinkStringToIntRootTech());
   }

   /**
    * 
    */
   public PowerLinkStringToInt(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      setInitNoload();
   }

   /**
    * Enumeration on the Strings
    * @return
    */
   public IPowerEnum getEnumOnLinkStringToInt(final int type, final Object param) {
      if (type >= 0 && type <= ENUM_TYPE_3_INTSTRINGS) {
         return charco.getEnumOnCharCol(type, param);
      } else if (type == ENUM_TYPE_6_INT_DATE) {
         return new IPowerEnum() {

            int counter = 0;

            public boolean hasNext() {
               return counter + 1 < nextEmpty;
            }

            /**
             * TODO when removed.. 
             */
            public Object getNext() {
               Integer i = new Integer(ints[counter]);
               counter++;
               return i;
            }
         };
      } else {
         throw new IllegalArgumentException(type + "");
      }

   }

   protected void memoryClearSub() {
      ints = null;
      nextEmpty = 0;
   }

   /**
    * @throws ArrayIndexOutOfBoundsException when bad pointer
    */
   protected int insideGetIntFromPointer(int pointer) {
      return ints[pointer];
   }

   public Object getMorph(MorphParams p) {
      if (p.cl == IntToStrings.class) {
         IntToStrings itsc = (IntToStrings) charco.getMorph(p);
         for (int i = 0; i < itsc.nextempty; i++) {
            int pointer = itsc.ints[i];
            itsc.ints[i] = ints[getInsidePointer(pointer)];
         }
         return itsc;
      }
      return this;
   }

   /**
    * The method {@link PowerLinkStringToInt#updatePointers(Object, Object)}
    * will be called if {@link IPowerCharCollector}
    * is not stable
    * {@link ITechPointerStruct#PS_FLAG_1_STABLE}
    * @param cs
    * @param offset
    * @param len
    * @return IPowerCharCollector.CHARS_NOT_FOUND when char not there
    */
   private int insideGetStringPointer(char[] cs, int offset, int len) {
      int insidePointer = insideGetOrCreateInsidePointer(cs, offset, len);
      if (insidePointer >= ints.length) {
         ints = pdc.getUC().getMem().increaseCapacity(ints, 100);
      }
      if (insidePointer >= nextEmpty) {
         nextEmpty = insidePointer + 1;
      }
      return insidePointer;
   }

   /**
    * 
    */
   public int incrementIntFromKeyString(int incr, char[] cs, int offset, int len) {
      methodStartsWrite();
      try {

         int pointer = insideGetInsidePointerNoCreateEx(cs, offset, len);
         ints[pointer] += incr;
         return getOutsidePointer(pointer);
      } finally {
         methodEndsWrite();
      }
   }

   public int linkIntToKeyString(int data, char[] cs, int offset, int len) {
      methodStartsWrite();
      try {
         int insidePointer = insideGetStringPointer(cs, offset, len);
         ints[insidePointer] = data;
         return getOutsidePointer(insidePointer);
      } finally {
         methodEndsWrite();
      }
   }

   public int linkIntToKeyString(int data, String s) {
      return linkIntToKeyString(data, s.toCharArray(), 0, s.length());
   }

   public void pointerSwap(Object struct, int newPointer, int oldPointer) {
      int val = ints[getInsidePointer(oldPointer)];
      ints[getInsidePointer(oldPointer)] = ints[newPointer];
      ints[getInsidePointer(newPointer)] = val;
   }

   public void removeKeyString(String str) {
      methodStartsWrite();
      try {
         int pointer = charco.getPointer(str);
         if (pointer != IPowerCharCollector.CHARS_NOT_FOUND) {
            charco.remove(pointer, false);
            ints[getInsidePointer(pointer)] = Integer.MAX_VALUE;
         }
      } finally {
         methodEndsWrite();
      }
   }

   public byte[] serializeRaw() {
      //create the data array
      int dataSize = 4 + nextEmpty * 4;
      byte[] array = new byte[dataSize];
      IntUtils.writeIntBE(array, 0, nextEmpty);
      int offset = 4;
      for (int i = 0; i < nextEmpty; i++) {
         IntUtils.writeIntBE(array, offset, ints[i]);
         offset += 4;
      }
      return serializeRawHelper(array);
   }

   private void serializeRawReverse() {
      int offset = this.getDataOffsetStartLoaded();
      int len = this.get4(offset);
      ints = new int[len];
      for (int i = 0; i < len; i++) {
         offset += 4;
         ints[i] = this.get4(offset);
      }
      removeData();
   }

   public void serializeReverse() {
      if (hasData()) {
         initCharCo();
         serializeRawReverse();
      } else {
         //only init if there is no bytecontroller flag
         int initSize = this.get2(LINKER_OFFSET_02_SIZE2);
         ints = new int[initSize];
         initCharCo();
      }
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      byte[] data = serializeRaw();
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(data);
      charco.serializeTo(bc);
      byteCon.setRefFromTo(charco.getTech(), bom, LINKER_OFFSET_03_REF_CHARCO2);
      return bom;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerLinkStringToInt");
      toStringPrivate(dc);
      super.toString(dc.sup());

      for (int i = 0; i < nextEmpty; i++) {
         dc.nl();
         dc.append(i + " = " + ints[i]);
      }
      dc.nl();
      dc.append(charco.toString());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("nextEmpty", nextEmpty);
      dc.appendVarWithSpace("pointerOffset", pointerOffset);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerLinkStringToInt");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

   /**
    * 
    */
   public void updatePointers(Object struct, Object mapping) {
      // TODO Auto-generated method stub

   }

}
