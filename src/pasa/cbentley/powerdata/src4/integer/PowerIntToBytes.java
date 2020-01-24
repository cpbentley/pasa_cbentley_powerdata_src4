package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToBytes;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToBytes;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public abstract class PowerIntToBytes extends PowerPointer implements IPowerLinkIntToBytes, ITechIntToBytes {

   public PowerIntToBytes(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public byte[] getBytes(int rid) {
      methodStarts();
      try {
         return insideGetBytes(getInsidePointer(rid));
      } finally {
         methodEnds();
      }
   }

   protected abstract byte[] insideGetBytes(int rid);


   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToBytes");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntToBytes");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

   
}
