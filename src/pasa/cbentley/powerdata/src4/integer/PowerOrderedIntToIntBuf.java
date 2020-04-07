package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.core.src4.ctx.UCtx;

import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntToInts;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerIntArrayOrderToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkOrderedIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechOrderedIntToInt;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Simple use of {@link IntBuffer} for values and datas.
 * <br>
 * @author Charles Bentley
 *
 */
public class PowerOrderedIntToIntBuf extends PowerBuildBase implements IPowerIntArrayOrderToInt, IPowerLinkIntToInt, ITechOrderedIntToInt {

   private IntToInts ints;

   public PowerOrderedIntToIntBuf(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerOrderedIntToIntBufRootTech());
   }

   public PowerOrderedIntToIntBuf(PDCtx pdc, ByteObjectManaged bom) {
      super(pdc, bom);
      initEmpty();
   }

   protected void initEmpty() {
      ints = new IntToInts(pdc.getUCtx(), IntToInts.TYPE_1UNO_ORDER, 6, false, -1);
   }

   public void addValue(int value) {
      this.addValue(value, 0);
   }

   public void addValue(int value, int data) {
      ints.add(value, data);
   }

   public void addValueShift(int value) {
      this.addValueShift(value, 0);
   }

   public synchronized void addValueShift(int value, int data) {
      int key = value;
      ints.manageNewIndex();
      int numKeys = ints.getNumKeys();
      int end = numKeys * 2 + 1;
      int[] ints = this.ints.getAllUnosDuos();
      int count = 0;
      for (int i = 1; i <= end; i += 2) {
         if (ints[i] >= key) {
            //insert just before
            int startOffset = i;
            //int shiftLen = (2 * numKeys) - (count * 2);
            int endOffset = end;
            //System.out.println("shift start at "+ i + " for Len=" + shiftLen + " numKeys=" + numKeys);
            IntUtils.shiftIntUp(ints, 2, startOffset, endOffset, false);

            ints[i] = value;
            ints[i + 1] = data;
            ints[0] += 1;
            //increment all values from 
            for (int j = i + 2; j <= end; j += 2) {
               ints[j]++;
            }
            return;
         }
         count++;
      }
      //was added at the end. just add it
      setKeyData(value, data);
   }

   public int getData(int value) {
      return ints.getDuoFromUno(value);
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getSize() {
      return ints.getNumKeys();
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public int getValueFromPosition(int count) {
      return ints.getUnoIndex(count);
   }

   public int getValueOrderCount(int value) {
      int val = ints.getUnoIndex(value);
      return val + 1;
   }

   public int[] getValues() {
      return ints.getAllUnos();
   }

   public int[] getValuesDatas() {
      return ints.getAllUnosDuos();
   }

   public boolean hasValue(int value) {
      return ints.getUnoIndex(value) != -1;
   }

   public int remove(int value) {
      int index = ints.getUnoIndex(value);
      if (index != -1) {
         int data = ints.getIndexedDuo(index);
         ints.removeUno(value);
         return data;
      }
      return -1;
   }

   private byte[] serializeRaw() {
      byte[] header = toByteArray();
      ByteObjectManaged tr = new ByteObjectManaged(pdc.getBOC(), header);

      int numKeys = ints.getNumKeys();
      int dataSize = 4 + (numKeys * 2 * 4);
      tr.expandResetArrayData(dataSize);
      byte[] data = tr.getByteObjectData();
      int offsetData = tr.getDataOffsetStartLoaded();
      IntUtils.writeIntBE(data, offsetData, numKeys);
      offsetData += 4;
      for (int i = 0; i < numKeys; i++) {
         IntUtils.writeIntBE(data, offsetData, ints.getIndexedUno(i));
         offsetData += 4;
         IntUtils.writeIntBE(data, offsetData, ints.getIndexedDuo(i));
         offsetData += 4;
      }
      return data;

   }

   public void serializeReverse() {
      int soff = getDataOffsetStartLoaded();
      int num = IntUtils.readIntBE(data, soff);
      soff += 4;
      ints = new IntToInts(pdc.getUCtx(), IntToInts.TYPE_1UNO_ORDER, 6, false, -1, num * 2);
      for (int i = 0; i < num; i++) {
         int uno = IntUtils.readIntBE(data, soff);
         soff += 4;
         int duo = IntUtils.readIntBE(data, soff);
         soff += 4;
         ints.add(uno, duo);
      }
      removeData();
   }

   public ByteObjectManaged serializeTo(ByteController byteCon) {
      return byteCon.serializeToUpdateAgentData(serializeRaw());
   }

   /**
    * only space for 1 data
    */
   public void setValueDatas(int value, int[] datas) {
      if (hasValue(value)) {

      } else {
         addValue(value, datas[0]);
      }
   }

   public int getKeyData(int key) {
      return getData(key);
   }

   public void setKeyData(int key, int data) {
      addValue(key, data);
   }

   public boolean isValidKey(int key) {
      // TODO Auto-generated method stub
      return false;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerOrderedIntToIntBuf");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerOrderedIntToIntBuf");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
