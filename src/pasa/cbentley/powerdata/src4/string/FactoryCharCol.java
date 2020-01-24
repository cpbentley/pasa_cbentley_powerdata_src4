package pasa.cbentley.powerdata.src4.string;
//package mordan.powerdata.string;
//
//import mordan.datastruct.power.IPowerCharCollector;
//import mordan.datastruct.power.IPowerDataTypes;
//import mordan.datastruct.power.itech.ITechCharCol;
//import mordan.datastruct.power.itech.ITechMorph;
//import mordan.powerdata.trie.PowerCharTrie;
//import mordan.universal.files.BytesCounter;
//import mordan.universal.memory.BOModule;
//import mordan.universal.memory.ByteController;
//import mordan.universal.memory.ByteObject;
//import mordan.universal.memory.ByteObjectManaged;
//import mordan.universal.memory.IObjectManaged;
//import mordan.universal.utils.BitMask;
//
///**
// * 
// * @author Charles Bentley
// * @see IPowerCharCollector
// * @see PowerCharTrie
// * @see PowerCharColBuild
// */
//public class FactoryCharCol {
//
//   /**
//    * Tries to create a {@link IPowerCharCollector} from the {@link ByteObjectManaged} serialized data.
//    * <br>
//    * Could be a {@link PowerCharColBuild}, a Run
//    * <br>
//    * Returns null when data is malformed.
//    * <br>
//    * <br>
//    * The starting data must be a valid {@link ByteObject} with its serialized format
//    * @param data
//    * @param offset
//    * @return
//    */
//   public static IPowerCharCollector createCharCollector(PDCtx pdc,byte[] data, int offset) {
//      BytesCounter bc = new BytesCounter(data, offset);
//      return createCharCollector(mod,bc);
//   }
//
//   public static IPowerCharCollector createCharCollector(PDCtx pdc,BytesCounter byc) {
//      IPowerCharCollector cp = null;
//      ByteObject header = ByteObject.unwrapByteObject(mod,byc);
//      int classid = header.get2(IObjectManaged.AGENT_OFFSET_02_CLASS_ID2);
//      if (classid == IPowerDataTypes.CLASS_TYPE_44_CHAR_COL_BUILD) {
//         cp = new PowerCharColBuild(mod,header);
//      } else if(classid == IPowerDataTypes.CLASS_TYPE_100_POWER_CHAR_TRIE) {
//         cp = new PowerCharTrie(mod,header);
//      }
//      return cp;
//   }
//
//   /**
//    * <li> {@link ITechMorph#MORPH_FLAG_1_RUN}
//    * <li> {@link ITechCharCol#CHARCOL_FLAG_2_STABLE}
//    * <li> {@link ITechCharCol#CHARCOL_FLAG_3_DUPLICATES}
//    * <li> {@link ITechCharCol#CHARCOL_FLAG_2_FAST_STRING_INDEX}
//    * 
//    * @param cctype
//    * @return
//    */
//   public static IPowerCharCollector createCharCollector(PDCtx pdc,int cctype) {
//      IPowerCharCollector cc = null;
//      if (BitMask.hasFlag(cctype, ITechMorph.MORPH_FLAG_1_RUN)) {
//         cc = new CharCollectorRunLight(mod);
//      } else {
//         ByteObject tech = PowerCharColBuild.getTechDefault();
//         if (BitMask.hasFlag(cctype, ITechCharCol.CHARCOL_FLAG_2_FAST_STRING_INDEX)) {
//            tech.setFlag(ITechCharColBuild.CHAR_BUILD_OFFSET_01_FLAG1, ITechCharColBuild.CHAR_BUILD_TECH_FLAG_2_USE_INDEX, true);
//         }
//         boolean duplis = BitMask.hasFlag(cctype, ITechCharCol.CHARCOL_FLAG_3_DUPLICATES);
//         tech.setFlag(ITechCharColBuild.CHAR_BUILD_OFFSET_01_FLAG1, ITechCharColBuild.CHAR_BUILD_TECH_FLAG_1_ALLOW_DUPLICATES, duplis);
//         cc = new PowerCharColBuild(mod,tech);
//      }
//      return cc;
//   }
//
//   /**
//    * When {@link IObjectManaged#AGENT_OFFSET_02_CLASS_ID2} is specified (different than 0) use it.
//    * <br>
//    * <br>
//    * Otherwise use {@link ITechMorph} flags.
//    * <br>
//    * <br>
//    * 
//    * @param tech
//    * @return
//    */
//   public static IPowerCharCollector createCharCollector(PDCtx pdc,ByteObject tech) {
//      if (tech == null) {
//         tech = PowerCharColBuild.getTechDefault();
//      }
//      int classtype = tech.get2(IObjectManaged.AGENT_OFFSET_02_CLASS_ID2);
//      boolean isRun = tech.hasFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_4_SMALL_FOOT_PRINT);
//      if (classtype != 0) {
//         switch (classtype) {
//            case IPowerDataTypes.CLASS_TYPE_44_CHAR_COL_BUILD:
//               return new PowerCharColBuild(mod,tech);
//            case IPowerDataTypes.CLASS_TYPE_100_POWER_CHAR_TRIE:
//               return new PowerCharTrie(mod,tech);
//            default:
//               break;
//         }
//      }
//
//      //use tech flag
//      if (isRun) {
//
//      }
//      return new PowerCharColBuild(mod,tech);
//   }
//
//   /**
//    * Create a {@link IPowerCharCollector} from the {@link ByteController}
//    * <br>
//    * <br>
//    * Which class? I have no idea? The template reference must be written during the serialization.
//    * <br>
//    * <br>
//    * The {@link ByteController} looks up his memory sources sequentially for an {@link IPowerCharCollector}
//    * type.
//    * <br>
//    * @param bc
//    * @return
//    */
//   public static IPowerCharCollector createCharCollector(ByteController bc) {
//      // TODO Auto-generated method stub
//      return null;
//   }
//}
