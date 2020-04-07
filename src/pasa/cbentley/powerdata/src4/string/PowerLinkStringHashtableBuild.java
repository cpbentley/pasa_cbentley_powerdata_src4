package pasa.cbentley.powerdata.src4.string;

import java.util.Enumeration;
import java.util.Hashtable;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.io.BAByteIS;
import pasa.cbentley.core.src4.io.BADataIS;
import pasa.cbentley.core.src4.io.BADataOS;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.string.IPowerLinkStringToBytes;
import pasa.cbentley.powerdata.src4.base.PowerBuildBase;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * A simple hashtable that implemens the IStringIndex interface.
 * <br>
 * deserialization 
 * @author Charles Bentley
 *
 */
public class PowerLinkStringHashtableBuild extends PowerBuildBase implements IPowerLinkStringToBytes {

   private Hashtable ht;

   public PowerLinkStringHashtableBuild(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getPowerLinkStringHashtableBuildTechDef());
   }

   public PowerLinkStringHashtableBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      ht = new Hashtable();
   }

   public PowerLinkStringHashtableBuild(PDCtx pdc, ByteController mod, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   public byte[] find(char[] c, int offset, int len) {
      byte[] b = (byte[]) ht.get(new String(c, offset, len));
      if (b == null)
         return new byte[0];
      return b;
   }

   public int findAsInt(String s) {
      byte[] b = (byte[]) ht.get(s);
      if (b != null)
         return IntUtils.readIntBE(b, 0);
      return -1;
   }

   public Object getMorph(MorphParams p) {
      return this;
   }

   public int getSize() {
      return ht.size();
   }

   public void insertWord(char[] word, int rid) {
      insertWord(word, 0, word.length, rid);
   }

   public void insertWord(char[] word, int offset, int len, byte[] data, int numbits) {
      ht.put(new String(word, offset, len), data);
   }

   public void insertWord(char[] word, int offset, int len, int rid) {
      ht.put(new String(word, offset, len), IntUtils.byteArrayBEFromInt(rid));
   }

   public void insertWord(String word, int rid) {
      insertWord(word.toCharArray(), 0, word.length(), rid);
   }

   public void removeWord(char[] word) {
      removeWord(new String(word));
   }

   public void removeWord(char[] word, int offset, int len) {
      removeWord(new String(word, offset, len));
   }

   public void removeWord(String s) {
      ht.remove(s);
   }

   public byte[] serializeRaw() {
      int size = ht.size();
      Enumeration en = ht.keys();
      BADataOS bos = new BADataOS(pdc.getUCtx());
      bos.writeInt(size);
      while (en.hasMoreElements()) {
         String str = (String) en.nextElement();
         bos.writeChars(str);
         byte[] data = (byte[]) ht.get(str);
         bos.writeByteArray(data);
      }
      byte[] data = bos.getOut().toByteArray();
      return serializeRawHelper(data);
   }

   public void serializeReverse() {
      ht = new Hashtable();
      //read data
      byte[] data = this.getByteObjectData();
      int offset = this.getDataOffsetStartLoaded();
      int len = this.getLength();
      UCtx uc = getUCtx();
      BADataIS dis = new BADataIS(uc, new BAByteIS(uc, data, offset, len));
      int size = dis.readInt();
      //#debug
      printDataStruct("PowerLinkStringHashtableBuild#serializeReverse Size " + size);
      for (int i = 0; i < size; i++) {
         String str = dis.readString();
         byte[] sdata = dis.readByteArray();
         ht.put(str, sdata);
      }
      removeData();
   }

   public ByteObjectManaged serializeTo(ByteController byteCon) {
      return byteCon.serializeToUpdateAgentData(serializeRaw());
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerLinkStringHashtableBuild");
      toStringPrivate(dc);
      super.toString(dc.sup());
      int countOnLine = 5;
      dc.nl();
      Enumeration en = ht.keys();
      int count = 0;
      while (en.hasMoreElements()) {
         String str = (String) en.nextElement();
         dc.append(str);
         byte[] data = (byte[]) ht.get(str);
         dc.append(" " + pdc.getUCtx().getBU().debugString(data, 0, ","));
         count++;
         if (count == countOnLine) {
            dc.nl();
            count = 0;
         }
      }
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerLinkStringHashtableBuild");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
