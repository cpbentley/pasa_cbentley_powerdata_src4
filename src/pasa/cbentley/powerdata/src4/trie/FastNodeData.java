package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.logging.Dctx;
import pasa.cbentley.core.src4.utils.BitCoordinate;
import pasa.cbentley.core.src4.utils.BitUtils;
import pasa.cbentley.core.src4.utils.IntUtils;
import pasa.cbentley.powerdata.spec.src4.power.IPointerUser;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechFastNodeData;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechNodeData;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNode;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNodeTopo;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * Trie Node data build for speed using the Consecutive Children strategy.. Stores everyting into integer arrays.
 * <br>
 * <br>
 * {@link ByteObjectManaged} is here only for the header and serialization.
 * <br>
 * <br>
 *  <br>
 *  
 * @author Charles Bentley
 *
 */
public class FastNodeData extends FamilyChainedNodeData implements IPowerTrieNodes, ITechFastNodeData {

   private int            arrayGrowSize;

   /**
    * Pointer to the last used index in Trie arrays. Keep in mind
    * that node 0 is unused.
    * <br>
    * <br>
    * It starts the root node which is there by construction time.
    */
   protected int          lastused   = 1;

   /**
    * Index is the offset and gives the child pointer.
    */
   protected int[]        offsetToChild;

   protected int[]        offsetToFamily;

   /**
    * Bit flags. Null when no bit flags used
    * <li>
    */
   protected int[]        offsetToFlags;

   /**
    * From the offset, get the parent.
    * <br>
    * Null when {@link ITechNodeData#NODEDATA_FLAG_1_PARENT} is not set
    * 
    */
   protected int[]        offsetToParent;

   /**
    * Will be char pointer data for char node.
    */
   protected int[]        offsetToPayload;

   /**
    * Down from First to Last.
    * <br>
    * <br>
    * Provides the link to the left child
    */
   protected int[]        offsetToRight;

   /**
    * Pointer user will be called to update the pointers
    */
   protected IPointerUser pointerUser;

   private int            statCountArrayGrow;

   private long           statTimeArrayGrowing;

   private long           statTimeCopying;

   /**
    * Index match the id 
    * <li> {@link TrieNodeTopo#TOPO_TYPE_01_NUM_NODES}
    * <li> {@link TrieNodeTopo#TOPO_TYPE_02_FAMILIES}
    * <li> {@link TrieNodeTopo#TOPO_TYPE_03_PATTERN}
    * <br>
    * <br>
    * When built by construction, trie fills in the value for all nodes in the result integer array.
    * When not built by construction and a modification occurs, toplogies are erased.
    * When a request is made via {@link IPowerTrieNodes#getTopology(TrieNodeTopo)},
    * class checks type
    */
   private TrieNodeTopo[] topologies = new TrieNodeTopo[10];

   /**
    * Array to track single value node topologies
    */
   private int[][]        topologiesToOffsetToCount;

   TrieNode               trie       = new TrieNode();

   public FastNodeData(PDCtx pdc) {
      this(pdc, pdc.getTechFactory().getFastNodeDataTechDefault());

   }

   public FastNodeData(PDCtx pdc, ByteObjectManaged tech) {
      super(pdc, tech);
      initConst();
   }

   private void initConst() {
      initArrays(get4(NODEDATA_OFFSET_07_INIT_BUFFER_LOAD4));
   }

