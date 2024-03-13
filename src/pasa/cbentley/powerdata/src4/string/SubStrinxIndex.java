package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObject;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechCharCol;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSearchTrie;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.trie.PowerCharTrie;

public class SubStrinxIndex extends PowerBuildBase {

   public IPowerCharCollector charco;

   /**
    * Disable words. we want just leaves.
    * Disable parent linking
    */
   public PowerCharTrie       trie;

   public PowerCharTrie       suffix;


   public SubStrinxIndex(PDCtx pdc) {
      super(pdc, pdc.getTechFactory().getSubStrinxIndexTechDefault());

      ByteController bc = getTech().getByteController();

      suffix = new PowerCharTrie(pdc, pdc.getTechFactory().getTrieTech(bc));
      //assign new reference ids
      trie = new PowerCharTrie(pdc, pdc.getTechFactory().getTrieTech(bc));
   }

   /**
    * For each suffix
    */
   public void buildIndex() {

      int minSize = 4;
      IntToStrings its = new IntToStrings(pdc.getUC(), 1);
      IPowerEnum pe = charco.getEnumOnCharCol(IPowerCharCollector.ENUM_TYPE_3_INTSTRINGS, its);
      while (pe.hasNext()) {
         pe.getNext();
         String str = its.strings[0];
         int pointer = its.ints[0];
         int iter = str.length() - minSize;
         for (int i = str.length() - 1; i >= minSize; i--) {
            //take a suffix
            int diff = str.length() - i;
            for (int j = 0; j < diff; j++) {
               int beginIndex = j + 1;
               int endIndex = beginIndex + i;
               String mstr = str.substring(beginIndex, endIndex);
               String pref = str.substring(0, beginIndex);
               System.out.println(pref + "_" + mstr);
               trie.addChars(mstr);

            }
         }
         char[] ar = StringUtils.reverse(str);
         suffix.addChars(ar, 0, ar.length);
      }
   }

   public IntToStrings search(String sub) {
      ByteObject searchSettings = pdc.getTechFactory().getTrieSearchSessionTechDefault();
      //prefix search
      searchSettings.set1(ITechSearchTrie.SEARCH_CHAR_OFFSET_03_SEARCH_TYPE1, ITechCharCol.SEARCH_0_PREFIX);
      //on leaves only
      searchSettings.set1(ITechSearchTrie.SEARCH_TRIE_OFFSET_02_TYPE1, ITechSearchTrie.SEARCTRIE_TYPE_1_LEAVES);
      //search longest prefix that are different (leaves)
      IntToStrings its = trie.search(searchSettings).searchWait(sub);
      System.out.println(its);
      IntToStrings finalIts = new IntToStrings(getUCtx());
      for (int i = 0; i < its.nextempty; i++) {
         String rev = StringUtils.reverseStr(its.strings[i]);

         IntToStrings sits = suffix.searchPrefix(0).searchWait(rev);
         System.out.println(sits);
         for (int j = 0; j < sits.nextempty; j++) {
            sits.strings[j] = StringUtils.reverseStr(sits.strings[j]);
         }
         finalIts.add(sits);
      }
      for (int i = 0; i < finalIts.nextempty; i++) {
         int pointer = charco.getPointer(finalIts.strings[i]);
         finalIts.ints[i] = pointer;
      }
      return finalIts;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }
}
