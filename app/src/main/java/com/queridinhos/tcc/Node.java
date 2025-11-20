package com.queridinhos.tcc;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final String id;
    public final float x;
    public final float y;
    public final List<Edge> neighbors = new ArrayList<>();

    // Var usadas pelo A* dinamicamente
    public Node parent = null; //Nó anterior ao encontrado
    public double gCost = Double.MAX_VALUE; //Custo inicial
    public double hCost = 0; //Estimativa

    public Node(String id, float x, float y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    //Limpa para nova busca
    public void reset() {
        parent = null;
        gCost = Double.MAX_VALUE;
        hCost = 0;
    }

    //Custo total
    public double getFCost() {
        return gCost + hCost;
    }

    public void addNeighbor(Node neighbor, double weight) {
        this.neighbors.add(new Edge(neighbor, weight));
    }

    //Conexão de aresta entre nós
    public static class Edge {
        public final Node target;
        public final double weight; //Custo para atravessar aresta

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }
}