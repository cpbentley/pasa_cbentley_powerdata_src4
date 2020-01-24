package pasa.cbentley.powerdata.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.byteobjects.src4.interfaces.IJavaObjectFactory;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.powerdata.spec.src4.IFactoryIDStruct;
import pasa.cbentley.powerdata.spec.src4.ctx.PDCtxA;
import pasa.cbentley.powerdata.src4.base.PowerFactory;
import pasa.cbentley.powerdata.src4.base.TechFactory;

public class PDCtx extends PDCtxA {

   private BOPowerDataModule  boPowerData;

   private TechFactory  techFactory;

   private PowerFactory powerFactory;

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

   public BOModuleAbstract getBOModule() {
      return boPowerData;
   }

   public TechFactory getTechFactory() {
      if (techFactory == null) {
         techFactory = new TechFactory(this);
      }
      return techFactory;
   }

   public IJavaObjectFactory getFactory(String id) {
      if(id == IFactoryIDStruct.ID) {
         return getPowerFactory();
      }
      throw new IllegalArgumentException();
   }
}
