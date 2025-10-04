package com.queridinhos.tcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar {

    // MÉTODO MODIFICADO: Agora recebe o grafo inteiro para poder resetá-lo
    public static List<Node> findPath(Map<String, Node> graph, Node start, Node goal) {
        // --- ESTA É A CORREÇÃO PRINCIPAL ---
        // Limpa todos os nós antes de começar
        for (Node node : graph.values()) {
            node.reset();
        }
        // ------------------------------------

        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFCost(), b.getFCost()));
        List<Node> closedSet = new ArrayList<>();

        start.gCost = 0;
        start.hCost = heuristic(start, goal);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            if (currentNode.id.equals(goal.id)) {
                return reconstructPath(goal);
            }

            closedSet.add(currentNode);

            for (Node.Edge edge : currentNode.neighbors) {
                Node neighbor = edge.target;
                if (closedSet.contains(neighbor)) {
                    continue;
                }

                double tentativeGCost = currentNode.gCost + edge.weight;

                if (tentativeGCost < neighbor.gCost) {
                    neighbor.parent = currentNode;
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = heuristic(neighbor, goal);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null; // Caminho não encontrado
    }

    private static double heuristic(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private static List<Node> reconstructPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }
}