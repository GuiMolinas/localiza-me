package com.queridinhos.tcc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar {

    //Recebe nós para encontrar menor caminho entre dois pontos
    public static List<Node> findPath(Map<String, Node> graph, Node start, Node goal) {
        //Reinicia estados dos nós dos grafos, para evitar residuos
        for (Node node : graph.values()) {
            node.reset();
        }

        //Lista de prioridade e ordenação por custo, nó menor primeiro
        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.getFCost(), b.getFCost()));
        List<Node> closedSet = new ArrayList<>();

        //Configuração nó de partida
        start.gCost = 0;
        start.hCost = heuristic(start, goal);
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll(); //Nó com menor custo

            //Destino chego, caminho de volta
            if (currentNode.id.equals(goal.id)) {
                return reconstructPath(goal);
            }

            closedSet.add(currentNode);

            //Vizinhos do meu nó atual
            for (Node.Edge edge : currentNode.neighbors) {
                Node neighbor = edge.target;
                if (closedSet.contains(neighbor)) {
                    continue; // Ignora caso avaliado
                }

                double tentativeGCost = currentNode.gCost + edge.weight;

                //Caso encontra caminho mais curto
                if (tentativeGCost < neighbor.gCost) {
                    neighbor.parent = currentNode; //Define caminho que viemos
                    neighbor.gCost = tentativeGCost;
                    neighbor.hCost = heuristic(neighbor, goal);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return null;  //Retorna caso não tenha nenhum caminho
    }

    //Estima distância até destino
    private static double heuristic(Node a, Node b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    //Reconstrução da lista de nós, de destino a inicio
    private static List<Node> reconstructPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path); //Inversão de lista, inicio ao fim
        return path;
    }
}