package pasa.cbentley.powerdata.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.byteobjects.src4.interfaces.IJavaObjectFactory;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.IFactoryIDStruct;
import pasa.cbentley.powerdata.spec.src4.ctx.PDCtxA;
import pasa.cbentley.powerdata.spec.src4.engine.TechFactory;
import pasa.cbentley.powerdata.src4.base.Morpher;
import pasa.cbentley.powerdata.src4.base.PowerFactory;

public class PDCtx extends PDCtxA {

   private BOPowerDataModule boPowerData;

 
   public static final int CTX_ID= 14;
   
   public int getCtxID() {
      return CTX_ID;
   }
   private PowerFactory      powerFactory;

   private Morpher           morpher;

   public PowerFactory getPowerFactory() {
      if (powerFactory == null) {
         powerFactory = new PowerFactory(this);
      }
      return powerFactory;
   }

   public void setPowerFactory(PowerFactory powerFactory) {
      this.powerFactory = powerFactory;
   }

   public PDCtx(UCtx uc, BOCtx boc) {
      super(uc, boc);

   }

   public Morpher getMorpher() {
      if (morpher == null) {
         morpher = new Morpher(this);
      }
      return morpher;
   }

   public BOModuleAbstract getBOModule() {
      return boPowerData;
   }



   public IJavaObjectFactory getFactory(String id) {
      if (id == IFactoryIDStruct.ID) {
         return getPowerFactory();
      }
      throw new IllegalArgumentException();
   }
   
   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PDCtx");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {
      
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PDCtx");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }
   //#enddebug


   

}
