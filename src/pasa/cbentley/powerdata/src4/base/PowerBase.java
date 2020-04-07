package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public abstract class PowerBase extends ByteObjectManaged {

   protected final PDCtx pdc;

   /**
    * See {@link ByteObjectManaged#ByteObjectManaged(ByteObject)}
    * <br>
    * <br>
    * @param tech
    */
   public PowerBase(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc.getBOC(), tech);
      this.pdc = pdc;
   }
   
   public BitUtils getBU() {
      return pdc.getUCtx().getBU();
   }

   
   protected BitCoordinate createBitCoordinate() {
      return new BitCoordinate(pdc.getUCtx());
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerBase");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerBase");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
