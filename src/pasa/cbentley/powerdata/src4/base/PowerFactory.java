package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.byteobjects.src4.interfaces.IJavaObjectFactory;
import pasa.cbentley.byteobjects.src4.tech.ITechObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechMorph;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.integer.PowerIntArrayBuild;
import pasa.cbentley.powerdata.src4.integer.PowerIntToBytesBuild;
import pasa.cbentley.powerdata.src4.integer.PowerIntToBytesRun;
import pasa.cbentley.powerdata.src4.integer.PowerIntToIntBuild;
import pasa.cbentley.powerdata.src4.integer.PowerIntToIntsBuild;
import pasa.cbentley.powerdata.src4.integer.PowerOrderedIntToIntBuf;
import pasa.cbentley.powerdata.src4.string.PowerCharColBuild;
import pasa.cbentley.powerdata.src4.string.PowerCharColRun;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringHashtableBuild;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringToInt;
import pasa.cbentley.powerdata.src4.string.PowerLinkStringToIntArray;
import pasa.cbentley.powerdata.src4.trie.FastNodeData;
import pasa.cbentley.powerdata.src4.trie.FastNodeDataChar;
import pasa.cbentley.powerdata.src4.trie.PowerCharTrie;
import pasa.cbentley.powerdata.src4.trie.PowerTrieLink;

/**
 * Factory has rules for loading object types.
 * <br>
 * It might decide to look for different type, if the one given is not found.
 * <br>
 * <br>
 * 
 * @author Charles-Philip
 *
 */
public class PowerFactory implements IJavaObjectFactory, IPowerDataTypes, ITechMorph {

   protected PDCtx    pdc;

   protected BOModuleAbstract mod;

   public PowerFactory(PDCtx pdc) {
      this.pdc = pdc;
   }

   public ByteObjectManaged createMorphObject(ByteObjectManaged tech, ByteController bc, Object param) {
      int intID = tech.get2(AGENT_OFFSET_04_INTERFACE_ID2);
      if (intID == 0 || intID == ByteObjectManaged.INTERFACE_OFFSET_MASK10BITS) {
         throw new IllegalArgumentException();
      }
      ByteObjectManaged bom = (ByteObjectManaged) createObject(tech, intID, bc);
      if (param != null) {
         if (bom instanceof IPowerCharCollector) {
            IPowerCharCollector ch = (IPowerCharCollector) bom;
            if (param instanceof String[]) {
               String[] da = (String[]) param;
               for (int i = 0; i < da.length; i++) {
                  ch.addChars(da[i]);
               }
               return bom;
            }
         }
         throw new RuntimeException();
      }
      return bom;
   }

   /**
    * @param tech
    */
   public Object createObject(ByteController mod, ByteObjectManaged tech) {
      if (tech == null) {
         throw new NullPointerException("#PowerFactory Cannot Create Object for Null Tech=");
      }
      ByteObjectManaged bom = null;
      int classid = tech.get2(ITechObjectManaged.AGENT_OFFSET_05_CLASS_ID2);
      bom = switchClassID(classid, tech);
      //      if (classid != 0) {
      //         if (classid == CLASS_TYPE_100_POWER_CHAR_TRIE) {
      //            bom = new PowerCharTrie(pdc, (ByteObjectManaged) tech);
      //         } else if (classid == CLASS_TYPE_44_CHAR_COL_BUILD) {
      //            ByteObjectManaged te = (ByteObjectManaged) tech;
      //            bom = new PowerCharColBuild(pdc, tech);
      //         } else if (classid == CLASS_TYPE_91_STRING_TO_INT) {
      //            bom = new PowerLinkStringToInt(mod.getModule(), tech);
      //         } else if (classid == CLASS_TYPE_90_STRINGLINKER_HASHTABLE) {
      //            bom = new PowerLinkStringHashtableBuild(pdc, tech);
      //         } else if (classid == IPowerDataTypes.CLASS_TYPE_34_ORDERED_INT_TO_INT_RUN) {
      //            bom = new PowerOrderedIntToIntRun(pdc, tech);
      //         } else if (classid == CLASS_TYPE_65_NODE_DATA_FAST) {
      //            bom = new FastNodeData(pdc, tech);
      //         } else if (classid == CLASS_TYPE_66_NODE_DATA_FAST_CHAR) {
      //            bom = new FastNodeDataChar(pdc, tech);
      //         } else if (classid == CLASS_TYPE_45_CHAR_COL_RUN_LIGHT) {
      //            bom = new PowerCharColBuild(pdc, tech);
      //         } else if (classid == CLASS_TYPE_35_ORDERED_INT_TO_INT_BUF) {
      //            bom = new PowerOrderedIntToIntBuf(pdc, tech);
      //         } else if (classid == CLASS_TYPE_32_INT_ARRAY_BUILD) {
      //            bom = new PowerIntArrayBuild(pdc, tech);
      //         } else if (classid == CLASS_TYPE_31_INT_ARRAY_RUN) {
      //            bom = new PowerIntArrayRun(pdc, tech);
      //         }
      //      } else {
      if (bom == null) {
         //in case of this method.. deserialization must know which class.
         String msg = "PowerFactory#createObject could not find a match for ClassID " + classid;
         throw new IllegalArgumentException(msg);
      }
      return bom;
   }

