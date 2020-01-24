package pasa.cbentley.powerdata.src4.string;

import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.structs.IntToStrings;
import pasa.cbentley.powerdata.spec.src4.power.IPowerCharCollector;
import pasa.cbentley.powerdata.spec.src4.power.integers.IPowerLinkIntToInts;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Models all the translations possible between several words
 * <br>
 * @author Charles Bentley
 *
 */
public class WordWeb extends PowerCharColAggregate {
   
   public WordWeb(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public WordWeb(PDCtx pdc, IPowerCharCollector[] cols) {
      super(pdc, pdc.getTechFactory().getPowerCharColAggregateTechRoot());
   }

   protected IPowerLinkIntToInts[][] datas;

   public IntToStrings getLinkes(String word, int srcType, int destType) {

      int pointer = types[srcType].getPointer(word);

      int[] destPointers = datas[srcType][destType].getKeyValues(pointer);

      IntToStrings its = new IntToStrings(getUCtx());
      for (int i = 0; i < destPointers.length; i++) {
         int destPointer = destPointers[i];
         String str = types[destType].getKeyStringFromPointer(destPointer);
         its.add(destPointer, str);
      }

      return its;
   }
}
