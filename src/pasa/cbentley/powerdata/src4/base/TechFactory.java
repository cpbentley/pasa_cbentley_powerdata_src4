package pasa.cbentley.powerdata.src4.base;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.logging.IStringable;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerDataTypes;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerIntArrayOrdered;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToBytes;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkOrderedIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharAggregate;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColLocale;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharColRun;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharTrie;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharTrieTable;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechFastNodeData;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntPowerArray;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToBytesRun;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToInt;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToInts;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsBuild;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRun;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechIntToIntsRunBytes;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechMorph;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechNodeData;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechOrderedIntToIntRun;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechPointerStruct;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchChar;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchTrie;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechStringLinker;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechTrieLink;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechUserTrie;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToBytes;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToInt;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToIntArray;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkTrieData;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerCharTrie;
import pasa.cbentley.powerdata.src4.ctx.BOPowerDataModule;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.trie.PowerCharTrie;
import pasa.cbentley.powerdata.src4.trie.PowerTrieLink;
import pasa.cbentley.powerdata.src4.utils.LetterIndex256Map;

/**
 * Helper methods to create {@link ByteObject} headers for this module classes.
 * 
 * Since Factory creates, it is also able to debug those headers
 * @author Charles Bentley
 *
 */
public class TechFactory implements IStringable, ITechFastNodeData, ITechCharTrieTable, ITechCharAggregate, ITechCharColRun, ITechCharColLocale, ITechIntToIntsBuild, ITechIntToBytesRun, ITechIntToIntBuild, ITechIntPowerArray, ITechStringLinker, ITechOrderedIntToIntRun, ITechCharColBuild,
      ITechSearchTrie, ITechIntToIntsRun, ITechIntToIntsRunBytes, ITechTrieLink, ITechUserTrie, ITechSearchChar, ITechIntToInts, ITechCharTrie {

   private PDCtx   pdc;

   private Morpher morpher;

   public TechFactory(PDCtx pdc) {
      this.pdc = pdc;

   }

   public ByteObjectManaged getPowerIntToIntsRunRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerIntToIntsRunRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntsRunRoot() {
      int hSize = ITIS_RUN_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_29_INT_TO_INTS_RUN;
      int intID = IPowerLinkIntToInts.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      return bo;
   }

   /**
    * Empty Techs. So all objects will use the default techs.
    * @param mod
    * @return
    */
   public ByteObjectManaged getUserTrieRoot() {
      int size = USERTRIE_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_104_USER_TRIE;
      int intid = IPowerDataTypes.INT_23_CHAR_TRIE;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   public ByteObjectManaged getSubStrinxIndexRoot() {
      int size = 50;
      int classid = IPowerDataTypes.CLASS_TYPE_65_NODE_DATA_FAST;
      int intid = IPowerDataTypes.INT_44_TRIE_NODE_DATA;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   public ByteObjectManaged getSubStrinxIndexTechDefault() {
      ByteObjectManaged bom = getSubStrinxIndexRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bom);
      return bom;
   }

   public ByteObjectManaged getTrieTech(ByteController bc) {
      ByteObjectManaged charcoTech = getPowerCharColBuildTechRoot(bc);
      ByteObjectManaged nodeTech = getFastNodeDataCharTechRoot(bc);
      nodeTech.setFlag(ITechNodeData.NODEDATA_OFFSET_01_FLAG, ITechNodeData.NODEDATA_FLAG_1_PARENT, false);
      ByteObjectManaged trieTech = getPowerCharTrieTechDefault(bc, nodeTech, charcoTech);
      return trieTech;
   }

   public ByteObjectManaged getUserTrieTechRoot(ByteController bc) {
      ByteObjectManaged bom = getUserTrieRoot();
      bc.addAgent(bom);
      return bom;
   }

   /**
    * Use the same linkTech for all
    * @param boLink
    * @param linkTech Template
    * @param numTypes
    */
   private void addArrayTech(ByteObjectManaged boLink, ByteObjectManaged[] linkTech) {
      boLink.set2(TRIEDATA_OFFSET_02_NUMTYPES2, linkTech.length);
      int dynSize = boLink.getLengthDynHeader();
      boLink.set2(TRIEDATA_OFFSET_04_REF_LINKER2, dynSize);
      int dynOffset = boLink.getLengthStaticHeader() + dynSize;
      boLink.incrementDynHeader(linkTech.length * 2);
      for (int i = 0; i < linkTech.length; i++) {
         ByteObjectManaged tech = linkTech[i];
         if (tech.getByteController() == null) {
            boLink.getByteController().addAgent(tech);
         } else if (tech.getByteController() != boLink.getByteController()) {
            throw new IllegalArgumentException();
         }
         int ref = tech.getIDRef();
         boLink.set2(dynOffset, ref);
         dynOffset += 2;
      }
   }

   /**
    * 1 type of data
    * @param mod
    * @return
    */
   public ByteObjectManaged getPowerTrieLinkRoot() {
      return getPowerTrieLinkRoot(1);
   }

   public ByteObjectManaged getPowerTrieLinkRoot(int num) {
      int size = TRIEDATA_BASIC_SIZE + (AGENT_REFID_BYTE_SIZE * (num - 1));
      int classid = IPowerDataTypes.CLASS_TYPE_103_POWER_TRIE_DATA;
      int intid = IPowerLinkTrieData.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      bo.set2(TRIEDATA_OFFSET_02_NUMTYPES2, num);
      return bo;
   }

   /**
    * With a {@link ByteController}.
    * @param mod
    * @return
    */
   public ByteObjectManaged getPowerTrieLinkTechDefault() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerTrieLinkTechRoot(bc);
      ByteObjectManaged boTrie = getPowerCharTrieTechDefault(bc);
      ByteObjectManaged boLink = getPowerLinkStringToIntArrayTechDefault(bc);
      boLink.set2(ITechStringLinker.LINKER_OFFSET_03_REF_CHARCO2, boTrie.getIDRef());

      bo.set2(TRIEDATA_OFFSET_03_REF_TRIE2, boTrie.getIDRef());
      bo.set2(TRIEDATA_OFFSET_04_REF_LINKER2, boLink.getIDRef());

      return bo;
   }

   public ByteObjectManaged getPowerTrieLinkTechDefault(PDCtx pdc, int numTypes) {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged boLink = getPowerTrieLinkTechRoot(bc, numTypes);
      ByteObjectManaged boTrie = getPowerCharTrieTechDefault(bc);
      boLink.set2(TRIEDATA_OFFSET_03_REF_TRIE2, boTrie.getIDRef());
      ByteObjectManaged[] techs = new ByteObjectManaged[numTypes];
      for (int i = 0; i < techs.length; i++) {
         techs[i] = getPowerLinkStringToIntArrayTechDefault(bc);
      }
      addArrayTech(boLink, techs);
      return boLink;

   }

   /**
    * From the Tech of a Trie, create a {@link PowerTrieLink} tech.
    * 
    * @param trie
    * @return
    */
   public ByteObjectManaged getPowerTrieLinkTechFromTrie(ByteObjectManaged trieTech) {
      ByteController bc = trieTech.getByteController();
      ByteObjectManaged arrayTech = bc.getFactory().createRootTech(IPowerLinkStringToIntArray.INT_ID);
      return getPowerTrieLinkTechFromTrieTech(bc, trieTech, arrayTech, 1);
   }

   /**
    * 
    * @param bc
    * @param trieTech
    * @return
    */
   public ByteObjectManaged getPowerTrieLinkTechFromTrieTech(ByteController bc, ByteObjectManaged trieTech, ByteObjectManaged linkTech, int numTypes) {
      if (linkTech.getByteController() != null) {
         throw new IllegalArgumentException();
      }
      ByteObjectManaged boLink = getPowerTrieLinkTechRoot(bc, numTypes);
      boLink.set2(TRIEDATA_OFFSET_03_REF_TRIE2, trieTech.getIDRef());
      bc.addAgent(trieTech);
      ByteObjectManaged[] techs = new ByteObjectManaged[numTypes];
      for (int i = 0; i < techs.length; i++) {
         techs[i] = linkTech.cloneBOMHeader();
         bc.addAgent(techs[i]);
      }
      addArrayTech(boLink, techs);
      linkTech.set2(ITechStringLinker.LINKER_OFFSET_03_REF_CHARCO2, trieTech.getIDRef());
      return boLink;
   }

   public ByteObjectManaged getPowerTrieLinkTechFromTrieTech(ByteController bc, int numTypes) {
      ByteObjectManaged trieTech = bc.getFactory().createRootTech(IPowerCharTrie.INT_ID);
      ByteObjectManaged arrayTech = bc.getFactory().createRootTech(IPowerLinkStringToIntArray.INT_ID);
      return getPowerTrieLinkTechFromTrieTech(bc, trieTech, arrayTech, numTypes);
   }

   /**
    * With a {@link ByteController}
    * @param bc
    * @return
    */
   public ByteObjectManaged getPowerTrieLinkTechRoot(ByteController bc) {
      return getPowerTrieLinkTechRoot(bc, 1);
   }

   public ByteObjectManaged getPowerTrieLinkTechRoot(ByteController bc, int num) {
      ByteObjectManaged bo = getPowerTrieLinkRoot();
      bc.addAgent(bo);
      return bo;
   }

   /**
    * a Trie Table with {@link LetterIndex256Map} function.
    * @param mod
    * @param trieTech
    * @return
    */
   public ByteObjectManaged getPowerCharTrieTableTechDefault(PDCtx pdc, ByteObjectManaged trieTech) {
      int size = CTRIETABLE_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_102_POWER_CHAR_TRIE_TABLE;
      int intid = IPowerDataTypes.INT_23_CHAR_TRIE;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      bo.addByteObject(trieTech);
      return bo;
   }

   public ByteObjectManaged getPowerCharTrieTableRoot() {
      int size = CTRIETABLE_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_102_POWER_CHAR_TRIE_TABLE;
      int intid = IPowerDataTypes.INT_23_CHAR_TRIE;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   public ByteObjectManaged getPowerCharTrieTableTechRoot(ByteController bc) {
      ByteObjectManaged bom = getPowerCharTrieTableRoot();
      bc.addAgent(bom);
      return bom;
   }

   public ByteObjectManaged getPowerCharColAggregateRoot() {
      int hSize = CHARCOL_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_46_CHAR_COL_AGGREGATE;
      int intID = IPowerDataTypes.INT_22_CHAR_COLLECTOR;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      return bo;
   }

   public ByteObjectManaged getPowerCharColAggregateTechRoot(ByteController bc) {
      ByteObjectManaged bo = getPowerCharColAggregateRoot();
      bc.addAgent(bo);
      return bo;
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * @return ByteObjectManaged
    */
   public ByteObjectManaged getPowerCharColAggregateTechRoot() {
      ByteObjectManaged bo = getPowerCharColAggregateRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bo);
      return bo;
   }

   public BOCtx getBOC() {
      return pdc.getBoc();
   }

   public ByteObjectManaged getCharCollectorRunLightRoot() {
      int hSize = ITechCharColRun.CHAR_RUN_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_45_CHAR_COL_RUN_LIGHT;
      int intID = IPowerDataTypes.INT_22_CHAR_COLLECTOR;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);

      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, false);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);
      return bo;
   }

   public ByteObjectManaged getCharCollectorRunLightTechDefault() {
      return getCharCollectorRunLightTechRoot();
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * @return ByteObjectManaged
    */
   public ByteObjectManaged getCharCollectorRunLightTechRoot() {
      ByteObjectManaged bo = getCharCollectorRunLightRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bo);
      return bo;
   }

   //#enddebug

   public ByteObject getCharSearchSessionTechDefault() {
      ByteObject bo = new ByteObject(getBOC(), IPowerDataTypes.OBJECT_120_SEARCH_CHAR, ITechSearchTrie.SEARCH_CHAR_BASIC_SIZE);
      bo.setFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_1_RETURN_FIRST_CAP, true);
      return bo;
   }

   /**
    * No {@link ByteController}
    * @param mod
    * @return
    */
   public ByteObjectManaged getFastNodeDataCharRoot() {
      int size = FAST_NODE_BUILD_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_66_NODE_DATA_FAST_CHAR;
      int intid = IPowerDataTypes.INT_45_TRIE_NODE_DATA_CHAR;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      bo.set4(NODEDATA_OFFSET_07_INIT_BUFFER_LOAD4, 1000);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);

      return bo;
   }

   public ByteObjectManaged getFastNodeDataCharTechDefault() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bom = getFastNodeDataCharTechRoot(bc);
      return bom;
   }

   public ByteObjectManaged getFastNodeDataCharTechRoot(ByteController bc) {
      ByteObjectManaged bom = getFastNodeDataCharRoot();
      bc.addAgent(bom);
      return bom;
   }

   public ByteObjectManaged getFastNodeDataRoot() {
      int size = FAST_NODE_BUILD_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_65_NODE_DATA_FAST;
      int intid = IPowerDataTypes.INT_44_TRIE_NODE_DATA;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      bo.set4(NODEDATA_OFFSET_07_INIT_BUFFER_LOAD4, 1000);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);

      return bo;
   }

   public ByteObjectManaged getFastNodeDataTechDefault() {
      ByteObjectManaged bom = getFastNodeDataRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bom);
      return bom;
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * No {@link ByteController}
    * <br>
    * @return ByteObjectManaged
    */
   public ByteObjectManaged getPowerCharColBuildRoot() {
      int hSize = CHAR_BUILD_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_44_CHAR_COL_BUILD;
      int intID = IPowerDataTypes.INT_22_CHAR_COLLECTOR;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.setFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_5_CONTINUOUS_POINTERS, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, true);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);
      bo.set4(PS_OFFSET_04_START_POINTER4, 1);
      return bo;
   }

   public ByteObjectManaged getPowerCharColBuildTechIndex() {
      return getPowerLinkStringHashtableBuildRoot();
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * @return ByteObjectManaged
    */
   public ByteObjectManaged getPowerCharColBuildTechRoot() {
      ByteObjectManaged bo = getPowerCharColBuildRoot();
      ByteController bc = new ByteController(pdc.getBoc(), pdc.getPowerFactory(), bo);
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerCharColBuildTechRoot(ByteController bc) {
      ByteObjectManaged bo = getPowerCharColBuildRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerCharColBuildTechWithIndex(ByteController bc) {
      ByteObjectManaged bo = getPowerCharColBuildTechRoot();
      bc.addAgent(bo);
      bo.setFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_2_FAST_STRING_INDEX, true);
      bo.setFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_4_INDEX_SERIALIZED, true);
      //ask controller
      bc.createAgent(IPowerLinkStringToBytes.INT_ID);
      return bo;
   }

   public ByteObjectManaged getPowerCharColLocaleRoot(int num) {
      int size = LOCALE_CC_BASIC_SIZE + (AGENT_REFID_BYTE_SIZE * (num - 1));
      int classid = IPowerDataTypes.CLASS_TYPE_103_POWER_TRIE_DATA;
      int intid = IPowerLinkTrieData.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * @return ByteObjectManaged
    */
   protected ByteObjectManaged getPowerCharRunRoot() {
      int hSize = ITechCharColRun.CHAR_RUN_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_43_CHAR_COL_RUN;
      int intID = IPowerDataTypes.INT_22_CHAR_COLLECTOR;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);

      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, false);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);
      return bo;
   }

   public ByteObjectManaged getPowerCharRunTechDefault() {
      return getPowerCharRunTechRoot();
   }

   /**
    * Creates the static header by filling static data related to the class behavior.
    * <br>
    * @return ByteObjectManaged
    */
   public ByteObjectManaged getPowerCharRunTechRoot() {
      ByteObjectManaged bo = getPowerCharRunRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bo);
      return bo;
   }

   /**
    * Creates a {@link ByteController}.
    * <br>
    * 
    * {@link PowerCharTrie} implements 2 interfaces
    * <li> {@link IPowerCharTrie}
    * <li> {@link IPowerCharCollector}
    * <br>
    * They are thus coded
    * <br>
    * @param mod
    * @param nodeTech
    * @param charcoTech
    * @return
    */
   public ByteObjectManaged getPowerCharTrieRoot() {
      int size = CTRIE_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_100_POWER_CHAR_TRIE;
      //
      int intid = ByteObjectManaged.interfaceCode(2, ITechCharTrie.CTRIE_OFFSET_09_INTID_TRIE2);
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);

      bo.set2(ITechCharTrie.CTRIE_OFFSET_09_INTID_TRIE2, IPowerCharTrie.INT_ID);
      bo.set2(ITechCharTrie.CTRIE_OFFSET_10_INTID_CHAR2, IPowerCharCollector.INT_ID);

      bo.set1(ITechCharTrie.CTRIE_OFFSET_03_MAX_CHARS_PER_NODE1, 10);

      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, true);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);

      return bo;
   }

   /**
    * The default configuration of a {@link PowerCharTrie}.
    * <br>
    * <li> fast read
    * <li> fast write
    * <li> stable
    * <br>
    * Default class identification is {@link IPowerDataTypes#CLASS_TYPE_100_POWER_CHAR_TRIE}
    * It uses 
    * <br>
    * Creates a {@link ByteController}.
    * <br>
    * @return {@link ITechCharTrie}
    */
   public ByteObjectManaged getPowerCharTrieTechDefault() {
      //sets the factory
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bom = getPowerCharTrieTechDefault(bc);
      return bom;
   }

   /**
    * 
    * @param bc
    * @return
    */
   public ByteObjectManaged getPowerCharTrieTechDefault(ByteController bc) {

      //System.out.println(bo);
      ByteObjectManaged nodeTech = getFastNodeDataCharTechRoot(bc);
      //we want a fast class type
      nodeTech.setFlag(ITechFastNodeData.NODEDATA_OFFSET_01_FLAG, ITechNodeData.NODEDATA_FLAG_1_PARENT, true);
      nodeTech.set1(ITechFastNodeData.NODEDATA_OFFSET_03_NUM_FLAGS1, 1);
      nodeTech.set4(ITechFastNodeData.NODEDATA_OFFSET_07_INIT_BUFFER_LOAD4, 1000);
      //

      ByteObjectManaged charcoTech = getPowerCharColBuildTechRoot(bc);
      charcoTech.set4(ITechCharColBuild.PS_OFFSET_04_START_POINTER4, 1);
      nodeTech.set2(ITechFastNodeData.FAST_NODE_OFFSET_REF_ID2, charcoTech.getIDRef());

      return getPowerCharTrieTechDefault(bc, nodeTech, charcoTech);

   }

   public ByteObjectManaged getPowerCharTrieTechDefault(ByteController bc, ByteObjectManaged nodeTech, ByteObjectManaged charcoTech) {
      ByteObjectManaged root = getPowerCharTrieTechRoot(bc);
      root.set2(CTRIE_OFFSET_07_REF_TRIEDATA2, nodeTech.getIDRef());
      root.set2(CTRIE_OFFSET_06_REF_CHARCOL2, charcoTech.getIDRef());
      boolean stable = nodeTech.hasFlag(PS_OFFSET_01_FLAG, PS_FLAG_1_STABLE);
      root.setFlag(PS_OFFSET_01_FLAG, PS_FLAG_1_STABLE, stable);
      return root;
   }

   public ByteObjectManaged getPowerCharTrieTechRoot(ByteController bc) {
      ByteObjectManaged bom = getPowerCharTrieRoot();
      bc.addAgent(bom);
      return bom;
   }

   /**
    * 
    * @param bc
    * @param nodeTech
    * @return
    */
   public ByteObjectManaged getPowerCharTrieTechWithNode(ByteController bc, ByteObjectManaged nodeTech) {

      bc.addAgent(nodeTech);
      ByteObjectManaged charcoTech = getPowerCharColBuildTechRoot(bc);
      charcoTech.set4(ITechCharColBuild.PS_OFFSET_04_START_POINTER4, 1);

      return getPowerCharTrieTechDefault(bc, nodeTech, charcoTech);
   }

   public ByteObjectManaged getPowerIntArrayBuildRoot() {
      int size = ITI_BUILD_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_32_INT_ARRAY_BUILD;
      int intid = IPowerIntArrayOrdered.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   public ByteObjectManaged getPowerIntArrayBuildRootTech() {
      ByteController bc = new ByteController(pdc.getBoc());
      ByteObjectManaged bo = getPowerIntArrayBuildRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerIntArrayRunRoot() {
      int size = ARRAY_INT_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_31_INT_ARRAY_RUN;
      int intid = IPowerIntArrayOrdered.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      return bo;
   }

   public ByteObjectManaged getPowerIntToBytesRunRoot() {
      int hSize = ITBR_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_25_INT_TO_BYTES_RUN;
      int intID = IPowerLinkIntToBytes.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.set4(ITB_OFFSET_02_BASE4, 0);
      return bo;
   }

   public ByteObjectManaged getPowerIntToBytesRunRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerIntToBytesRunRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntBuildRoot() {
      int hSize = ITI_BUILD_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_22_INT_TO_INT_CABLE_BUILD;
      int intID = IPowerLinkIntToInt.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.set4(PS_OFFSET_05_END_POINTER4, 10);
      bo.setFlag(PS_OFFSET_01_FLAG, PS_FLAG_5_GROWTH_TOP, true);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntBuildRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerIntToIntBuildRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntsBuildRoot() {
      int hSize = ITIS_BUILD_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_30_INT_TO_INTS_BUILD;
      int intID = IPowerLinkIntToInts.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.set2(ITIS_OFFSET_03_COL_INIT_SIZE2, 10);
      bo.set2(ITIS_OFFSET_04_ROW_INIT_SIZE2, 10);
      bo.set4(PS_OFFSET_05_END_POINTER4, 10);
      bo.setFlag(PS_OFFSET_01_FLAG, PS_FLAG_5_GROWTH_TOP, true);
      bo.setFlag(PS_OFFSET_01_FLAG, PS_FLAG_6_REFERENCE, true);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntsBuildRootTech() {
      ByteObjectManaged bo = getPowerIntToIntsBuildRoot();
      ByteController bc = new ByteController(getBOC());
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntsRunBytesRoot() {
      int hSize = ITIS_RUNBYTES_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_28_INT_TO_INTS_BYTESRUN;
      int intID = IPowerLinkIntToInts.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      return bo;
   }

   public ByteObjectManaged getPowerIntToIntsRunBytesRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerIntToIntsRunBytesRoot();
      bc.addAgent(bo);
      return bo;
   }

   /**
    * Index Tech Default
    * @param mod
    * @return
    */
   public ByteObjectManaged getPowerLinkStringHashtableBuildRoot() {
      //
      int hSize = ITechPointerStruct.PS_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_90_STRINGLINKER_HASHTABLE;
      int intID = IPowerDataTypes.INT_25_LINK_STRING;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, true);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringHashtableBuildTechDef() {
      ByteObjectManaged bo = getPowerLinkStringHashtableBuildRoot();
      ByteController bc = new ByteController(pdc.getBoc(), pdc.getPowerFactory(), bo);
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringHashtableBuildTechRoot(ByteController bc) {
      ByteObjectManaged bo = getPowerLinkStringHashtableBuildRoot();
      bc.addAgent(bc);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringHashtableBuildTechRoot(ByteController bc, int refid) {
      ByteObjectManaged bo = getPowerLinkStringHashtableBuildRoot();
      bc.addNextReferenceID(bo, refid);
      return bo;
   }

   /**
    * 
    * @param mod
    * @return
    */
   public ByteObjectManaged getPowerLinkStringToIntArrayRoot() {
      int size = LINKER_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_92_STRING_TO_INTS;
      int intid = IPowerDataTypes.INT_26_LINK_STRING_TO_INTARRAY;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);

      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, true);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);

      return bo;
   }

   /**
    * Does not define a Char
    * @param bc
    * @return
    */
   public ByteObjectManaged getPowerLinkStringToIntArrayTechDefault() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerLinkStringToIntArrayTechRoot(bc);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringToIntArrayTechDefault(ByteController bc) {
      return getPowerLinkStringToIntArrayTechRoot(bc);
   }

   public ByteObjectManaged getPowerLinkStringToIntArrayTechRoot(ByteController bc) {
      ByteObjectManaged bo = getPowerLinkStringToIntArrayRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringToIntRoot() {
      int size = LINKER_BASIC_SIZE;
      int classid = IPowerDataTypes.CLASS_TYPE_91_STRING_TO_INT;
      int intid = IPowerLinkStringToInt.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(size, classid, intid);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ, true);
      bo.setFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE, true);
      bo.setFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE, true);
      return bo;
   }

   public ByteObjectManaged getPowerLinkStringToIntRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerLinkStringToIntRoot();
      bc.addAgent(bo);
      return bo;
   }

   //#enddebug

   public ByteObjectManaged getPowerLinkStringToIntTechDefaultFromCharCo(ByteObjectManaged charco) {
      ByteController bc = charco.getByteController();
      ByteObjectManaged bo = getPowerLinkStringToIntRoot();
      bc.addAgent(bo);
      bo.set2(LINKER_OFFSET_03_REF_CHARCO2, charco.getIDRef());
      return bo;
   }

   public ByteObjectManaged getPowerOrderedIntToIntBufRoot() {
      int hSize = OITI_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_35_ORDERED_INT_TO_INT_BUF;
      int intID = ByteObjectManaged.interfaceCode(2, OITI_INT1_ID2);
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      //
      bo.set2(OITI_INT1_ID2, IPowerLinkOrderedIntToInt.INT_ID);
      bo.set2(OITI_INT2_ID2, IPowerLinkIntToInt.INT_ID);

      return bo;
   }

   public ByteObjectManaged getPowerOrderedIntToIntBufRootTech() {
      ByteController bc = new ByteController(getBOC());
      ByteObjectManaged bo = getPowerOrderedIntToIntBufRoot();
      bc.addAgent(bo);
      return bo;
   }

   public ByteObjectManaged getPowerOrderedIntToIntRunRoot() {
      int hSize = OITI_RUN_BASIC_SIZE;
      int classID = IPowerDataTypes.CLASS_TYPE_34_ORDERED_INT_TO_INT_RUN;
      int intID = IPowerLinkOrderedIntToInt.INT_ID;
      ByteObjectManaged bo = getBOC().getByteObjectManagedFactory().getTechDefault(hSize, classID, intID);
      bo.set1(OITI_RUN_OFFSET_02_DATA_BITSIZE1, 0);
      //when 1. it is automatic bit size
      bo.set1(OITI_RUN_OFFSET_03_DATABITSIZE_FLAG1, 1);

      return bo;
   }

   public ByteObject getTrieSearchSessionTechDefault() {
      ByteObject bo = new ByteObject(getBOC(), IPowerDataTypes.OBJECT_121_SEARCH_TRIE, ITechSearchTrie.SEARCH_TRIE_BASIC_SIZE);
      bo.set1(SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1, ITechCharCol.SEARCH_0_PREFIX);
      return bo;
   }

   public ByteObject getTrieSearchSessionTechFramePrefix(int frame, int prefix) {
      ByteObject bo = new ByteObject(getBOC(), IPowerDataTypes.OBJECT_121_SEARCH_TRIE, ITechSearchTrie.SEARCH_TRIE_BASIC_SIZE);
      bo.set2(SEARCH_CHAR_OFFSET_02_FRAME_SIZE2, frame);
      bo.set1(SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1, prefix);
      return bo;
   }

   //#mdebug
   public String toString() {
      return Dctx.toString(this);
   }

   public void toString(Dctx dc) {
      dc.root(this, "TechFactory");
      toStringPrivate(dc);
   }

   public String toString1Line() {
      return Dctx.toString1Line(this);
   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "TechFactory");
      toStringPrivate(dc);
   }

   //#mdebug
   public void toStringFastNodeDataCharTech(Dctx sb, ByteObject tech) {
      sb.append("#ITechFastNodeDataChar");
      toStringFastNodeDataTech(sb.nLevel(), tech);
   }

   /**
    * 
    * @param nl
    * @param bo
    * @return
    */
   public void toStringFastNodeDataTech(Dctx sb, ByteObject tech) {
      sb.append("#ITechFastNodeData");
      toStringTechNode(sb.nLevel(), tech);
   }

   public UCtx toStringGetUCtx() {
      return pdc.getUCtx();
   }

   //#mdebug
   public void toStringPowerCharColBuildTech(Dctx sb, ByteObject tech) {
      sb.append("#ITechCharPowerColBuild");
      sb.append(" startPointer=" + tech.get4(PS_OFFSET_04_START_POINTER4));
      sb.append(" IndexRef= " + tech.get2(CHARCOL_OFFSET_05_INDEX_REF2));
      sb.append(" Flags=");
      if (tech.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_3_DUPLICATES)) {
         sb.append(" AllowDuplicates");
      }
      if (tech.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_2_FAST_STRING_INDEX)) {
         sb.append(" UseIndex");
      }
      if (tech.hasFlag(CHARCOL_OFFSET_01_FLAG1, CHARCOL_FLAG_4_INDEX_SERIALIZED)) {
         sb.append(" IndexSerial");
      }
      toStringTechCharCol(sb.nLevel(), tech);
   }

   /**
    * Will be called by {@link BOPowerDataModule#subToString(ByteObject, String)
    * @param tech
    * @param nl
    * @return
    */
   public void toStringPowerIntToBytesRunTech(ByteObject tech, Dctx sb) {
      sb.append("ITechIntToBytesRun");
      sb.append("Base=" + tech.get4(ITB_OFFSET_02_BASE4));
      sb.append("Base=" + tech.get4(ITB_OFFSET_03_NEXT4));
      sb.append("NumBlocks=" + tech.get4(ITBR_OFFSET_04_NUM_BLOCK3));
   }

   public void toStringPowerIntToIntsBuildTech(Dctx sb, ByteObject tech) {
      sb.append("#ITechIntToIntsBuild");
      sb.append(" EmptiesSize=" + tech.get2(ITIS_BUILD_OFFSET_02_EMPTIES_SIZE2));
      sb.append(" Empties= " + tech.hasFlag(ITIS_BUILD_OFFSET_01_FLAG1, ITIS_BUILD_FLAG_1_EMPTIES));
      toStringTechIntToInts(sb.nLevel(), tech);
   }

   public void toStringPowerStringLinkerTech(Dctx sb, ByteObject tech) {
      sb.append("#ITechStringLinker");
      sb.append(" size=" + tech.get2(LINKER_OFFSET_02_SIZE2));
      sb.append(" CharCoRef= " + tech.get2(LINKER_OFFSET_03_REF_CHARCO2));
      sb.append(" PointersRef= " + tech.get2(LINKER_OFFSET_04_REF_POINTERS2));
      toStringTechPointer(sb.nLevel(), tech);
   }

   private void toStringPrivate(Dctx dc) {

   }

   //#mdebug

   /**
    * {@link ITechCharCol}
    * @param nl
    * @param tech
    * @return
    */
   public void toStringTechCharCol(Dctx sb, ByteObject tech) {
      sb.append("#ITechCharCol");
      sb.append(" Single Plane=" + tech.hasFlag(ITechCharCol.CHARCOL_OFFSET_01_FLAG1, ITechCharCol.CHARCOL_FLAG_6_SINGLE_PLANE));
      toStringTechPointer(sb.nLevel(), tech);
   }

   public void toStringTechCharSearch(Dctx sb, ByteObject tech) {
      sb.append("#ITechCharSearch");
      sb.append(" isFirstCap=" + tech.hasFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_1_RETURN_FIRST_CAP));
      sb.append(" isThreaded=" + tech.hasFlag(SEARCH_CHAR_OFFSET_01_FLAG1, SEARCH_CHAR_FLAG_2_IS_THREADED));
      sb.nl();
      sb.append(" MaxFrameSize=" + tech.get2(SEARCH_CHAR_OFFSET_02_FRAME_SIZE2));
      tech.toStringBackUp(sb.nLevel());
   }

   //#mdebug
   public void toStringTechCharTrie(Dctx sb, ByteObject tech) {
      sb.append("#ITechCharTrie");
      sb.append(" MaxCharsPerNode=");
      sb.append(tech.get1(ITechCharTrie.CTRIE_OFFSET_03_MAX_CHARS_PER_NODE1));
      sb.append(" Height=");
      sb.append(tech.get2(ITechCharTrie.CTRIE_OFFSET_05_HEIGHT_TRIE2));
      sb.append(" ReuseChars=" + tech.hasFlag(ITechCharTrie.CTRIE_OFFSET_01_FLAG, ITechCharTrie.CTRIE_FLAG_1_REUSE_CHARS));
      toStringTechCharCol(sb.nLevel(), tech);
   }

   public void toStringTechIntToInt(Dctx sb, ByteObject tech) {
      sb.append("#ITechIntToInt");
      sb.append(" DefValue=" + tech.get4(ITechIntToInt.ITI_OFFSET_02_DEF_VALUE4));
      sb.nl();
      toStringTechPointer(sb.nLevel(), tech);
   }

   public void toStringTechIntToInts(Dctx sb, ByteObject tech) {
      sb.append("#ITechIntToInts");
      sb.append(" Duplicates=" + tech.hasFlag(ITIS_OFFSET_01_FLAG, ITIS_FLAG_2_DUPLICATES));
      sb.append(" OrderType=" + tech.get1(ITIS_OFFSET_02_ORDER_TYPE1));
      sb.append(" ColInit=" + tech.get2(ITIS_OFFSET_03_COL_INIT_SIZE2));
      sb.append(" RowInit=" + tech.get2(ITIS_OFFSET_04_ROW_INIT_SIZE2));
      toStringTechIntToInt(sb.nLevel(), tech);
   }

   /**
    * {@link ITechMorph}
    * @param nl
    * @param tech
    * @return
    */
   public void toStringTechMorph(Dctx sb, ByteObjectManaged tech) {
      sb.append("#ITechMorphTech ");
      sb.append(" FastRead=" + tech.hasFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_2_FAST_READ));
      sb.append(" FastWrite=" + tech.hasFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_3_FAST_WRITE));
      sb.append(" SmallMem=" + tech.hasFlag(ITechMorph.MORPH_OFFSET_01_FLAG, ITechMorph.MORPH_FLAG_4_SMALL_FOOT_PRINT));
      sb.append(" mode=" + tech.get1(ITechMorph.MORPH_OFFSET_02_MODE1));
      sb.append(" ref=" + tech.get2(ITechMorph.MORPH_OFFSET_03_REF2));
      sb.nl();
      getBOC().getByteObjectManagedFactory().toStringHeader(sb.nLevel(), (ByteObjectManaged) tech);
   }

   public void toStringTechNode(Dctx sb, ByteObject tech) {
      sb.append("#ITechNode");
      sb.append(" ParentPointers=");
      sb.append("" + tech.hasFlag(ITechNodeData.NODEDATA_OFFSET_01_FLAG, ITechNodeData.NODEDATA_FLAG_1_PARENT));
      int type = tech.get1(ITechNodeData.NODEDATA_OFFSET_02_TYPE1);
      if (type == ITechNodeData.NODEDATA_TYPE_0_CHAIN) {
         sb.append(" Chain" + "");
      } else {
         sb.append(" Family");
      }
      toStringTechPointer(sb.nLevel(), tech);
   }

   /**
    * {@link ITechPointerStruct}
    * @param nl
    * @param tech
    * @return
    */
   public void toStringTechPointer(Dctx sb, ByteObject tech) {
      sb.append("#ITechPointerStruct");
      sb.append(" Stable=" + tech.hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_1_STABLE));
      sb.append(" NumPointers=" + tech.get4(ITechPointerStruct.PS_OFFSET_06_NUM_POINTER4));
      sb.nl();
      sb.append(" StartP=" + tech.get4(ITechPointerStruct.PS_OFFSET_04_START_POINTER4));
      sb.append(" EndP=" + tech.get4(ITechPointerStruct.PS_OFFSET_05_END_POINTER4));
      sb.append(" GrowthBelow=" + tech.hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_4_GROWTH_BELOW));
      sb.append(" GrowthTop=" + tech.hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_5_GROWTH_TOP));
      sb.append(" References=" + tech.hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_6_REFERENCE));
      sb.append(" Copies=" + tech.hasFlag(ITechPointerStruct.PS_OFFSET_01_FLAG, ITechPointerStruct.PS_FLAG_7_COPIES));
      toStringTechMorph(sb.nLevel(), (ByteObjectManaged) tech);
   }

   //#mdebug
   public void toStringTechTrieSearch(Dctx sb, ByteObject tech) {
      sb.append("#ITechTrieSearch");
      toStringTechCharSearch(sb.nLevel(), tech);
   }

   public Morpher getMorpher() {
      if (morpher == null) {
         morpher = new Morpher(pdc);
      }
      return morpher;
   }

   //#enddebug

}