   /**
    * 
    */
   public Object createObject(ByteObjectManaged tech, int intID) {
      return createObject(tech, intID, null);
   }

   /**
    * Create 
    */
   public Object createObject(ByteObjectManaged tech, int intID, ByteController bc) {
      if (tech == null) {
         //gets default tech for intID
         tech = switchIntIDGetRootTech(intID);
         if (tech == null) {
            throw new IllegalArgumentException("Unknown Interface ID " + intID);
         }
         bc.addAgent(tech);
      }
      Object o = insideCreateObject(tech, intID);
      if (o == null) {
         throw new IllegalArgumentException("Unknown Interface ID " + intID);
      }
      return o;
   }

   /**
    * Based on the {@link ITechMorph} tech, create.
    * Everything except the ClassID is correct
    */
   public Object createObjectInt(int intid, ByteObjectManaged tech) {
      //construct a new tech from the hints
      ByteObjectManaged bom = null;
      boolean isBuild = tech.get1(ITechMorph.MORPH_OFFSET_02_MODE1) == ITechMorph.MODE_1_BUILD;

      if (intid == INT_23_CHAR_TRIE) {
         bom = new PowerCharTrie(pdc);
      } else if (intid == INT_22_CHAR_COLLECTOR) {
         if (isBuild) {
            ByteObjectManaged techb = pdc.getTechFactory().getPowerCharColBuildTechRoot();
            int classID = techb.getIDClass();
            techb.burnHeader(tech);
            techb.set2(ITechObjectManaged.AGENT_OFFSET_05_CLASS_ID2, classID);
            bom = new PowerCharColBuild(pdc, techb);
         } else {
            //choose a Trie
            if (tech.hasFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_4_SMALL_FOOT_PRINT)) {
               bom = new PowerCharColRun(pdc);
            } else {
               bom = new PowerCharTrie(pdc);
            }
         }
      } else if (intid == INT_45_TRIE_NODE_DATA_CHAR) {
         bom = new FastNodeData(pdc);
      } else if (intid == INT_44_TRIE_NODE_DATA) {
         bom = new FastNodeDataChar(pdc);
      } else if (intid == INT_27_LINK_ORDEREDINT_TO_INT) {
         bom = new PowerOrderedIntToIntBuf(pdc);
      } else if (intid == INT_28_LINK_INT_TO_INT) {
         bom = new PowerIntToIntBuild(pdc);
      } else if (intid == INT_29_LINK_INT_TO_INTS) {
         bom = new PowerIntToIntsBuild(pdc);
      }
      if (bom == null) {
         throw new NullPointerException("#PowerFactory Cannot Create Object for intid=" + intid);
      }
      return bom;
   }

   public ByteObjectManaged createRootTech(int intID) {
      return switchIntIDGetRootTech(intID);
   }

   /**
    * Factory is responsible to sanitiaze the Tech.
    * <br>
    * Tech might not be fully compatible.
    * 
    * {@link ByteObjectManaged#initMe()} is called
    */
   public Object insideCreateObject(ByteObjectManaged tech, int intID) {
      if (tech == null) {
         throw new NullPointerException("#PowerFactory Cannot Create Object for Null Tech=");
      }
      ByteObjectManaged bom = null;
      int classid = tech.get2(ITechObjectManaged.AGENT_OFFSET_05_CLASS_ID2);
      if (classid != 0) {
         bom = switchClassID(classid, tech);
      }
      if (bom == null) {
         bom = (ByteObjectManaged) createObjectInt(intID, tech);
      }
      if (bom == null) {
         throw new IllegalArgumentException("Could not create object from parameters " + tech);
      } else {
         bom.initMe();
      }
      return bom;
   }

   private ByteObjectManaged switchClassID(int classid, ByteObjectManaged tech) {
      switch (classid) {
         case CLASS_TYPE_22_INT_TO_INT_CABLE_BUILD:
            return new PowerIntToIntBuild(pdc, tech);
         case CLASS_TYPE_23_ORDERED_INT_TO_INT_BUILD:
            return new PowerOrderedIntToIntBuf(pdc, tech);
         case CLASS_TYPE_25_INT_TO_BYTES_RUN:
            return new PowerIntToBytesRun(pdc, tech);
         case CLASS_TYPE_26_INT_TO_BYTES_BUILD:
            return new PowerIntToBytesBuild(pdc, tech);
         case CLASS_TYPE_29_INT_TO_INTS_RUN:
            return new PowerIntToIntBuild(pdc, tech);
         case CLASS_TYPE_30_INT_TO_INTS_BUILD:
            return new PowerIntToIntsBuild(pdc, tech);
         case CLASS_TYPE_32_INT_ARRAY_BUILD:
            return new PowerIntArrayBuild(pdc, tech);
         case CLASS_TYPE_35_ORDERED_INT_TO_INT_BUF:
            return new PowerOrderedIntToIntBuf(pdc, tech);
         case CLASS_TYPE_44_CHAR_COL_BUILD:
            return new PowerCharColBuild(pdc, tech);
         case CLASS_TYPE_45_CHAR_COL_RUN_LIGHT:
            return new PowerCharColRun(pdc, tech);
         case CLASS_TYPE_65_NODE_DATA_FAST:
            return new FastNodeData(pdc, tech);
         case CLASS_TYPE_66_NODE_DATA_FAST_CHAR:
            return new FastNodeDataChar(pdc, tech);
         case CLASS_TYPE_90_STRINGLINKER_HASHTABLE:
            return new PowerLinkStringHashtableBuild(pdc, tech);
         case CLASS_TYPE_91_STRING_TO_INT:
            return new PowerLinkStringToInt(pdc, tech);
         case CLASS_TYPE_92_STRING_TO_INTS:
            return new PowerLinkStringToIntArray(pdc, tech);
         case CLASS_TYPE_100_POWER_CHAR_TRIE:
            return new PowerCharTrie(pdc, tech);
         case CLASS_TYPE_103_POWER_TRIE_DATA:
            return new PowerTrieLink(pdc, tech);
         default:
            break;
      }
      return null;
   }

   /**
    * Get default structure configurations for the interface ID.
    * <br>
    * The Tech definition is unknown so usually it will be a build structure.
    * <br>
    * No ByteController? because it will be used by the calling {@link ByteController}.
    * <br>
    * @param mod
    * @param intid a Tech header for the Interface ID. Factory has its own strategy to choose
    * default interface implementation.
    * @return
    */
   private ByteObjectManaged switchIntIDGetRootTech(int intid) {
      ByteObjectManaged bom = null;
      switch (intid) {
         case INT_22_CHAR_COLLECTOR:
            return pdc.getTechFactory().getPowerCharColBuildRoot();
         case INT_23_CHAR_TRIE:
            return pdc.getTechFactory().getPowerCharTrieRoot();
         case INT_24_LINK_STRING_TO_INT:
            //string mapper to integer default to trie in this factory
            return pdc.getTechFactory().getPowerCharTrieRoot();
         case INT_25_LINK_STRING:
            return pdc.getTechFactory().getPowerLinkStringHashtableBuildRoot();
         case INT_26_LINK_STRING_TO_INTARRAY:
            return pdc.getTechFactory().getPowerLinkStringToIntArrayRoot();
         case INT_27_LINK_ORDEREDINT_TO_INT:
            return pdc.getTechFactory().getPowerOrderedIntToIntBufRoot();
         case INT_28_LINK_INT_TO_INT:
            return pdc.getTechFactory().getPowerIntToIntBuildRoot();
         case INT_29_LINK_INT_TO_INTS:
            return pdc.getTechFactory().getPowerIntToIntsBuildRoot();
         case INT_44_TRIE_NODE_DATA:
            return pdc.getTechFactory().getFastNodeDataRoot();
         case INT_45_TRIE_NODE_DATA_CHAR:
            return pdc.getTechFactory().getFastNodeDataCharRoot();
         case INT_55_TRIE_DATA_TYPE:
            return pdc.getTechFactory().getPowerTrieLinkRoot();
         default:
            break;
      }
      return bom;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "PowerFactory");
      toStringPrivate(dc);
      for (int i = 0; i < 100; i++) {
         ByteObjectManaged bom = switchIntIDGetRootTech(i);
         if (bom != null) {
            dc.nl();
            dc.append(i + " ");
            bom.toString1Line(dc.newLevel());
         }
      }
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerFactory");
      toStringPrivate(dc);
   }

   public UCtx toStringGetUCtx() {
      return pdc.getUCtx();
   }
   //#enddebug

}
