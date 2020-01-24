package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.core.src4.helpers.StringBBuilder;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.IPowerEnum;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

public class CharColUtilz {

   protected final PDCtx pdc;

   public CharColUtilz(PDCtx pdc) {
      this.pdc = pdc;
      
   }
   /**
    * Get locks
    * @param cc
    * @param charp
    * @return
    */
   public static char[] get(IPowerCharCollector cc, int[] charp) {
      int size = 0;
      for (int i = 0; i < charp.length; i++) {
         size += cc.getLen(charp[i]);
      }
      char[] val = new char[size];
      int off = 0;
      for (int i = 0; i < charp.length; i++) {
         off += cc.copyChars(charp[i], val, off);
      }
      return val;
   }

   public  String toStringContent(IPowerCharCollector c) {
      return toStringContent(c, "\n\t", 10);
   }

   public String toStringContent(IPowerCharCollector c, String nl, int numRow) {
      StringBBuilder sb = new StringBBuilder(pdc.getUCtx());
      IPowerEnum e = c.getEnumOnCharCol(IPowerCharCollector.ENUM_TYPE_0_STRING, null);
      int count = 0;
      while (e.hasNext()) {
         String str = (String) e.getNext();
         sb.append(str);
         count++;
         if (count > numRow) {
            sb.append(nl);
            count = 0;
         } else {
            sb.append(" ");
         }
      }
      return sb.toString();
   }

}
