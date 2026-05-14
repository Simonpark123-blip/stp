package project.alg.stp.entity;

import java.util.List;

public record Connection(int cost, List<String> connectedPorts) {}