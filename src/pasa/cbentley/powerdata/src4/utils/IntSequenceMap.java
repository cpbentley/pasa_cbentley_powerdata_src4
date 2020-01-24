package pasa.cbentley.powerdata.src4.utils;

import pasa.cbentley.byteobjects.src4.core.BOModuleAbstract;
import pasa.cbentley.byteobjects.src4.ctx.BOCtx;
import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerIntArrayOrderToInt;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;
import pasa.cbentley.powerdata.src4.integer.PowerOrderedIntToIntBuf;

public class IntSequenceMap extends TableTrieFunction {

   private IPowerIntArrayOrderToInt map;

   public IntSequenceMap(PDCtx pdc) {
      super(pdc);
      map = new PowerOrderedIntToIntBuf(pdc);
   }

   public int getTrieID(char[] c, int offset, int len) {
      return getTrieID(c[offset]);
   }

   public int getTrieID(String s) {
      return getTrieID(s.charAt(0));
   }

   public int getTrieID(char c) {
      if (StringUtils.isLowerCase(c)) {
         return map.getValueOrderCount(c & 0xFF);
      } else {
         return 0;
      }
   }

   public int getMax() {
      return 0;
   }

}
