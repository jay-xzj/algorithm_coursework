package uk.co.ncl.da.coursework;

import java.io.*;
import java.util.*;

/**
* @description: process to execute the algorithm
* @author JayXu
*/
public class RunAlgorithm {
    /**
     * Main method to run the algorithm.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //1. Read the neighbor relationships between nodes from an arbitrary txt file.
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input your filename（1～3）!");
        String s = sc.nextLine();
        if (!s.equals("1")&&!s.equals("2")&&!s.equals("3")){
            throw new IllegalArgumentException("Please input the correct filename from 1 to 3!");
        }
        final String path = System.getProperty("user.dir") + "/src/resources/";
        System.out.println("+++Read file start!+++");
        //Make the node itself as a key, and a list of its neighs as value.
        Map<String, List<String>> map = getStringListMap(path+s+".txt");
        System.out.println("---Read file complete!---");
        System.out.println();

        List<Node> nodeList = assembleNodeList(map);
        int[][] matrix = initiateMatrix(map);

        //2. Start running the echo algorithm.
        runAlgorithm(nodeList,matrix);

        //3. Count the messages sent among the nodes
        int count = 0;
        System.out.println();
        System.out.println("Matrix：");
        for (int i = 0; i < matrix.length; i++) {
            int[] matrix2 = matrix[i];
            for (int j = 0; j <matrix2.length; j++) {
                System.out.print(matrix2[j]+"   ");
                if (matrix2[j]==1){
                    count++;
                }
                if ((j+1)%matrix.length==0){
                    System.out.println();
                }
            }
        }
        System.out.println("Count of Messages："+count);
    }


    /**
     * Originally, set all the elements in this matrix as 0,
     * when the messages been sent between the specific nodes, change the value to 1.
     * at the end of the algorithm, we only need to count the amount of 1.
     * @param map key is node's number, and value is the neighs of this node.
     * @return int[][] the matrix to record the amount of the messages send between these nodes.
     */
    private static int[][] initiateMatrix(Map<String, List<String>> map) {
        int size = map.size();
        int[][] matrix = new int[size][size];
        for (Map.Entry<String,List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer iKey = Integer.valueOf(key);
            List<String> value = entry.getValue();
            for (String s: value) {
                Integer is = Integer.valueOf(s);
                matrix[iKey][is] = 0;
            }
        }
        return matrix;
    }

    /**
     * Execute echo algorithm over the structure input.
     * @param nodeList original nodeList
     * @param matrix to record the msgs
     */
    private static void runAlgorithm(List<Node> nodeList, int[][] matrix) {
        Node initiator = null;
        int K = 10;
        for (int i = 0; i <K ; i++) {
            System.out.println("+ the "+(i+1)+" time~");
            //RandomLy select R nodes out of N nodes.1~size
            int size = nodeList.size();
            int r = (int)(Math.random() * size)+1;
            //Randomly select several nodes from the nodeList
            List<Node> list = getRandomList(nodeList,r);

            //select the initiator from the randomly selected list.
            if (i==0){
                //initiator = nodeList.get(7);
                initiator = list.get(0);
                initiator.setInitiator(true);
                System.out.println("The initiator is node "+initiator.getId());
                initiator.sendMsgs(getNeighs(initiator,nodeList),matrix);
            }else{
                //如果选到的点有父节点的话，或者未到达的节点的话需要跳过。
                //select the qualified nodes, continue the algorithm.
                List<Node> qNodes = qualifiedNodes(list);
                selectNodeSendMsgs(qNodes,nodeList,matrix);
            }

            if (initiator.getRecs().size() == initiator.getSends().size()) {
                System.out.println("initiator: "+initiator.getId()+" decides!");
                System.out.println("Algorithm over!");
                break;
            }
            //没有跑完算法就自增10，If the algorithm isn't over, we'll increment the loop times
            //算法是否走完——initiator是否收到所有子节点的reply——check它的recs是不是等于所有neighs
            if (i == K-1 && initiator.getNeighs().size()!=initiator.getRecs().size()){
                K = K+10;
            }
        }
    }

    /**
     * Which node is qualified to run the algorithm?
     * --- Already has its own father.
     *
     * @param list list of nodes
     * @return qualifiedNodes
     */
    private static List<Node> qualifiedNodes(List<Node> list) {
        List<Node> qualifiedNodes = new ArrayList<>();
        list.forEach(node -> {
            if (node.getFather()!=null){
                qualifiedNodes.add(node);
            }
        });
        return qualifiedNodes;
    }

    /**
     * Select the neigh nodes of the qualified nodes to execute the algorithm.
     * @param qNodes qualified nodes
     * @param nodeList originally assembled nodeList
     * @param matrix matrix for counting messages
     */
    private static void selectNodeSendMsgs(List<Node> qNodes, List<Node> nodeList, int[][] matrix) {
        for (Node node :qNodes) {
            node.sendMsgs(getNeighs(node,nodeList),matrix);
        }
    }

    /**
     * Select the current node's neighs from the original nodeList.
     * @param node current qualified node
     * @param nodeList original nodeList
     * @return nodes neigh nodes
     */
    private static List<Node> getNeighs(Node node, List<Node> nodeList) {
        List<String> neighs = node.getNeighs();
        List<Node> nodes = new ArrayList<>();
        for (String s:neighs) {
            nodes.add(nodeList.get(Integer.parseInt(s)));
        }
        return nodes;
    }

    /**
     *  Randomly select several nodes from nodeList.
     * @param nodeList original nodeList
     * @param rdm how many nodes will be selected.
     * @return
     */
    private static List<Node> getRandomList(List<Node> nodeList, int rdm) {
        List<Node> list = new ArrayList<>();
        Set<Node> set = new HashSet<>();
        while(set.size()<rdm){
            int s = (int)(Math.random() * nodeList.size());
            set.add(nodeList.get(s));
        }
        list.addAll(set);
        return list;
    }

    /**
     * Use the map created from the txt file to initiate nodes,
     * and use the nodelist to run echo algorithm.
     *
     * @param map the map created from the txt file
     * @return nodeList list of all nodes that been created from the parameter
     */
    private static List<Node> assembleNodeList(Map<String, List<String>> map) {
        List<Node> nodeList = new ArrayList<>();
        map.forEach((s, strings) -> {
            Integer iKey = Integer.valueOf(s);
            //create a node with key and neigh strings.
            Node node = new Node(false, iKey, null, strings, new ArrayList<String>(), new ArrayList<>());
            nodeList.add(node);
        });
        return nodeList;
    }

    /**
     * Read the node and their neighbor node from the txt file.
     * Assemble the [node,node] structure into [node,{neigh,neigh,...}] structure,
     * preparing for building the node list.
     * @param path The real address of the file in this computer.
     * @return Map
     * @throws IOException FileNotFound
     */
    private static Map<String, List<String>> getStringListMap(String path) throws IOException {
        //io stream read the txt file
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path))));
        String lineTxt = null;
        Map<String, List<String>> map= new HashMap<>();
        while ((lineTxt = br.readLine()) != null) {
            //split the string to a 2 length array
            System.out.println(lineTxt);
            String[] split = lineTxt.split(",");
            String s = split[0];
            if (map.get(s) != null) {
                List<String> strings = map.get(s);
                strings.add(split[1]);
            }else{
                List<String> strs = new ArrayList<String>();
                strs.add(split[1]);
                map.put(s, strs);
            }
        }
        br.close();
        return map;
    }
}

