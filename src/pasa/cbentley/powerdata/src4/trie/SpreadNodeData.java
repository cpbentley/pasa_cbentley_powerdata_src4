package pasa.cbentley.powerdata.src4.trie;

import pasa.cbentley.byteobjects.src4.core.ByteController;
import pasa.cbentley.byteobjects.src4.core.ByteObjectManaged;
import pasa.cbentley.core.src4.structs.IntBuffer;
import pasa.cbentley.powerdata.spec.src4.power.MorphParams;
import pasa.cbentley.powerdata.spec.src4.power.itech.ITechSpreadNodeData;
import pasa.cbentley.powerdata.spec.src4.power.trie.IPowerTrieNodes;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNode;
import pasa.cbentley.powerdata.spec.src4.power.trie.TrieNodeTopo;
import pasa.cbentley.powerdata.src4.ctx.PDCtx;

/**
 * The idea of this node data.
 * <br>
 * <br>
 * <br>
 * The family pattern of each node is computed and nodes with same patterns are saved together. The Leaves
 * are not implicit. They are implicit in the pattern.
 * <br>
 * 
 * Here the pattern is actually the topology of the sub family of a node. That is
 * if a node X has 3 children, pattern will be of size 3, the content of the pattern 1 or 0 is the topology
 * Does Child Exist? 1 for Yes 0 for No. So a pattern of 101 for node X says Node X has 3 children and first and last children are not leaves.
 * Second child is a leaf. Node Topology is Branch,Leaf,Branch. 101.
 * <br>
 * <br>
 * Grappe Roots are seen as Leaves
 * <br>
 * <br>
 * 
 *   //if we use compression 
 //parse every nodes
 //when we find a pattern, we incr its counter

 // virtual store: new node's position with parent ref (mapping is used for update)
 // numberofnode(patternid)[newnode:parent=oldnode]
 // 0, 00 , 000, 0000, 01, 10, 001, 100, 010 | put other leaves with zeros |  
 // 1(0)[0:34=56], 1(00)[1:99=2],[2:99=3]
 //first pass is over, we update child pointer of parents, by using the mapping

 * we will have
 *  
 * [base header [] ] 
 * [sub header []] 
 * [familyData [bittrie or patterned sequence]]
 * [childrenPointerData [patterned sequence]]
 * 
 * [parentLinksData]
 * [Sub class data]
 * <br>
 * <br>
 * A Patterned Node is a node whose leaf children are compressed and has the same family size as all nodes in the same
 * rameau/pattern category. Un Rameau. All rameau of a given pattern/shape are stored
 * together with a single family size value. Each rameau will only have its non leaf child pointer.
 * <br>
 * <br>
 * 
 * For 0 patterned node, it will be stored with its child in the '0' chunk, children position will implicitely be +1
 * For 00 patterned nodes, it will be stored its 2 children in the '00' chunk. Children position will
 * implicitly be +1 and +2
 * For 01 patterned nodes, it will be stored with its 2 children in the 01 chunk, children position will
 * implicitly be +1 and +2 with this last child have a childpointer node.
 * <br>
 * <br>
 * @param bitChildSize the bit size of child data 
 * @return
 */
public class SpreadNodeData extends FamilyPackedNodeData implements ITechSpreadNodeData, IPowerTrieNodes {
   
   /**
    * 
    */
   protected int subClassDataOffset;

   public SpreadNodeData(PDCtx pdc, byte[] data, int index) {
      super(pdc, data, index);
   }

   public int addChild(int parentNode) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int addChild(int parentNode, int familyIndex) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int[] getChildren(int node) {
      // TODO Auto-generated method stub
      return null;
   }

   public int[] getFamily(int node) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getFirstNode() {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getFlag() {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getLastNode() {
      // TODO Auto-generated method stub
      return 0;
   }

   public Object getMorph(MorphParams p) {
      // TODO Auto-generated method stub
      return null;
   }

   public TrieNode getNode(int node) {
      // TODO Auto-generated method stub
      return null;
   }

   public int getNodeChildFirst(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeChildren(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeChildren(int node, IntBuffer dest, boolean reverse) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeFamilySize(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeParent(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodePayload(int node) {
      return 0;
   }

   public int getNodeSisterLeft(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodeSisterRight(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNodesMaxFamilySize() {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getNumberOfNodes() {
      // TODO Auto-generated method stub
      return 0;
   }

   public int getRootNode() {
      // TODO Auto-generated method stub
      return 0;
   }

   public ByteObjectManaged getTech() {
      return this;
   }

   public TrieNodeTopo getTopology(int node, int type, TrieNodeTopo tnt) {
      // TODO Auto-generated method stub
      return null;
   }

   public void getTopology(TrieNodeTopo tnt) {
      // TODO Auto-generated method stub

   }

   public boolean hasNodeFlag(int node, int flag) {
      // TODO Auto-generated method stub
      return false;
   }

   public void insert(int findex, int parentNode) {
      // TODO Auto-generated method stub

   }

   public boolean isLeaf(int node) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isSuffixNode(int node) {
      // TODO Auto-generated method stub
      return false;
   }

   public int nextNode() {
      // TODO Auto-generated method stub
      return 0;
   }

   public int payLoadSplit(int node, int firstPL, int secondPL) {
      // TODO Auto-generated method stub
      return 0;
   }

   public byte[] serializePack() {
      // TODO Auto-generated method stub
      return null;
   }

   public void serializeReverse(ByteController bc) {
      // TODO Auto-generated method stub

   }

   public ByteObjectManaged serializeTo(ByteController bc) {
      return null;
   }

   public void setNodeFlag(int node, int flag, boolean v) {
      // TODO Auto-generated method stub

   }

   public int splitNode(int node) {
      // TODO Auto-generated method stub
      return 0;
   }

   public String toStringNode(int i) {
      // TODO Auto-generated method stub
      return null;
   }

}