   protected void serializeRawReverse() {
      int offset = getDataOffsetStartLoaded();
      lastused = IntUtils.readIntBE(data, offset);
      initArrays(lastused + 1);
      offset += 4;
      int bitSizeChild = data[offset++];
      int bitSizeFamily = data[offset++];
      int bitSizeFlags = data[offset++];
      int bitSizeParent = data[offset++];
      int bitSizeLoad = data[offset++];
      int bitSizeRight = data[offset++];

      //      System.out.println("#FastNodeData bitsChild=" + bitSizeChild + " bitsFamily=" + bitSizeFamily + " bitsFlags=" + bitSizeFlags + " bitsParent=" + bitSizeParent + " bitsLoad=" + bitSizeLoad
      //            + " bitSizeRight=" + bitSizeRight + " lastused=" + lastused);

      BitCoordinate c = new BitCoordinate(pdc.getUC(), offset, 0);
      c.tick();
      for (int i = 1; i <= lastused; i++) {
         offsetToChild[i] = BitUtils.readBits(data, c, bitSizeChild);
         offsetToFamily[i] = BitUtils.readBits(data, c, bitSizeFamily);
         offsetToFlags[i] = BitUtils.readBits(data, c, bitSizeFlags);
         offsetToParent[i] = BitUtils.readBits(data, c, bitSizeParent);
         offsetToPayload[i] = BitUtils.readBits(data, c, bitSizeLoad);
         offsetToRight[i] = BitUtils.readBits(data, c, bitSizeRight);
         //System.out.println(i + "=" + c.toString());
      }
      this.removeData();
   }

   public int addChild(int parentNode) {
      return addChild(parentNode, -1);
   }

   /**
    * 
    * Create a new child node insert it below parent node at the given family position.
    * <br>
    * <br>
    * If there are not enough nodes for the given index value, node is appended last in the family. 
    * <br>
    * <br>
    * 
    * returns a stable user node
    * 
    * @param parentNode
    * @param familyIndex when -1, appended in all cases.
    */
   public int addChild(int parentNode, int familyIndex) {

      int childnode = nextNode();
      int familySize = getNodeFamilySize(parentNode);
      int childp = getNodeChildFirst(parentNode);
      if (familySize == 0 || familyIndex == 0) {
         //we can simply 
         setChildrenPointer(parentNode, childnode);
      }
      if (familyIndex == -1) {
         familyIndex = familySize;
      }
      //sets the index when familyIndex is too big
      if (familyIndex > familySize) {
         familyIndex = familySize;
      }
      //at least 1 sister node
      int currentSister = childp;
      int previousSister = NOT_A_NODE;
      for (int i = 0; i <= familyIndex; i++) {
         //check 
         if (i == familyIndex) {
            //we replace the currentSister
            if (previousSister != NOT_A_NODE) {
               offsetToRight[previousSister] = childnode;
            }
            offsetToRight[childnode] = currentSister;
         }
         previousSister = currentSister;
         currentSister = getNodeSisterRight(currentSister);
      }
      //offsets follow the family pack pattern

      incrementFamSize(parentNode);

      setParentPointer(childnode, parentNode);

      return childnode;
   }

   /**
    * Sub class with specific data may extend the header here.
    * 
    * @param ar
    * @param pointer
    */
   protected void addSpecificRunHeader(byte[] ar, int pointer) {

   }

   private void arrayGrowCheck() {
      if (lastused + 3 >= offsetToChild.length) {
         long time = System.currentTimeMillis();
         statCountArrayGrow++;
         int incr = offsetToChild.length;
         arrayGrowSize += incr;
         incrementArrays(incr);
         offsetToParent = pdc.getUC().getMem().increaseCapacity(offsetToParent, incr);
         offsetToFamily = pdc.getUC().getMem().increaseCapacity(offsetToFamily, incr);
         offsetToChild = pdc.getUC().getMem().increaseCapacity(offsetToChild, incr);
         offsetToRight = pdc.getUC().getMem().increaseCapacity(offsetToRight, incr);
         offsetToFlags = pdc.getUC().getMem().increaseCapacity(offsetToFlags, incr);
         offsetToPayload = pdc.getUC().getMem().increaseCapacity(offsetToPayload, incr);
         statTimeArrayGrowing += (System.currentTimeMillis() - time);
      }
   }

   /**
    * Copy to the byte array 
    * @param ar
    * @param c
    * @param off
    */
   protected void copyCoreValues(byte[] ar, BitCoordinate c, int off) {

   }

