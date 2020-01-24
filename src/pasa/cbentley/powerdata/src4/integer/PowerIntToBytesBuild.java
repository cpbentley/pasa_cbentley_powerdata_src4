package pasa.cbentley.powerdata.src4.integer;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.ctx.UCtx;
import pasa.cbentley.core.src4.io.BADataIS;
import pasa.cbentley.core.src4.io.BADataOS;
import pasa.cbentley.core.src4.io.BAByteIS;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * 
 * @author Charles Bentley
 *
 */
public class PowerIntToBytesBuild extends PowerIntToBytes {

   private byte[][] data;

   private int[]    flags;

   private int      base;

   public PowerIntToBytesBuild(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
   }

   private void initEmptyConstructor() {
      base = get4(ITB_OFFSET_02_BASE4);
   }

   protected byte[] insideGetBytes(int rid) {
      int idIndex = getIDIndex(rid);
      return data[idIndex];
   }

   private int getID(int idIndex) {
      return idIndex + base;
   }

   /**
    * 
    */
   public int addBytes(byte[] b) {

      return 0;
   }

   public void setBytes(int id, byte[] b) {
      if (b != null) {
         setBytes(id, b, 0, b.length);
      } else {
         setBytes(id, null, 0, 0);
      }
   }

   public void setBytes(int id, byte[] b, int offset, int len) {
      int idIndex = getIDIndex(id);
      byte[] bb = null;
      if (b != null) {
         bb = new byte[len];
         System.arraycopy(b, offset, bb, 0, len);
      }
      data[idIndex] = bb;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public byte[] serializeRaw() {
      BADataOS bos = new BADataOS(pdc.getUCtx());
      bos.writeInt(data.length);
      for (int i = 0; i < data.length; i++) {
         if (flags[i] == 0) {
            if (data[i] == null) {
               bos.write(1);
            } else {
               bos.write(1);
               bos.write(data[i]);
            }
         } else {
            bos.write(2);
         }
      }
      return super.serializeRawHelper(bos.getOut().toByteArray());
   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return bc.serializeToUpdateAgentData(serializeRaw());
   }

   public void serializeReverse() {
      if (hasData()) {
         UCtx uc = getUCtx();
         byte[] d = this.getByteObjectData();
         int offset = this.getDataOffsetStartLoaded();
         int len = this.getLength();
         BADataIS dis = new BADataIS(uc, new BAByteIS(uc, d, offset, len));
         int s = dis.readInt();
         data = new byte[s][];
         flags = new int[s];
         for (int i = 0; i < s; i++) {
            flags[i] = dis.readByte();
            if (flags[i] == 0) {
               data[i] = dis.readByteArray();
            }
         }
         initEmptyConstructor();
      } else {
         initEmptyConstructor();
      }
   }

   public void getBytes(int rid, byte[] b, int offset, int len) {
      int index = getIDIndex(rid);
      byte[] bb = data[index];
      System.arraycopy(bb, 0, b, offset, len);
   }

   private int getIDIndex(int rid) {
      return rid - base;
   }

   public int addBytes(byte[] b, int offset, int len) {
      data = pdc.getUCtx().getMem().increaseCapacity(data, 1);
      int id = data.length - 1;
      data[id] = b;
      increment(ITB_OFFSET_04_NUM_ELEMENTS4, 4, 1);
      increment(ITB_OFFSET_03_NEXT4, 4, 1);
      return getIDIndex(id);
   }

   public void deleteBytes(int rid) {
      int idIndex = getIDIndex(rid);
      flags[idIndex] = 2;
   }

   public int getSize() {
      // TODO Auto-generated method stub
      return 0;
   }

   //#mdebug
   public void toString(Dctx dc) {
      dc.root(this, "PowerIntToBytesBuild");
      toStringPrivate(dc);
      super.toString(dc.sup());
      int start = base;
      int end = get4(ITB_OFFSET_03_NEXT4);
      for (int rid = start; rid < end; rid++) {
         byte[] b = getBytes(rid);
         dc.nl();
         dc.append(rid + " = " + pdc.getUCtx().getBU().debugString(b, 0, ","));
      }
   }

   private void toStringPrivate(Dctx dc) {

   }

   public void toString1Line(Dctx dc) {
      dc.root1Line(this, "PowerIntToBytesBuild");
      toStringPrivate(dc);
      super.toString1Line(dc.sup1Line());
   }

   //#enddebug

}
