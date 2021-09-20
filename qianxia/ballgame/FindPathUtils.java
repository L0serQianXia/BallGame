package qianxia.ballgame;

import java.io.NotActiveException;
import java.util.*;

/**
 * @description: 来寻路
 * @author: QianXia
 * @create: 2021/09/20 18:14
 **/
public class FindPathUtils {
    private static Queue<Node> openList = new PriorityQueue<>();
    private static List<Node> closeList = new ArrayList<>();
    private static Node found = null;

    public static List<Node> findPath(int[] from, int[] to) {
        openList.clear();
        closeList.clear();
        found = null;

        openList.add(new Node(from));

        while (!openList.isEmpty()) {
            Node node = openList.poll();
            closeList.add(node);

            addNeighbourNode(from, to, node, node.x - 1, node.y);
            if (found != null) {
                break;
            }
            addNeighbourNode(from, to, node, node.x + 1, node.y);
            if (found != null) {
                break;
            }
            addNeighbourNode(from, to, node, node.x, node.y - 1);
            if (found != null) {
                break;
            }
            addNeighbourNode(from, to, node, node.x, node.y + 1);
            if (found != null) {
                break;
            }
        }
        List<Node> pathNode = null;
        if (found == null) {
            System.out.println("Sorry, not found the path:(((");
        } else {
            pathNode = new ArrayList<>();
            Node parent = found;
            while (parent != null) {
                pathNode.add(0, parent);
                parent = parent.parent;
            }
        }
        return pathNode;
    }

    private static void addNeighbourNode(int[] from, int[] to, Node neighbour, int x, int y) {
        if (canAdd(x, y)) {
            Node now = new Node(x, y);
            if (to[0] == x && to[1] == y) {
                now.parent = neighbour;
                found = now;
                return;
            }
            if (!isInCollection(openList, x, y)) {
                now.g = getDistance(from, new int[]{x, y});
                now.h = getDistance(new int[]{x, y}, to);
                now.f = now.g + now.h;
                now.parent = neighbour;
                openList.add(now);
            } else {
                now = getNodeByQueue(x, y);
                if (neighbour.f < now.f) {
                    now.parent = neighbour;
                    now.g = getDistance(from, new int[]{x, y});
                    now.h = getDistance(new int[]{x, y}, to);
                    now.f = now.g + now.h;
                }
            }
        }

    }

    private static Node getNodeByQueue(int x, int y) {
        for (Node node : openList) {
            if (node.x == x && node.y == y) {
                return node;
            }
        }
        return null;
    }

    private static int getDistance(int[] from, int[] to) {
        return Math.abs(from[0] - to[0]) + Math.abs(from[1] - to[1]);
    }

    private static boolean isInCollection(Collection<Node> collection, int x, int y) {
        for (Node node : collection) {
            if (node.x == x && node.y == y) {
                return true;
            }
        }
        return false;
    }

    private static boolean canAdd(int x, int y) {
        // close list?
        boolean flag = true;
        for (Node node : closeList) {
            if (node.x == x && node.y == y) {
                flag = false;
                break;
            }
        }
        return x >= 0 && x <= 8 && y >= 0 && y <= 8 && UI.INSTANCE.getBallFromGamePosition(x, y) == null && flag;
    }

    public static class Node implements Comparable {
        int x, y;
        // 代价，起始点到该点的距离，该点到目标点的距离
        int f, g, h;
        Node parent = null;

        public Node(int[] from) {
            this.x = from[0];
            this.y = from[1];
        }

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Object o) {
            Node fromNode = (Node) o;
            if ((g + h) > (fromNode.g + fromNode.h)) {
                return 1;
            } else if ((g + h) < (fromNode.g + fromNode.h)) {
                return -1;
            }
            return 0;
        }
    }
}
