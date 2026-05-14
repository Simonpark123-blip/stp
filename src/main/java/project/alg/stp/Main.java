package project.alg.stp;

import project.alg.stp.entity.Connection;
import project.alg.stp.entity.Port;
import project.alg.stp.entity.Switch;

import java.util.*;

import static project.alg.stp.helper.Helper.randomMACAddress;

public class Main {

    private static final List<Switch> switchList = initializeSwitches();
    private static final List<Connection> connectionList = new ArrayList<>(List.of(
            new Connection(1, List.of("A/1", "B/3")),
            new Connection(1, List.of("B/1", "D/1")),
            new Connection(1, List.of("D/2", "E/1")),
            new Connection(2, List.of("E/2", "B/2")),
            new Connection(2, List.of("E/4", "F/1")),
            new Connection(2, List.of("E/3", "C/2")),
            new Connection(1, List.of("C/1", "A/2"))
    ));

    public static void main(String[] args) {
        try {
            printSwitches();

            Switch rootBridge = findRootBridge();
            if (rootBridge == null) {
                throw new IllegalStateException("No root bridge found");
            }

            printConnections();

            findUsedPorts();
            printSwitches();

            findPathCosts(rootBridge);
            printSwitches();

            markDesignatedPorts(rootBridge);
            printSwitches();

            findBlockedPorts(rootBridge);
            printSwitches();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static void findPathCosts(Switch rootBridge) {
        List<Switch> notRootSwitches = switchList.stream()
                .filter(sw -> !sw.getName().equals(rootBridge.getName()))
                .toList();

        for (Switch currentSwitch : notRootSwitches) {
            System.out.println("Calculating path costs for switch " + currentSwitch.getName());

            Map.Entry<Connection, Integer> costs = handleConnections(0, rootBridge, currentSwitch, null);
            if (costs == null || costs.getKey() == null) {
                System.out.println("No valid path found for switch " + currentSwitch.getName());
                continue;
            }

            System.out.println("Processing switch " + currentSwitch.getName() + " finished, min costs are: " + costs.getValue());

            String rootPortStr = costs.getKey()
                    .connectedPorts()
                    .stream()
                    .filter(port -> port.contains(currentSwitch.getName()))
                    .findFirst()
                    .orElse(null);

            if (rootPortStr == null) {
                continue;
            }

            Port rootPort = currentSwitch.getPorts()
                    .stream()
                    .filter(port -> rootPortStr.equals(port.getPortName()))
                    .findFirst()
                    .orElse(null);

            if (rootPort == null) {
                continue;
            }

            rootPort.setIsRoot(true);
            System.out.println("Root port for switch " + currentSwitch.getName() + ": " + rootPortStr);
        }
    }

    private static Map.Entry<Connection, Integer> handleConnections(int pathCosts, Switch rootBridge, Switch currentSwitch, Connection lastConnection) {
        if (currentSwitch == null || rootBridge == null) {
            return null;
        }

        List<Connection> currentConnections = connectionList.stream()
                .filter(connection ->
                        (connection.connectedPorts().getFirst().contains(currentSwitch.getName())
                                || connection.connectedPorts().getLast().contains(currentSwitch.getName()))
                                && !connection.equals(lastConnection))
                .toList();
        Map<Connection, Integer> costsList = new HashMap<>();

        for (Connection connection : currentConnections) {
            System.out.println("Processing connection " + connection.connectedPorts());

            Map.Entry<Connection, Integer> cost = calcPathCostsForConnection(pathCosts, rootBridge, currentSwitch, connection);

            if (cost != null) {
                costsList.put(connection, cost.getValue());
                System.out.println("Final costs for connection " + connection.connectedPorts() + " is "+ cost.getValue());
            }
        }

        if (costsList.isEmpty()) {
            System.out.println("No connections found for switch " + currentSwitch.getName() + " towards root-bridge");
            return null;
        }

        return costsList.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null);
    }

    private static Map.Entry<Connection, Integer> calcPathCostsForConnection(int pathCosts, Switch rootBridge, Switch currentSwitch, Connection connection) {
        if (connection == null || currentSwitch == null || rootBridge == null) {
            return null;
        }

        if (connection.connectedPorts().getFirst().contains(currentSwitch.getName())
                && connection.connectedPorts().getLast().contains(currentSwitch.getName())) {
            throw new RuntimeException("Connection with connected ports " + connection.connectedPorts().getFirst() + " and " + connection.connectedPorts().getLast() + " exists");
        }

        if (connection.connectedPorts().getFirst().contains(rootBridge.getName())
                || connection.connectedPorts().getLast().contains(rootBridge.getName())) {
            int costs = pathCosts + connection.cost();
            System.out.println("Direct connection to root-bridge reached with connection " + connection.connectedPorts() + " results in path-costs of " + costs);

            return Map.entry(connection, costs);
        }

        String connectedPortName = connection.connectedPorts()
                .stream()
                .filter(port -> !port.contains(currentSwitch.getName()))
                .findFirst()
                .orElse(null);

        if (connectedPortName == null || !connectedPortName.contains("/")) {
            return null;
        }

        String connectedSwitch = connectedPortName.split("/")[0];
        Switch updatedCurrentSwitch = switchList
                .stream()
                .filter(switchEntity -> switchEntity.getName().contains(connectedSwitch))
                .findFirst()
                .orElse(null);

        if (updatedCurrentSwitch == null) {
            return null;
        }

        System.out.println("Switch "
                + currentSwitch.getName()
                + " has connection to switch "
                + connectedSwitch);

        return handleConnections(
                pathCosts + connection.cost(),
                rootBridge,
                updatedCurrentSwitch,
                connection
        );
    }

    private static Switch findRootBridge() {
        Switch rootSwitch = Main.switchList.stream()
                .min(Comparator.comparing(Switch::getPriority)
                        .thenComparing(Switch::getMacAddress))
                .orElse(null);

        if (rootSwitch != null) {
            System.out.println("Root bridge is: \n-- Switch "
                    + rootSwitch.getName()
                    + " --");
        }

        return rootSwitch;
    }

    private static List<Switch> initializeSwitches() {
        Switch switchA = new Switch(40960, 'A', randomMACAddress());
        Switch switchB = new Switch(32768, 'B', randomMACAddress());
        Switch switchC = new Switch(36864, 'C', randomMACAddress());
        Switch switchD = new Switch(32768, 'D', randomMACAddress());
        Switch switchE = new Switch(40960, 'E', randomMACAddress());
        Switch switchF = new Switch(32768, 'F', randomMACAddress());

        return new ArrayList<>(List.of(
                switchA,
                switchB,
                switchC,
                switchD,
                switchE,
                switchF
        ));
    }

    private static void findBlockedPorts(Switch rootBridge) {
        for (Switch switchEntity : switchList) {
            for (Port port : switchEntity.getPorts()) {
                boolean isRootBridgePort =
                        switchEntity.getName().equals(rootBridge.getName());

                if (!port.isUsed()
                        || isRootBridgePort
                        || (port.isDesignated() || port.isRoot())) {

                    port.setIsBlocked(false);
                }
            }
        }
    }

    private static void findUsedPorts() {
        for (Switch switchEntity : switchList) {
            for (Port port : switchEntity.getPorts()) {
                boolean isPortFound = connectionList.stream()
                        .anyMatch(connection -> connection.connectedPorts().contains(port.getPortName()));

                port.setIsUsed(isPortFound);
            }
        }
    }

    private static void printSwitches() {
        List<Switch> sortedSwitchList = switchList.stream()
                .sorted(Comparator.comparing(Switch::getName))
                .toList();

        String portFormat = "%-6s%-7s%-9s%-7s%-10s";
        String switchFormat = "%n%-8s | %-17s | %-8s | %-40s| %-40s| %-40s| %-40s";
        StringBuilder outputTable = new StringBuilder();
        String portDesc = String.format(
                portFormat,
                "Port",
                "used",
                "blocked",
                "root",
                "designated"
        );

        outputTable.append(String.format(
                switchFormat,
                "Switch",
                "MacAddress",
                "Priority",
                portDesc,
                portDesc,
                portDesc,
                portDesc
        ));

        for (Switch switchEntity : sortedSwitchList) {
            List<String> portValues = new ArrayList<>();
            List<Port> ports = switchEntity.getPorts()
                    .stream()
                    .sorted(Comparator.comparing(Port::getPortName))
                    .toList();

            ports.forEach(port -> portValues.add(
                    String.format(
                            portFormat,
                            port.getPortName(),
                            port.isUsed(),
                            port.isBlocked(),
                            port.isRoot(),
                            port.isDesignated()
                    )
            ));

            outputTable.append(String.format(
                    switchFormat,
                    "Switch " + switchEntity.getName(),
                    switchEntity.getMacAddress(),
                    switchEntity.getPriority(),
                    !portValues.isEmpty() ? portValues.get(0) : "",
                    portValues.size() > 1 ? portValues.get(1) : "",
                    portValues.size() > 2 ? portValues.get(2) : "",
                    portValues.size() > 3 ? portValues.get(3) : ""
            ));
        }

        System.out.println(outputTable);
        System.out.println();
    }

    private static void printConnections() {
        System.out.println("\n------------------------------------");
        for (Connection connection : Main.connectionList) {
            System.out.print("| "
                    + connection.connectedPorts().getFirst()
                    + " | "
                    + connection.connectedPorts().getLast()
                    + " | "
                    + connection.cost()
                    + " | ");
            System.out.println("\n------------------------------------");
        }
        System.out.println("\n");
    }

    private static List<Connection> findNotRootConnections(Switch rootBridge) {
        return connectionList.stream()
                .filter(connection ->
                        !connection.connectedPorts().getFirst().contains(rootBridge.getName())
                                && !connection.connectedPorts().getLast().contains(rootBridge.getName()))
                .toList();
    }

    private static void markDesignatedPorts(Switch rootBridge) {
        List<Connection> notRootConnections = findNotRootConnections(rootBridge);

        for (Connection connection : notRootConnections) {
            List<Switch> currentSwitches = switchList.stream()
                    .filter(sw ->
                            connection.connectedPorts().toString().contains(sw.getName()))
                    .toList();

            System.out.println("Evaluating designated port for connection " + connection.connectedPorts());
            Map<Switch, Integer> switchCosts = new HashMap<>();

            for (Switch currentSwitch : currentSwitches) {
                Map.Entry<Connection, Integer> connectingPortCost = handleConnections(0, rootBridge, currentSwitch, null);
                Integer cost = connectingPortCost != null
                                ? connectingPortCost.getValue()
                                : null;

                System.out.println("Cost for connecting port of connection " + connection.connectedPorts() + " with currentSwitch " + currentSwitch.getName() + " is " + cost);
                switchCosts.put(currentSwitch, cost);
            }

            Optional<Map.Entry<Switch, Integer>> lowestCostSwitch = switchCosts.entrySet()
                            .stream()
                            .filter(el -> el.getValue() != null)
                            .min(Map.Entry.<Switch, Integer>comparingByValue()
                                    .thenComparing(e -> e.getKey().getPriority())
                                    .thenComparing(e -> e.getKey().getMacAddress()));

            if (lowestCostSwitch.isEmpty()) {
                continue;
            }

            Switch selectedSwitch = lowestCostSwitch.get().getKey();
            System.out.println("Switch with lowest cost is " + selectedSwitch.getName());

            List<Port> designatedPortList = selectedSwitch.getPorts()
                    .stream()
                    .filter(port ->
                            connection.connectedPorts().contains(port.getPortName()))
                    .toList();

            if (designatedPortList.size() != 1) {
                throw new IllegalStateException("Found an illegal amount of ports");
            }

            Port designatedPort = designatedPortList.getFirst();
            designatedPort.setIsDesignated(true);
        }
    }
}