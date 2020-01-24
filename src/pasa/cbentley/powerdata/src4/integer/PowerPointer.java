package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Defines a mapping for Integer keys with a continuous interval from pointerStart to pointerLast exclusive.
 * @author Charles Bentley
 *
 */
public abstract class PowerPointer extends PowerBuildBase implements ITechPointerStruct {
   
   /**
    * 
    */
   protected int pointerLast;

   /**
    * 
    */
   protected int pointerStart;

   public PowerPointer(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   protected int[] checkPointersBelow(int key, int defValue, int[] pointers) {
      // if pointer can be changed
      if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_4_GROWTH_BELOW)) {
         int diff = pointerStart - key;
         int[] d = new int[pointers.length + diff + 1];
         for (int i = 0; i < diff; i++) {
            d[i] = defValue;
         }
         for (int i = 0; i < pointers.length; i++) {
            d[i + diff] = pointers[i];
         }
         return d;
      } else {
         throw new IllegalArgumentException();
      }
   }

   protected int[][] checkPointersBelow(int key, int[][] pointers) {
      // if pointer can be changed
      if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_4_GROWTH_BELOW)) {
         int diff = pointerStart - key;
         int[][] d = new int[pointers.length + diff + 1][];
         for (int i = 0; i < pointers.length; i++) {
            d[i + diff] = pointers[i];
         }
         return d;
      } else {
         throw new IllegalArgumentException();
      }
   }

   protected int[] checkPointersTop(int key, int defValue, int[] pointers) {
      if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_5_GROWTH_TOP)) {
         int diff = key - pointerLast;
         int[] d = new int[pointers.length + diff + 1];
         for (int i = 0; i < pointers.length; i++) {
            d[i] = pointers[i];
         }
         for (int i = pointers.length; i < d.length; i++) {
            d[i] = defValue;
         }
         return d;
      } else {
         throw new IllegalArgumentException();
      }
   }

   protected int[][] checkPointersTop(int key, int[][] pointers) {
      if (hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_5_GROWTH_TOP)) {
         int diff = key - pointerLast;
         int[][] d = new int[pointers.length + diff + 1][];
         for (int i = 0; i < pointers.length; i++) {
            d[i] = pointers[i];
         }
         return d;
      } else {
         throw new IllegalArgumentException();
      }
   }

   protected int getInsidePointer(int pointer) {
      return pointer - pointerStart;
   }

   public int getSize() {
      return pointerLast - pointerStart;
   }

   protected void initEmpty() {
      pointerStart = get4(PS_OFFSET_04_START_POINTER4);
      pointerLast = get4(PS_OFFSET_05_END_POINTER4);
   }

   public boolean isValidKey(int key) {
      if (key >= pointerStart && key <= pointerLast) {
         return true;
      }
      return false;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerPointer");
      toStringPrivate(dc);
      super.toString(dc.sup());

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerPointer");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {
      dc.appendVarWithSpace("pointerLast", pointerLast);
      dc.appendVarWithSpace("pointerLast", pointerLast);
   }

   //#enddebug

   protected void updateLastPointer(int key) {
      pointerLast = key;
      set4(PS_OFFSET_05_END_POINTER4, key);
   }

   protected void updateStartPointer(int key) {
      pointerStart = key;
      set4(PS_OFFSET_04_START_POINTER4, key);
   }
}
