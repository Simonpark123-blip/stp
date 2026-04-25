package project.alg.stp;

import project.alg.stp.entity.Connection;
import project.alg.stp.entity.Port;
import project.alg.stp.entity.Switch;

import java.util.*;

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
        printSwitches();

        Switch rootBridge = findRootBridge();

        printConnections();

        findPathCosts(rootBridge);

        printSwitches();

        findBlockedPorts();

        printSwitches();

        markDesignatedPorts(rootBridge);

        printSwitches();
    }

    private static void findPathCosts(Switch rootBridge) {
        List<Switch> notRootSwitches = switchList.stream().filter(switchEntity -> !switchEntity.getName().equals(rootBridge.getName())).toList();
        // iteriere durch alle nicht Roots
        for (Switch notRootSwitch : notRootSwitches) {
            System.out.println("Calculating path costs for switch " + notRootSwitch.getName());

            // Alle Verbindungen zum aktuellen Switch durchsuchen nach geringsten Kosten
            Map.Entry<Connection, Integer> costs = handleConnections(0, rootBridge, notRootSwitch, null);
            System.out.println("Processing switch " + notRootSwitch.getName() + " finished, min costs are: " + costs.getValue());
            List<Port> currentPorts = notRootSwitch.getPorts();
            String rootPortStr = costs.getKey().getConnectedPorts().stream().filter(port -> port.contains(notRootSwitch.getName())).findFirst().orElse(null);
            Port rootPort = currentPorts.stream().filter(port -> port.getPortName().equals(rootPortStr)).findFirst().orElse(null);
            System.out.println("Root port for switch " + notRootSwitch.getName() + ": " + rootPortStr);
            assert rootPort != null;
            currentPorts.remove(rootPort);
            rootPort.setIsRoot(true);
            currentPorts.add(rootPort);
            notRootSwitch.setPorts(currentPorts);
        }
    }

    private static Map.Entry<Connection, Integer> handleConnections(int pathCosts, Switch rootBridge, Switch currentSwitch, Connection lastConnection){
        // durchlaufe alle angeschlossenen connections
        List<Connection> currentConnections = connectionList
                .stream()
                .filter(
                        connection ->
                            (connection.getConnectedPorts().getFirst().contains(currentSwitch.getName())
                                    || connection.getConnectedPorts().getLast().contains(currentSwitch.getName()))
                                    && connection != lastConnection).toList();

        Map<Connection, Integer> costsList = new HashMap<>();
        for(Connection connection : currentConnections){
            System.out.println("Processing connection " + connection.getConnectedPorts());
            Map.Entry<Connection, Integer> cost = calcPathCostsForConnection(pathCosts, rootBridge, currentSwitch, connection);
            costsList.put(connection, cost != null ? cost.getValue() : null);
            System.out.println("Final costs for connection " + connection.getConnectedPorts() + " is " + (cost != null ? cost.getValue() : null));
        }
        if(costsList.isEmpty()){
            System.out.println("No connections found for switch " + currentSwitch.getName() + " towards root-bridge");
            return null;
        }
        costsList.values().removeIf(Objects::isNull);

        return costsList.entrySet().stream().min(Map.Entry.comparingByValue()).orElse(null);
    }

    private static Map.Entry<Connection, Integer> calcPathCostsForConnection(int pathCosts, Switch rootBridge, Switch currentSwitch, Connection connection) {
        // iteriere durch alle Verbindungen des aktuellen nicht root-Switches
        // error-case: Selbstreferenz
        if(connection.getConnectedPorts().getFirst().contains(currentSwitch.getName()) && connection.getConnectedPorts().getLast().contains(currentSwitch.getName())){
            throw new RuntimeException("project.alg.stp.entity.Connection with connected ports " + connection.getConnectedPorts().getFirst() + " and " + connection.getConnectedPorts().getLast() + " exists");
        }
        // Verbindung zu Root erreicht
        if(connection.getConnectedPorts().getFirst().contains(rootBridge.getName()) || connection.getConnectedPorts().getLast().contains(rootBridge.getName())){
            int costs = pathCosts + connection.getCost();
            System.out.println("Direct connection to root-bridge reached with connection " + connection.getConnectedPorts() + " results in path-costs of " + costs);
            return Map.entry(connection, costs);
        }

        String connectedPortName = connection.getConnectedPorts().stream().filter(port -> !port.contains(currentSwitch.getName())).findFirst().orElse(null);
        String connectedSwitch = connectedPortName.split("/")[0];
        pathCosts += connection.getCost();
        Switch updatedCurrentSwitch = switchList.stream().filter(switchEntity -> switchEntity.getName().contains(connectedSwitch)).findFirst().orElse(null);
        System.out.println("Switch " + currentSwitch.getName() + " has connection to switch " + connectedSwitch);

        return handleConnections(pathCosts, rootBridge, updatedCurrentSwitch, connection);
    }

    private static Switch findRootBridge() {
        Switch rootSwitch = Main.switchList.stream().sorted(Comparator.comparing(Switch::getPriority).thenComparing(Switch::getMacAddress)).toList().getFirst();
        System.out.println("Root bridge is: \n-- Switch " + rootSwitch.getName() + " --");
        return rootSwitch;
    }

    private static List<Switch> initializeSwitches() {
//        Switch switchA = new Switch(40960, 'A', randomMACAddress());
//        Switch switchB = new Switch(32768, 'B', randomMACAddress());
//        Switch switchC = new Switch(36864, 'C', randomMACAddress());
//        Switch switchD = new Switch(32768, 'D', randomMACAddress());
//        Switch switchE = new Switch(40960, 'E', randomMACAddress());
//        Switch switchF = new Switch(32768, 'F', randomMACAddress());
        Switch switchA = new Switch(40960, 'A', "00:01:13:D7:3E:5C");
        Switch switchB = new Switch(32768, 'B', "00:00:11:A3:3E:58");
        Switch switchC = new Switch(36864, 'C', "00:00:13:FF:3E:55");
        Switch switchD = new Switch(32768, 'D', "00:00:12:A5:12:55");
        Switch switchE = new Switch(40960, 'E', "00:01:13:D7:3E:B5");
        Switch switchF = new Switch(32768, 'F', "00:01:13:FF:3E:D5");

        return new ArrayList<>(List.of(switchA, switchB, switchC, switchD, switchE, switchF));
    }

    private static List<Connection> initializeConnections() {
        List<Connection> connectionList = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        boolean moreConnections = true;

        while(moreConnections){
            boolean invalidInput = false;
            int cost = 0;

            System.out.print("Enter connection [e.g.: X/1; Y/2; <COST>]: ");
            String input = scanner.nextLine();
            List<String> inputList = Arrays.asList(input.split("; "));

            try{
                cost = Integer.parseInt(inputList.getLast());
            }
            catch(NumberFormatException e){
                invalidInput = true;
            }
            if(inputList.size() != 3){
                invalidInput = true;
            }

            if(invalidInput){
                System.out.println("Invalid input. Please try again.");
                continue;
            }

            Connection connection = new Connection(cost, List.of(inputList.getFirst(), inputList.get(1)));
            connectionList.add(connection);
            System.out.println("project.alg.stp.entity.Connection " + inputList.getFirst() + " - " + inputList.get(1) + " successfully registered");

            System.out.print("Register another connection? [yes/no]: ");
            String nextConnection = scanner.nextLine();
            if("no".equals(nextConnection)){
                moreConnections = false;
            }
        }

        return connectionList;
    }

    private static void findBlockedPorts() {
        for(Switch switchEntity : switchList) {
            List<Port> ports = switchEntity.getPorts();
            for(Port port : ports){
                if(port.isDesignated() || port.isRoot()){
                    ports.remove(port);
                    port.setIsBlocked(true);
                    ports.add(port);
                }
            }
            switchEntity.setPorts(ports);
        }
    }

    private static void printSwitches() {
        System.out.println("\n------------------------------------");
        for (Switch switchEntity : Main.switchList) {
            System.out.print("| Switch " + switchEntity.getName() + " | " + switchEntity.getMacAddress() + " | " + switchEntity.getPriority() + " | ");
            switchEntity.getPorts().forEach(port -> System.out.print(port.getPortName() + " - " + port.isRoot() + " - " + port.isDesignated() + " - " + port.isBlocked() + " | "));
            System.out.println("\n------------------------------------");
        }
        System.out.println();
    }

    private static void printConnections() {
        System.out.println("\n------------------------------------");
        for (Connection connection : Main.connectionList) {
            System.out.print("| " + connection.getConnectedPorts().getFirst() + " | " + connection.getConnectedPorts().getLast() + " | " + connection.getCost() + " | ");
            System.out.println("\n------------------------------------");
        }
        System.out.println();
    }

    private static List<Connection> findNotRootConnections(Switch rootBridge){
        return connectionList.stream().filter(connection -> !connection.getConnectedPorts().getFirst().contains(rootBridge.getName()) && !connection.getConnectedPorts().getLast().contains(rootBridge.getName())).toList();
    }

    private static void markDesignatedPorts(Switch rootBridge){
        List<Connection> notRootConnections = findNotRootConnections(rootBridge);

        for(Connection connection : notRootConnections){
            List<Switch> currentSwitches = switchList.stream().filter(switchEntity -> connection.getConnectedPorts().toString().contains(switchEntity.getName())).toList();

            System.out.println("Evaluating designated port for connection " + connection.getConnectedPorts().toString());
            Map<Switch, Integer> switchCosts = new HashMap<>();
            for(Switch currentSwitch : currentSwitches){
                Map.Entry<Connection, Integer> connectingPortCost = handleConnections(0, rootBridge, currentSwitch, null);
                Integer cost = connectingPortCost != null ? connectingPortCost.getValue() : null;
                System.out.println("Cost for connecting port of connection " + connection.getConnectedPorts().toString() + " with currentSwitch " + currentSwitch.getName() + " is " + cost);
                switchCosts.put(currentSwitch, cost);
            }

            Map.Entry<Switch, Integer> lowestCostSwitch = switchCosts.entrySet().stream().filter(el -> el.getValue() != null).min(Map.Entry.comparingByValue()).orElse(null);
            System.out.println("Switch with lowest cost is " + lowestCostSwitch.getKey().getName());

            List<Port> originalPorts = lowestCostSwitch.getKey().getPorts();
            List<Port> designatedPortList = originalPorts.stream().filter(port -> connection.getConnectedPorts().contains(port.getPortName())).toList();
            if(designatedPortList.size() != 1){
                throw new IllegalStateException("Found an illegal amount of ports");
            }
            Port designatedPort = designatedPortList.getFirst();
            originalPorts.remove(designatedPort);
            designatedPort.setIsDesignated(true);
            originalPorts.add(designatedPort);
            switchList.remove(lowestCostSwitch.getKey());
            Switch lowestCostSwitchEntity = lowestCostSwitch.getKey();
            lowestCostSwitchEntity.setPorts(originalPorts);
            switchList.add(lowestCostSwitchEntity);
        }
    }

}