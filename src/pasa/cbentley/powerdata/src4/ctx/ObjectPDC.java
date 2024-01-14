package pasa.cbentley.powerdata.src4.ctx;

import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDLog;
import pasa.cbentley.core.src4.logging.IStringable;

public class ObjectPDC implements IStringable {

   protected final PDCtx pdc;

   public ObjectPDC(PDCtx pdc) {
      this.pdc = pdc;
   }

   public PDCtx getPDC() {
      return pdc;
   }

   //#mdebug
   public IDLog toDLog() {
      return toStringGetUCtx().toDLog();
   }

   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, ObjectPDC.class, "@line5");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, ObjectPDC.class);
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return pdc.getUCtx();
   }

   //#enddebug

}
