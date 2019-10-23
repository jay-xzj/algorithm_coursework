package uk.co.ncl.da.coursework;

import java.util.List;

/**
* @description: Node class
* @author JayXu
*/
public final class Node {
    private Boolean isInitiator; //current node is or isn't initiator
    private Integer id; //current node's id
    private Node father; //current node's father node
    private List<String> neighs; // current node's neighs
    private List<String> sends; // nodes received current node's message
    private List<String> recs; // current node received message from

    public Node(Boolean isInitiator, Integer id, Node father, List<String> neighs,
                List<String> sends, List<String> recs) {
        this.isInitiator = isInitiator;
        this.id = id;
        this.father = father;
        this.neighs = neighs;
        this.sends = sends;
        this.recs = recs;
    }

    public Boolean getInitiator() {
        return isInitiator;
    }

    public void setInitiator(Boolean initiator) {
        isInitiator = initiator;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Node getFather() {
        return father;
    }

    public void setFather(Node father) {
        this.father = father;
    }

    public List<String> getNeighs() {
        return neighs;
    }

    public void setNeighs(List<String> neighs) {
        this.neighs = neighs;
    }

    public List<String> getSends() {
        return sends;
    }

    public void setSends(List<String> sends) {
        this.sends = sends;
    }

    public List<String> getRecs() {
        return recs;
    }

    public void setRecs(List<String> recs) {
        this.recs = recs;
    }
    /**
     * Traverse and send messages to neighbors of the current node
     * @param neighs used to iterator the nodes to send message
     * @param matrix used to store(or record) messages that has been sent(change 0 to 1 in the matrix)
     */
    public void sendMsgs(List<Node> neighs,int[][] matrix){
        for (Node neigh : neighs) {
            int nSize = this.getNeighs().size();
            int rSize = this.getRecs().size();
            int sSize = this.getSends().size();
            Node father = this.getFather();
            Boolean boi = this.getInitiator();
            if (boi == true) {
                //when current node is the initiator, then send messages to all its neighbors
                sendMsg(neigh, matrix);
            } else {
                //when current node is non-initiator, then send messages to neighbors except its father
                if (neigh.getId() != father.getId()) {
                    //when neighbor node isn't father node, send message directly
                    sendMsg(neigh, matrix);
                } else {
                    // If the current node wants to send information to the parent node, it must satisfy the following conditions:
                    // 1. The information has been sent to all neighbor nodes except the parent node.
                    // 2. These neighbors also replied to the information to it.
                    if (rSize == nSize && sSize == (nSize - 1)) {
                        sendMsg(father, matrix);
                    }
                }
            }
        }
    }

    /**
     * This method define the
     *
     *  Process of sending information to neighbors:
     *  1、add the neigh's id into the sends list.
     *  2、If the neighbor is not initiator and its father node is null, then we assign the current node as this neighbor's father node.
     *  3、add the current node's id into neigh's recs list.
     *
     * @param neigh
     * @param matrix
     */
    public void sendMsg(Node neigh,int[][] matrix){
        //1. add the neigh into the sends list.
        List<String> sends = this.getSends();
        if (!sends.contains(neigh.getId()+"")) {
            sends.add(neigh.getId() + "");
            matrix[this.getId()][neigh.getId()] = 1;
            System.out.println("node "+this.getId() +" ==> node "+neigh.getId());
        }
        this.setSends(sends);
        //2. If the neighbor is not initiator and its father node is null, then we should assign the current node as this neighbor's father node.
        Node father = neigh.getFather();
        if (null == father && !neigh.getInitiator()){
            neigh.setFather(this);
            System.err.println("node "+neigh.getId()+"'s father is node "+this.getId());
        }
        //3. add the current node's id into neigh's recs list.
        List<String> recs = neigh.getRecs();
        Integer id = this.getId();
        if (!recs.contains(id+"")){
            recs.add(id+"");
        }
        neigh.setRecs(recs);
    }

}