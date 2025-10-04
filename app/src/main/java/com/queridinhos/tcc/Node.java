package com.queridinhos.tcc;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final String id;
    public final float x;
    public final float y;
    public final List<Edge> neighbors = new ArrayList<>();

    // Campos para o algoritmo A*
    public Node parent = null;
    public double gCost = Double.MAX_VALUE;
    public double hCost = 0;

    public Node(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    // NOVO MÉTODO PARA LIMPAR O NÓ
    public void reset() {
        parent = null;
        gCost = Double.MAX_VALUE;
        hCost = 0;
    }

    public double getFCost() {
        return gCost + hCost;
    }

    public void addNeighbor(Node neighbor, double weight) {
        this.neighbors.add(new Edge(neighbor, weight));
    }

    public static class Edge {
        public final Node target;
        public final double weight;

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }
}