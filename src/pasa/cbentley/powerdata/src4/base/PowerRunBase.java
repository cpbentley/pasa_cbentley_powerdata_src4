package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.powerdata.spec.src4.power.IDataMorphable;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechMorph;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Class for those structures that don't need byte array.
 * <br>
 * <br>
 * 
 * @author Charles Bentley
 *
 */
public abstract class PowerRunBase extends PowerBase implements IDataMorphable, ITechMorph {

   /**
    * See {@link ByteObjectManaged#ByteObjectManaged(ByteObject)}
    * <br>
    * <br>
    * @param tech
    */
   public PowerRunBase(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public Object getMorphedRun(int type) {
      return this;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public boolean hasMorphFlag(int flag) {
      return hasFlag(MORPH_OFFSET_01_FLAG, flag);
   }

   /**
    * return the whole byte array
    */
   public byte[] serializePack() {
      return data;
   }

   public void setMorphFlag(int flag, boolean v) {
      setFlag(MORPH_OFFSET_01_FLAG, flag, v);
   }
}
