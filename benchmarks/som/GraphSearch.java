package som;

import java.util.ArrayList;

/**
Breadth-first search
====================

In Breadth-first search, a graph (comprised of vertices and edges) is read in
from a file.  From this, a single-source shortest-path algorithm is computed.
This algorithm currently assumes that each edge weight is one.  However, the
algorithm should be easily extendable to cover this case.  This particular
program has been ported from the Rodidnia Benchmark Suite.

Note: This application was ported from the Rodinia Suite
      (https://www.cs.virginia.edu/~skadron/wiki/rodinia/).
Note2: Actually, ported from
      https://github.com/Sable/Ostrich/blob/master/graph-traversal/bfs/js/bfs.js
*/
public class GraphSearch extends Benchmark {
  private static int MIN_NODES      = 20;
  private static int MAX_NODES      = 1<<31;
  private static int MIN_EDGES      = 2;
  private static int MAX_INIT_EDGES = 4;
  private static int MIN_WEIGHT     = 1;
  private static int MAX_WEIGHT     = 1;

  private static int EXPECTED_NO_OF_NODES = 3000000;
  private static int EXPECTED_TOTAL_COST  = 26321966;

  public static void main(final String[] args) {
    new GraphSearch().run(args);
  }

  private static class Node {
    public final int starting;
    public final int noOfEdges;
    Node(final int starting, final int noOfEdges) {
      this.starting  = starting;
      this.noOfEdges = noOfEdges;
    }
  }

  private static class Edge {
    public final int dest;
    public final int weight;

    Edge(final int dest, final int weight) {
      this.dest   = dest;
      this.weight = weight;
    }
  }

  private Node[]    hGraphNodes;
  private boolean[] hGraphMask;
  private boolean[] hUpdatingGraphMask;
  private boolean[] hGraphVisited;
  private int[]     hCost;
  private int[]     hGraphEdges;

  private void initializeGraph(final int noOfNodes) {
    hGraphNodes        = new Node[noOfNodes];
    hGraphMask         = new boolean[noOfNodes];
    hUpdatingGraphMask = new boolean[noOfNodes];
    hGraphVisited      = new boolean[noOfNodes];
    hCost              = new int[noOfNodes];

    int source = 0;
    @SuppressWarnings("unchecked")
    ArrayList<Edge>[] graph = new ArrayList[noOfNodes];
    for (int i = 0; i < noOfNodes; ++i) {
        graph[i] = new ArrayList<>();
    }

    for (int i = 0; i < noOfNodes; ++i) {
        int noOfEdges = Math.abs(JenkinsRandom.random() % (MAX_INIT_EDGES - MIN_EDGES + 1)) + MIN_EDGES;
        for (int j = 0; j < noOfEdges; ++j) {
            int nodeId = Math.abs(JenkinsRandom.random() % noOfNodes);
            int weight = Math.abs(JenkinsRandom.random() % (MAX_WEIGHT - MIN_WEIGHT + 1)) + MIN_WEIGHT;

            graph[i].add(new Edge(nodeId, weight));
            graph[nodeId].add(new Edge(i, weight));
        }
    }

    int totalEdges = 0;
    for (int i = 0; i < noOfNodes; ++i) {
        int noOfEdges         = graph[i].size();
        hGraphNodes[i]        = new Node(totalEdges, noOfEdges);
        hGraphMask[i]         = false;
        hUpdatingGraphMask[i] = false;
        hGraphVisited[i]      = false;

        totalEdges += noOfEdges;
    }

    hGraphMask[source]    = true;
    hGraphVisited[source] = true;

    hGraphEdges = new int[totalEdges];

    int k = 0;
    for (int i = 0; i < noOfNodes; ++i) {
        for (int j = 0; j < graph[i].size(); ++j) {
            hGraphEdges[k] = graph[i].get(j).dest;
            ++k;
        }
    }

    for (int i = 0; i < noOfNodes; ++i) {
        hCost[i] = -1;
    }
    hCost[source] = 0;
  }

  @Override
  public Object benchmark() {
    JenkinsRandom.setSeed(49734321);
    int noOfNodes = EXPECTED_NO_OF_NODES;
    initializeGraph(noOfNodes);
    breadthFirstSearch(noOfNodes);
    return hCost;
  }

  private void breadthFirstSearch(final int noOfNodes) {
    boolean stop;

    do {
      stop = false;

      for(int tid = 0; tid < noOfNodes; ++tid) {
        if (hGraphMask[tid]) {
          hGraphMask[tid] = false;
          for (int i = hGraphNodes[tid].starting;
             i < (hGraphNodes[tid].noOfEdges + hGraphNodes[tid].starting);
            ++i) {
            int id = hGraphEdges[i];
            if (!hGraphVisited[id]) {
              hCost[id] = hCost[tid] + 1;
              hUpdatingGraphMask[id] = true;
            }
          }
        }
      }

      for (int tid = 0; tid < noOfNodes; ++tid) {
        if (hUpdatingGraphMask[tid]) {
          hGraphMask[tid]    = true;
          hGraphVisited[tid] = true;
          stop = true;
          hUpdatingGraphMask[tid] = false;
        }
      }
    } while(stop);
  }

  @Override
  public boolean verifyResult(final Object result) {
    int totalCost = 0;

    for (int i = 0; i < hCost.length; ++i) {
      totalCost += hCost[i];
    }

    if (hCost.length == EXPECTED_NO_OF_NODES) {
      if (totalCost != EXPECTED_TOTAL_COST) {
          throw new Error("ERROR: the total cost obtained for '" + hCost.length
              + "' nodes is '" + totalCost + "' while the expected cost is '"
              + EXPECTED_TOTAL_COST + "'");
      }
    } else {
      System.out.println("WARNING: no self-checking step for '" + hCost.length
          + "' nodes, only valid for '" + EXPECTED_NO_OF_NODES + "' nodes");
    }

    return true;
  }
}
