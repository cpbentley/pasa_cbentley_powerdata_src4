package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.ITechLvl;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
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
public abstract class PowerBuildBase extends PowerBase implements IDataMorphable, ITechMorph {

   private int           morphFlags;

   /**
    * See {@link ByteObjectManaged#ByteObjectManaged(ByteObject)}
    * <br>
    * <br>
    * @param tech
    */
   public PowerBuildBase(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

 

   public Object getMorphedBuild(int type) {
      return this;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public boolean hasMorphFlag(int flag) {
      return this.hasFlag(morphFlags, flag);
   }

   public void printDataStruct(String log) {
      pdc.getUCtx().toDLog().pMemory(log, this, PowerBuildBase.class, "printDataStruct", ITechLvl.LVL_05_FINE, true);
   }

   public void setMorphFlag(int flag, boolean v) {
      morphFlags = BitUtils.setFlag(morphFlags, flag, v);
   }

   //#mdebug

   public void toString(Dctx dc) {
      dc.root(this, "PowerBuildBase");
      super.toString(dc.newLevel());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerBuildBase");
   }
   //#enddebug
}