   public void decrementFamilySize(int node) {
      if (offsetToFamily[node] > 0) {
         offsetToFamily[node]--;
      }
   }

   public int getChildDataByteSize() {
      return 0;
   }

   /**
    * Returns 0
    */
   public int getFirstNode() {
      return 1;
   }

   /**
    * the size of the header for the children class
    * @return
    */
   public int getHeaderSizeAddition() {
      return 0;
   }

   public int getLastNode() {
      return lastused;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   /**
    * 
    */
   public TrieNode getNode(int node) {
      TrieNode trie = new TrieNode();
      trie.familySize = offsetToFamily[node];
      trie.childPointer = offsetToChild[node];
      trie.parentPointer = offsetToParent[node];
      return trie;
   }

   public int getNodeChildFirst(int node) {
      return offsetToChild[node];
   }

   public int getNodeFamilySize(int node) {
      return offsetToFamily[node];
   }

   public int getNodeLast() {
      return lastused;
   }

   public int getNodeParent(int node) {
      return getParentPointer(node);
   }

   public int getNodePayload(int node) {
      return 0;
   }

   public int getNodePayLoad(int node) {
      return offsetToPayload[node];
   }

   public String getNodeSig(int node) {
      return "";
   }

   /**
    * 
    */
   public int getNodeSisterLeft(int node) {
      //read parent
      int parent = getParentPointer(node);
      int childParent = getNodeChildFirst(parent);
      int sisterLeft = NOT_A_NODE;
      while (childParent != node && childParent != 0) {
         int sisterRight = getNodeSisterRight(childParent);
         sisterLeft = childParent;
         childParent = sisterRight;
      }
      return sisterLeft;
   }

   public int getNodeSisterRight(int node) {
      return offsetToRight[node];
   }

   public int getNodesMaxFamilySize() {
      return IntUtils.getMax(offsetToFamily, 0, lastused + 1);
   }

   /**
    * At least 1. which is the root node.
    */
   public int getNumberOfNodes() {
      return lastused;
   }

   public int getParentPointer(int node) {
      return offsetToParent[node];
   }

   public int getRootNode() {
      return 1;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public void getTopology(TrieNodeTopo tnt) {
      if (topologies != null && topologies[tnt.paramType] != null && topologies[tnt.paramType].paramTypeSub == tnt.paramTypeSub) {
         topologies[tnt.paramType].setTo(tnt);
      } else {
         //compute

         NodeTopologyComputer t = new NodeTopologyComputer(pdc);
         t.reset(tnt, this);
         t.compute();
         //save result for later user
         topologies[tnt.paramType] = new TrieNodeTopo();
         tnt.setTo(topologies[tnt.paramType]);
      }
   }

   public boolean hasNodeFlag(int node, int flag) {
      return BitUtils.hasFlag(offsetToFlags[node], flag);
   }

   protected void incrementArrays(int incr) {

   }

   public void incrementFamSize(int node) {
      offsetToFamily[node]++;
   }

   protected void initArrays(int init) {
      offsetToFamily = new int[init];
      offsetToChild = new int[init];
      offsetToParent = new int[init];
      offsetToFlags = new int[init];
      offsetToPayload = new int[init];
      offsetToRight = new int[init];
   }

   public boolean isLeaf(int node) {
      return offsetToFamily[node] == 0;
   }

   /**
    * Create a new node. The node is free dangling.
    */
   public int nextNode() {
      arrayGrowCheck();
      lastused++;
      return lastused;
   }

   public int payLoadSplit(int node, int firstPL, int secondPL) {
      int parentNode = getParentPointer(node);

      int burgeonNode = nextNode();
      setChildrenPointer(burgeonNode, node);
      setFamilySize(burgeonNode, 1);
      setParentPointer(burgeonNode, parentNode);
      setNodePayLoad(burgeonNode, firstPL);
      offsetToRight[burgeonNode] = offsetToRight[node];

      setParentPointer(node, burgeonNode);
      offsetToRight[node] = 0;
      setNodePayLoad(node, secondPL);

      //we now must replace burgeonNode to the ParentNode
      int opchild = getNodeChildFirst(parentNode);
      if (opchild == node) {
         offsetToChild[parentNode] = burgeonNode;
      } else {
         int previousSister = opchild;
         int nextSister = getNodeSisterRight(previousSister);
         while (nextSister != NOT_A_NODE) {
            if (nextSister == node) {
               offsetToRight[previousSister] = burgeonNode;
               break;
            } else {
               previousSister = nextSister;
               nextSister = getNodeSisterRight(nextSister);
            }
         }
      }
      return burgeonNode;

   }

   public byte[] serializeRaw() {

      byte[] header = toByteArray();

      //printDataStruct("FastNodeData#serializePack Header Length=" + header.length);

      //encapsulates the header for easier manipulation.
      ByteObjectManaged tr = new ByteObjectManaged(pdc.getBOC(), header, 0);

      //System.out.println("FastNodeData#serializePack "+ByteObject.unwrapByteObject(header, 0).toString());
      int numElements = lastused; //the + 1 is needed
      int bitSizeChild = IntUtils.getMaxBitSize(offsetToChild, 1, numElements);
      int bitSizeFamily = IntUtils.getMaxBitSize(offsetToFamily, 1, numElements);
      int bitSizeFlags = IntUtils.getMaxBitSize(offsetToFlags, 1, numElements);
      int bitSizeParent = IntUtils.getMaxBitSize(offsetToParent, 1, numElements);
      int bitSizePayLoad = IntUtils.getMaxBitSize(offsetToPayload, 1, numElements);
      int bitSizeRight = IntUtils.getMaxBitSize(offsetToRight, 1, numElements);

      int mainDataSize = 10; //6 bytes for the bit size and last used
      int segmentBits = bitSizeChild + bitSizeFamily + bitSizeFlags + bitSizeParent + bitSizeRight + bitSizePayLoad;
      int totalBits = (numElements * segmentBits);
      int totalMainDataSize = mainDataSize + IntUtils.divideCeil(totalBits, 8);

      //look if there is old data inside. strip it away

      int totalSize = totalMainDataSize;

      tr.expandResetArrayData(totalSize); //this manages 

      byte[] data = tr.getByteObjectData();
      int offset = tr.getDataOffsetStartLoaded();

      //System.out.println("#FastNodeData#serializePack TotalSize=" + totalSize + " HeaderSize=" + header.length + " BitsPerSegment=" + segmentBits);

      IntUtils.writeIntBE(data, offset, lastused);
      offset += 4;
      data[offset++] = (byte) bitSizeChild;
      data[offset++] = (byte) bitSizeFamily;
      data[offset++] = (byte) bitSizeFlags;
      data[offset++] = (byte) bitSizeParent;
      data[offset++] = (byte) bitSizePayLoad;
      data[offset++] = (byte) bitSizeRight;

      //      System.out.println("#FastNodeData bitsChild=" + bitSizeChild + " bitsFamily=" + bitSizeFamily + " bitsFlags=" + bitSizeFlags + " bitsParent=" + bitSizeParent + " bitsLoad=" + bitSizePayLoad
      //            + " bitSizeRight=" + bitSizeRight + " lastused=" + lastused);

      BitCoordinate c = new BitCoordinate(pdc.getUC(), offset, 0);
      for (int i = 1; i <= lastused; i++) {
         BitUtils.copyBits(data, c, offsetToChild[i], bitSizeChild);
         BitUtils.copyBits(data, c, offsetToFamily[i], bitSizeFamily);
         BitUtils.copyBits(data, c, offsetToFlags[i], bitSizeFlags);
         BitUtils.copyBits(data, c, offsetToParent[i], bitSizeParent);
         BitUtils.copyBits(data, c, offsetToPayload[i], bitSizePayLoad);
         BitUtils.copyBits(data, c, offsetToRight[i], bitSizeRight);
         //System.out.println(i + "=" + c.toString());
      }

      return data;
   }

   public void serializeReverse() {
      if (hasData()) {
         serializeRawReverse();
      } else {
         initConst();
      }
   }

   /**
    * 
    */
   public ByteObjectManaged serializeTo(ByteController bc) {
      ByteObjectManaged bom = bc.serializeToUpdateAgentData(serializeRaw());
      return bom;
   }

   /**
    * Update the performance table for updating childrenPointers during an insert
    */
   public void setChildrenPointer(int node, int childpointer) {
      offsetToChild[node] = childpointer;
   }

   public void setFamilySize(int node, int val) {
      offsetToFamily[node] = val;
   }

   public void setNodeFlag(int node, int flag, boolean v) {
      offsetToFlags[node] = BitUtils.setFlag(offsetToFlags[node], flag, v);
   }

   public void setNodePayLoad(int node, int payload) {
      offsetToPayload[node] = payload;
   }

   public void setParentPointer(int node, int pointer) {
      if (node == 0) {
         throw new RuntimeException("trying to set a parent pointer to root node");
      }
      offsetToParent[node] = pointer;
   }

   public void setPointerUser(IPointerUser user) {
      pointerUser = user;
   }

   /**
    * Return the new burgeon node
    * the burgeon nodes instead of the node who becomes the kid
    * <br>
    * <br>
    * The burgeon node inherits the family of node.
    * <br>
    * <br>
    * {@link FastNodeData} has node stability. which means "node" must still
    * point to the same old data.
    * <br>
    * <br>
    * 
    * @param node the node to split
    * @return the new node at the split
    */
   public int splitNode(int node) {

      int burgeonNode = nextNode();

      //it can be zero if we have to split a leaf
      int nodeChildPointer = getNodeChildFirst(node);

      //save old family size
      int famSizeBeforeSplit = getNodeFamilySize(node);

      setChildrenPointer(burgeonNode, nodeChildPointer);
      setFamilySize(burgeonNode, famSizeBeforeSplit);

      //set the node to point towards
      setChildrenPointer(node, burgeonNode);
      setFamilySize(node, 1);

      //since a split does not involve family order, just take next available node

      //update the parent pointers for all children
      int nextSister = nodeChildPointer;
      while (nextSister != NOT_A_NODE) {
         offsetToParent[nextSister] = burgeonNode;
         nextSister = getNodeSisterRight(nextSister);
      }
      offsetToParent[burgeonNode] = node;
      return burgeonNode;
   }

   //#mdebug
   public void toString(Dctx sb) {
      sb.root(this, "FastNodeData");
      sb.nl();
      pdc.getTechFactory().toStringFastNodeDataTech(sb, this);
      sb.nl();
      int first = getFirstNode();
      int last = getLastNode();
      for (int i = first; i <= last; i++) {
         if (i != first) {
            sb.nl();
         }
         sb.append("node #" + i);
         sb.append(" fam=" + getNodeFamilySize(i));
         sb.append(" firstchild=" + getNodeChildFirst(i));
         sb.append(" rightsister=" + getNodeSisterRight(i));
         sb.append(" parent=" + getNodeParent(i));
         toStringSub(sb, i);
      }
   }

   public void toStringNode(Dctx sb, int i) {
      sb.append("node #" + i);
      sb.append(" fam=" + getNodeFamilySize(i));
      sb.append(" firstchild=" + getNodeChildFirst(i));
      sb.append(" rightsister=" + getNodeSisterRight(i));
      sb.append(" parent=" + getNodeParent(i));
      toStringSub(sb, i);
   }

   public String toStringNode(int i) {
      Dctx d = new Dctx(pdc.getUC(), "\n\t");
      toStringNode(d, i);
      return d.toString();
   }

   public void toStringSub(Dctx sb, int offset) {

   }
   //#enddebug

}
