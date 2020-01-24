package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.io.BADataIS;
import pasa.cbentley.core.src4.io.BADataOS;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntBuild;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * IPower structure mapping an integer (key) to another integer (data).
 * <br>
 * <br>
 * No assumption is made whatsoever.
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public class PowerIntToIntBuild extends PowerPointer implements IPowerLinkIntToInt, ITechIntToIntBuild {

   /**
    * Buffer
    */
   private int[] pointers = new int[5];

   public PowerIntToIntBuild(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerIntToIntBuildRootTech());
   }

   public PowerIntToIntBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initEmpty();
   }

   public void setKeyData(int key, int value) {
      if (key < pointerStart) {
         // if pointer can be changed
         pointers = checkPointersBelow(key, get4(ITI_OFFSET_02_DEF_VALUE4), pointers);
         updateStartPointer(key);
      } else if (key > pointerLast) {
         pointers = checkPointersTop(key, get4(ITI_OFFSET_02_DEF_VALUE4), pointers);
         updateLastPointer(key);
      }
      //normalize key
      int insidePointer = getInsidePointer(key);
      pointers[insidePointer] = value;
   }

   protected void initEmpty() {
      super.initEmpty();
      int size = pointerLast - pointerStart + 1;
      pointers = new int[size];
   }

   public int getKeyData(int key) {
      int insidePointer = getInsidePointer(key);
      if (insidePointer < 0 || insidePointer >= pointers.length) {
         throw new IllegalArgumentException(insidePointer + " >= " + pointers.length);
      }
      return pointers[insidePointer];
   }

   public Object getMorph(MorphParams p) {
      return this;
   }

   public byte[] serializeRaw() {
      int totalDataSize = 4 + pointers.length * 4;
      BADataOS dos = serializeRawHelper(totalDataSize);
      dos.writeInt(pointers.length);
      for (int i = 0; i < pointers.length; i++) {
         dos.writeInt(pointers[i]);
      }
      return dos.getOut().toByteArray();
   }

   public void serializeReverse() {
      if (hasData()) {
         BADataIS dis = getDataInputStream();
         int nextEmpty = dis.readInt();
         pointers = new int[nextEmpty];
         for (int i = 0; i < nextEmpty; i++) {
            pointers[i] = dis.readInt();
         }
      } else {
         initEmpty();
      }
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      byte[] data = serializeRaw();
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(data);
      return bom;
   }

}
