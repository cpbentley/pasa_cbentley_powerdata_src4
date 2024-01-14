package pasa.cbentley.powerdata.src4.ctx;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.interfaces.IBOAgentManaged;
import pasa.cbentley.byteobjects.src4.ctx.IBOTypesBOC;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IDebugStringable;
import pasa.cbentley.powerdata.spec.src4.engine.TechFactory;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;

/**
 * ByteObject module for all {@link ByteObject} defined in the scope of {@link PDCtx}.
 * <br>
 * Enable the dynamic debug of ByteObject.
 * 
 * To String is delegated to {@link TechFactory}.
 * 
 * @author Charles Bentley
 *
 */
public class BOPowerDataModule extends BOModuleAbstract implements IDebugStringable, IPowerDataTypes {

   public static final int BIP_FACTORY_ID = 14;

   public static final int BIP_MODULE_ID  = 43;

   protected final PDCtx   pdc;

   public BOPowerDataModule(PDCtx pdc) {
      super(pdc.getBOC());
      this.pdc = pdc;

   }

   public ByteObject getFlagOrderedBO(ByteObject bo, int offset, int flag) {
      // TODO Auto-generated method stub
      return null;
   }

   public String toStringGetDIDString(int did, int value) {
      return null;
   }

   public ByteObject merge(ByteObject root, ByteObject merge) {
      int type = merge.getType();
      switch (type) {
         default:
            //not found here
            return null;
      }
   }

   /**
    * {@link IBOAgentManaged#AGENT_BASE_TYPE} which is {@link IBOTypesBOC#TYPE_035_STRUCT}.
    * 
    */
   public void subToString(Dctx dc, ByteObject bo) {
      int type = bo.getType();
      switch (type) {
         case IBOAgentManaged.AGENT_BASE_TYPE:
            int subtype = bo.get2(IBOAgentManaged.AGENT_OFFSET_05_CLASS_ID2);
            subToStringClass(bo, dc, subtype);
         default:
      }
   }

   public void subToString1Line(Dctx dc, ByteObject bo) {
      int type = bo.getType();
      switch (type) {
         case IBOAgentManaged.AGENT_BASE_TYPE:
            int subtype = bo.get2(IBOAgentManaged.AGENT_OFFSET_05_CLASS_ID2);
            subToStringClass(bo, dc, subtype);
      }
   }

   /**
    * String representation of the header given the class id from {@link IBOAgentManaged#AGENT_OFFSET_05_CLASS_ID2}
    * <br>
    * <br>
    * 
    * @param bo
    * @param nl
    * @param classid
    * @return
    */
   public void subToStringClass(ByteObject bo, Dctx dc, int classid) {
      TechFactory techFactory = pdc.getTechFactory();
      switch (classid) {
         case IPowerDataTypes.CLASS_TYPE_100_POWER_CHAR_TRIE:
            techFactory.toStringTechCharTrie(dc, bo);
            break;
         case IPowerDataTypes.CLASS_TYPE_65_NODE_DATA_FAST:
            techFactory.toStringFastNodeDataTech(dc, bo);
            break;
         case IPowerDataTypes.CLASS_TYPE_44_CHAR_COL_BUILD:
            techFactory.toStringPowerCharColBuildTech(dc, bo);
            break;
         case IPowerDataTypes.CLASS_TYPE_66_NODE_DATA_FAST_CHAR:
            techFactory.toStringFastNodeDataCharTech(dc, bo);
            break;
         case IPowerDataTypes.CLASS_TYPE_25_INT_TO_BYTES_RUN:
            techFactory.toStringPowerIntToBytesRunTech(bo, dc);
            break;
         default:
            dc.append("Unknown Class ID " + classid);
            break;
      }
   }


   public boolean toString(Dctx dc, ByteObject bo) {
      // TODO Auto-generated method stub
      return false;
   }


   public boolean toString1Line(Dctx dc, ByteObject bo) {
      // TODO Auto-generated method stub
      return false;
   }

   public String toStringOffset(ByteObject o, int offset) {
      // TODO Auto-generated method stub
      return null;
   }

   public String toStringType(int type) {
      // TODO Auto-generated method stub
      return null;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, BOPowerDataModule.class, "@line5");
      toStringPrivate(dc);
      super.toString(dc.sup());
   }

   private void toStringPrivate(Dctx dc) {
      
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, BOPowerDataModule.class);
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug
   

}
