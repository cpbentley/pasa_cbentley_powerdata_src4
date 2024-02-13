package pasa.cbentley.powerdata.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.interfaces.IJavaObjectFactory;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.IFactoryIDStruct;
import pasa.cbentley.powerdata.spec.src4.ctx.PDCtxA;
import pasa.cbentley.powerdata.src4.base.Morpher;
import pasa.cbentley.powerdata.src4.base.PowerFactory;

public class PDCtx extends PDCtxA {

   public static final int   CTX_ID = 14;

   private BOPowerDataModule boPowerData;

   private Morpher           morpher;

   private PowerFactory      powerFactory;

   public PDCtx(UCtx uc, BOCtx boc) {
      super(uc, boc);

      //#debug
      toDLog().pInit("", this, PDCtx.class, "Created@30", LVL_05_FINE, true);
   }

   public BOModuleAbstract getBOModule() {
      return boPowerData;
   }

   public int getCtxID() {
      return CTX_ID;
   }

   public IJavaObjectFactory getFactory(String id) {
      if (id == IFactoryIDStruct.ID) {
         return getPowerFactory();
      }
      throw new IllegalArgumentException();
   }

   public Morpher getMorpher() {
      if (morpher == null) {
         morpher = new Morpher(this);
      }
      return morpher;
   }

   public PowerFactory getPowerFactory() {
      if (powerFactory == null) {
         powerFactory = new PowerFactory(this);
      }
      return powerFactory;
   }

   public void setPowerFactory(PowerFactory powerFactory) {
      this.powerFactory = powerFactory;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PDCtx");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PDCtx");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug

   private void toStringPrivate(Dctx dc) {

   }

}
