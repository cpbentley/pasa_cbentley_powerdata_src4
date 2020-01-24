package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.structs.IntSorter;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRunBytes;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Uses a {@link PowerIntToBytesRun} to store integers as bytes.
 * <br>
 * @author Charles-Philip Bentley
 *
 */
public class PowerIntToIntsRunBytes extends PowerIntToInts implements IPowerLinkIntToInts, ITechIntToIntsRunBytes {


   private PowerIntToBytesRun db;

   public PowerIntToIntsRunBytes(PDCtx pdc) {
      this(pdc,pdc.getTechFactory().getPowerIntToIntsRunBytesRootTech());
   }

   public PowerIntToIntsRunBytes(PDCtx pdc, ByteObjectManaged bo) {
      super(pdc, bo);
   }

   public void serializeReverse() {

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return bc.serializeToUpdateAgentData(serializeRaw());
   }

   private byte[] serializeRaw() {
      // TODO Auto-generated method stub
      return null;
   }

   
   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected int insideGetKeyValue(int key, int index) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideSetKeyData(int key, int data) {
      // TODO Auto-generated method stub

   }

   
   protected int[] insideGetKeysValues(int[] keys) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected int insideGetKeyData(int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideAddValuesToKey(int[] values, int key) {
      int[] vals = insideGetKeyValues(key);
      //add those values
      for (int i = 0; i < values.length; i++) {
         vals = addValueToArray(values[i], vals);
      }
      compressAndSet(vals, key);
   }

   /**
    * Use compression method or best of
    * @param values
    * @param key
    */
   private void compressAndSet(int[] values, int key) {

      byte[] data = null;
      int type = get1(ITIS_RUNBYTES_OFFSET_02_TYPE1);
      switch (type) {
         case ITIS_RUNBYTES_TYPE_0_NONE:

            break;

         default:
            break;
      }
      db.setBytes(key, data);
   }

   
   protected void insideAddValueToKey(int value, int key) {
      // TODO Auto-generated method stub

   }

   
   protected int insideRemoveKeyValues(int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideRemoveValueFromKey(int value, int key) {
      // TODO Auto-generated method stub

   }

   
   protected int insideGetNumValuesFromKey(int pointer) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected int[] insideGetKeyValues(int pointer) {
      byte[] b = db.getBytes(pointer);
      if (b == null) {
         return null;
      } else {
         int compressionType = b[0];
         switch (compressionType) {
            case ITIS_RUNBYTES_TYPE_0_NONE:
               break;

            default:
               break;
         }
      }
      throw new IllegalArgumentException();
   }

   
   public IPowerEnum getEnu(int type, Object param) {
      // TODO Auto-generated method stub
      return null;
   }

   
   protected int insideGetIndexOfValueFromKey(int value, int key) {
      // TODO Auto-generated method stub
      return 0;
   }

   
   protected void insideSetValuesForKey(int[] values, int key) {
      // TODO Auto-generated method stub
      
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToIntsRunBytes");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntToIntsRunBytes");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
