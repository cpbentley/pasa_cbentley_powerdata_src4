package pasa.cbentley.powerdata.src4.utils;

import pasa.cbentley.core.src4.utils.StringUtils;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * 
 * function to c -> talbe ID
 * Inverse function is not mandatory
 * return 0 for Uppercase letters
 * <br>
 * Dataless
 * @author Charles Bentley
 *
 */
public class LetterIndex256Map extends TableTrieFunction {

   public LetterIndex256Map(PDCtx pdc) {
      super(pdc);
   }

   public int getTrieID(char[] c, int offset, int len) {
      return getTrieID(c[offset]);
   }

   public int getTrieID(String s) {
      return getTrieID(s.charAt(0));
   }

   public int getTrieID(char c) {
      if (StringUtils.isLowerCase(c)) {
         return c & 0xFF;
      } else {
         return 1;
      }
   }

   public int getMax() {
      return 256;
   }

}
