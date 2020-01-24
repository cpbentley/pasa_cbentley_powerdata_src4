package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntBuild;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * 
 * int[] array in the {@link ByteObjectManaged} framework.
 * <br>
 * <br>
 * 
 * This is the base array for building a simple array base
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class PowerIntArrayBuild extends PowerBuildBase implements IPowerLinkIntToInt, ITechIntToIntBuild {

   private int[] dataInt;

   private int   lastused = -1;

   public PowerIntArrayBuild(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerIntArrayBuildRootTech());
   }

   public PowerIntArrayBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      dataInt = new int[1024];
   }

   public PowerIntArrayBuild(PDCtx pdc, int[] ar) {
      this(pdc);
      for (int i = 0; i < ar.length; i++) {
         addInt(ar[i]);
      }
   }

   public void addInt(int value) {
      if (!hasValue(value)) {
         if (lastused + 1 >= dataInt.length) {
            dataInt = getUCtx().getMem().increaseCapacity(dataInt, dataInt.length);
         }
         dataInt[lastused + 1] = value;
         lastused++;
         increaseVersionCount();
      }
   }

   public void addValue(int value) {
      addInt(value);
   }

   public void bufferTrim() {
      int[] newd = new int[lastused + 1];
      System.arraycopy(dataInt, 0, newd, 0, lastused);
      dataInt = newd;
   }

   public int[] getInts() {
      int[] ar = new int[lastused + 1];
      for (int i = 0; i < ar.length; i++) {
         ar[i] = dataInt[i];
      }
      return ar;
   }

   public int getKeyData(int key) {
      return dataInt[key];
   }

   public Object getMorph(MorphParams p) {
      //return runBag();
      return this;
   }

   public int getSize() {
      return size();
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public int[] getValues() {
      return getInts();
   }

   public boolean hasValue(int value) {
      for (int i = 0; i <= lastused; i++) {
         if (dataInt[i] == value)
            return true;
      }
      return false;
   }

   public boolean isValidKey(int key) {
      // TODO Auto-generated method stub
      return false;
   }

   public int remove(int value) {
      int v = 0;
      if (value >= 0 && value < dataInt.length) {
         v = dataInt[value];
         dataInt[value] = 0;
      }
      return v;
   }

   public PowerIntArrayRun runBag() {
      PowerIntArrayRun b = new PowerIntArrayRun(pdc);
      for (int i = 0; i <= lastused; i++) {
         b.addInt(dataInt[i]);
      }
      b.bufferTrim();
      return b;
   }

   public byte[] serializePack() {
      // TODO Auto-generated method stub
      return null;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController byteCon) {
      return null;
   }

   public void setKeyData(int key, int data) {
      // TODO Auto-generated method stub

   }

   public int size() {
      return lastused + 1;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntArrayBuild");
      toStringPrivate(dc);
      super.toString(dc.sup());
      int max = 0;
      if (max < 1)
         max = lastused;
      max = Math.min(lastused, max);
      dc.append("size=" + (lastused + 1) + " ");
      for (int i = 0; i <= max; i++) {
         dc.append(dataInt[i]);
         if (i + 1 <= lastused) {
            dc.append(",");
         }
      }
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntArrayBuild");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   private void toStringPrivate(Dctx dc) {

   }

   //#enddebug

}
