package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntSorter;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToInts;
import pasa.cbentley.powerdata.src4.ctx.BOPowerDataModule;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * 
 * @author Charles Bentley
 *
 */
public abstract class PowerIntToInts extends PowerPointer implements IPowerLinkIntToInts, ITechIntToInts {

   private IntSorter sorter;

   public PowerIntToInts(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public IntSorter getSorter() {
      if (sorter == null) {
         int type = get1(ITIS_OFFSET_02_ORDER_TYPE1);
         boolean isUnique = !hasFlag(ITIS_OFFSET_01_FLAG, ITIS_FLAG_2_DUPLICATES);
         sorter = new IntSorter(getUCtx(), type, isUnique);
      }
      return sorter;
   }

   
   public void addValuesToKey(int[] value, int key) {
      methodStartsWrite();
      try {
         insideAddValuesToKey(value, getInsidePointer(key));
      } finally {
         methodEndsWrite();
      }
   }
   public boolean isValueBelongToKey(int value, int key) {
      return getIndexOfValueFromKey(value, key) != -1;
   }

   /**
    * If using
    * @param value
    * @param key
    * @return
    */
   protected int[] addValueToArray(int value, int[] array) {
      int val = getSorter().addToArray(value, array);
      return getSorter().getArray();
   }

   public void addValueToKey(int value, int pointer) {
      methodStartsWrite();
      try {
         insideAddValueToKey(value, getInsidePointer(pointer));
      } finally {
         methodEndsWrite();
      }
   }

   public int getIndexOfValueFromKey(int value, int key) {
      methodStarts();
      try {
         return insideGetIndexOfValueFromKey(value, getInsidePointer(key));
      } finally {
         methodEnds();
      }
   }

   public int getKeyData(int key) {
      methodStarts();
      try {
         return insideGetKeyData(getInsidePointer(key));
      } finally {
         methodEnds();
      }
   }

   public int[] getKeysValues(int[] keys) {
      methodStarts();
      try {
         int[] v = new int[keys.length];
         for (int i = 0; i < v.length; i++) {
            v[i] = getInsidePointer(keys[i]);
         }
         return insideGetKeysValues(v);
      } finally {
         methodEnds();
      }
   }

   public int getKeyValue(int key, int index) {
      methodStarts();
      try {
         return insideGetKeyValue(getInsidePointer(key), index);
      } finally {
         methodEnds();
      }
   }

   public int[] getKeyValues(int pointer) {
      methodStarts();
      try {
         return insideGetKeyValues(getInsidePointer(pointer));
      } finally {
         methodEnds();
      }
   }

   public int getNumValuesFromKey(int key) {
      methodStarts();
      try {
         return insideGetNumValuesFromKey(getInsidePointer(key));
      } finally {
         methodEnds();
      }
   }

   protected abstract void insideAddValuesToKey(int[] value, int key);

   protected abstract void insideAddValueToKey(int value, int key);

   /**
    * 
    * @param value
    * @param key
    * @return
    * @see IPowerLinkIntToInts#getIndexOfValueFromKey(int, int)
    */
   protected abstract int insideGetIndexOfValueFromKey(int value, int key);

   protected abstract int insideGetKeyData(int key);

   protected abstract int[] insideGetKeysValues(int[] keys);

   protected abstract int insideGetKeyValue(int key, int index);

   protected abstract int[] insideGetKeyValues(int pointer);

   protected abstract int insideGetNumValuesFromKey(int pointer);

   protected abstract int insideRemoveKeyValues(int key);

   protected abstract void insideRemoveValueFromKey(int value, int key);

   protected abstract void insideSetKeyData(int key, int data);

   protected boolean isUnique() {
      return !hasFlag(ITIS_OFFSET_01_FLAG, ITIS_FLAG_2_DUPLICATES);
   }

   public int removeKeyValues(int key) {
      methodStartsWrite();
      try {
         return insideRemoveKeyValues(getInsidePointer(key));
      } finally {
         methodEndsWrite();
      }
   }

   public void removeValueFromKey(int value, int key) {
      methodStartsWrite();
      try {
         insideRemoveValueFromKey(value, getInsidePointer(key));
      } finally {
         methodEndsWrite();
      }
   }

   protected abstract void insideSetValuesForKey(int[] values, int key);

   public void setValuesForKey(int[] values, int key) {
      methodStartsWrite();
      try {
         insideSetValuesForKey(values, getInsidePointer(key));
      } finally {
         methodEndsWrite();
      }
   }

   public void setKeyData(int key, int data) {
      methodStartsWrite();
      try {
         insideSetKeyData(getInsidePointer(key), data);
      } finally {
         methodEndsWrite();
      }
   }

   //#mdebug

   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToInts");
      super.toString(dc.newLevel());
   }

   public void toString1Line(Dctx dc) {
      dc.root(this, "PowerIntToInts");
   }
   //#enddebug
}
