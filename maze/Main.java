package maze;

import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random(); 
        Menu menu = new Menu();
        Action action = new Action(random);

        int menuNo = -1;
        while (menuNo != 0) {
            if (action.isFullMenu()) {
                menuNo = menu.getMenuNoFull(scanner);
            } else {
                menuNo = menu.getMenuNo(scanner);
            }
            switch (menuNo) {
                case 1:
                    action.generate(scanner);
                    break;
                case 2:
                    action.load(scanner);
                    break;
                case 3:
                    action.save(scanner);
                    break;
                case 4:
                    action.display();
                    break;
                case 5:
                    action.findEscape();
                    break;
                case 0:
                    action.exit();
                    break;    
                default:
                    break;
            }
            if (menuNo != 0) {
                System.out.println();
            }
        }

        scanner.close();
    }
}

class Action {
    Maze maze;

    Action(Random random) {
        maze = new Maze(random);
    }

    void generate(Scanner scanner) {
        System.out.println("Enter the size of a new maze");
        maze.generate(scanner);
        maze.display();
    }

    void load(Scanner scanner) {
        maze.load(scanner);
    }

    void save(Scanner scanner) {
        maze.save(scanner);
    }

    void display() {
        maze.display();
    }

    void findEscape() {
        maze.findEscape();
    }

    void exit() {
        System.out.println("Bye!");
    }

    boolean isFullMenu() {
        if (maze.pathList.isEmpty()) {
            return false;
        }
        return true;
    }
}

class Menu {
    
    int getMenuNoFull(Scanner scanner) {
        System.out.println("=== Menu ===");
        System.out.println("1. Generate a new maze");
        System.out.println("2. Load a maze");
        System.out.println("3. Save the maze");
        System.out.println("4. Display the maze");
        System.out.println("5. Find the escape");
        System.out.println("0. Exit");

        int menuNo = -1;
        while (menuNo == -1) {
            String input = scanner.nextLine();
            if ("".equals(input)) {
                displayErrorMsg();
                continue;
            }
            if (!input.matches("[0-5]")) {
                displayErrorMsg();
                continue;
            }
            menuNo = Integer.parseInt(input);
        }
        return menuNo;
    }

    int getMenuNo(Scanner scanner) {
        System.out.println("=== Menu ===");
        System.out.println("1. Generate a new maze");
        System.out.println("2. Load a maze");
        System.out.println("0. Exit");

        int menuNo = -1;
        while (menuNo == -1) {
            String input = scanner.nextLine();
            if ("".equals(input)) {
                displayErrorMsg();
                continue;
            }
            if (!input.matches("[0-2]")) {
                displayErrorMsg();
                continue;
            }
            menuNo = Integer.parseInt(input);
        }
        return menuNo;
    }

    void displayErrorMsg() {
        System.out.println("Incorrect option. Please try again");
    }
}

class Maze {
    Random random;
    int inputN;
    int n;
    int m;
    int nm;
    ArrayList<Link> pathList;
    ArrayList<Node> treeNodes;
    ArrayList<Link> linkList;
    ArrayList<Node> escPathList;
    EscNode[][] escNodes;
    Node entrance;
    Node exit; 

    Maze(Random random) {
        this.random = random;
        pathList = new ArrayList<>();
        treeNodes = new ArrayList<>();
        linkList = new ArrayList<>();
        escPathList = new ArrayList<>();
        escNodes = new EscNode[0][0];
    }

    void generate(Scanner scanner) {
        pathList.clear();
        treeNodes.clear();
        linkList.clear();
    
        String input = scanner.nextLine();
        inputN = Integer.parseInt(input);
        n = (inputN - 2 + 1) / 2;
        m = (inputN - 2 + 1) / 2;
        nm = n * m;

        for (int i = 0; i < n; i++) {
            for(int j = 0; j < m - 1; j++) {
                Node from = new Node(i,  j);
                Node to = new Node(i, j + 1);
                int weight = (Math.abs(random.nextInt()) % nm) + 1;
                linkList.add(new Link(from, to, weight));
            }
        }

        for (int j = 0; j < m; j++) {
            for(int i = 0; i < n - 1; i++) {
                Node from = new Node(i,  j);
                Node to = new Node(i + 1, j);
                int weight = (Math.abs(random.nextInt()) % nm) + 1;
                linkList.add(new Link(from, to, weight));
            }
        }

        primeSpanningTree(new Node(0, 0));

        int entranceColumn = Math.abs(random.nextInt()) % n;
        int exitColumn = Math.abs(random.nextInt()) % n;

        entrance = new Node(0 , entranceColumn);
        exit = new Node(n - 1, exitColumn);
    }

    void findEscape() {
        escPathList.clear();
        escNodes = new EscNode[n][m];

        dijkstraAlgorithm();

        displayEscape();
    }

    void dijkstraAlgorithm() {

        int distance;
        for (int i = 0; i < n; i++) {
            for(int j = 0; j < m; j++) {
                if (i == entrance.row && j == entrance.column) {
                    distance = 0;
                } else {
                    distance = Integer.MAX_VALUE;
                }
                escNodes[i][j] = new EscNode(i, j, distance);
            }
        }

        for (int i = 0; i < nm - 1; i++) {
//            System.out.println(i);
            EscNode minNode = getMinDistanceNode();
            ArrayList<Node> neighbors = getAdjacentNodes(minNode);
            for (Node node: neighbors) {
                EscNode escNode = escNodes[node.row][node.column];
                if (!escNode.unprocessed) {
                    continue;
                }
                if (!isExistInPathList(minNode, node)) {
                    continue;
                }
                if (minNode.distance + 1 < escNode.distance) {
                    escNode.distance = minNode.distance + 1;
                    escNode.previous = minNode;
                }
            }
            minNode.unprocessed = false;
        }

        EscNode node = escNodes[exit.row][exit.column];
        while (!entrance.equals(node)) {
           escPathList.add(node);
           node = node.previous;
        }
        escPathList.add(node);
    }

    boolean isExistInPathList(Node from, Node to) {
        for (Link p: pathList) {
            if (p.from.equals(from) && p.to.equals(to) || p.to.equals(from) && p.from.equals(to)) {
                return true;
            }
        }
        return false;
    }

    EscNode getMinDistanceNode() {
        int minDistance = Integer.MAX_VALUE;
        EscNode minNode = null;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                EscNode t = escNodes[i][j];
                if (!t.unprocessed) {
                   continue;
                }
                if (t.distance < minDistance) {
                    minDistance = t.distance;
                    minNode = t;
                }
            }
        }
        return minNode;
    } 

    void primeSpanningTree(Node node) {
        Node targetNode = node.clone();
        treeNodes.add(targetNode);
        for (int i = 0; i < nm - 1; i++) {
            Link link = getMinWeightLink();
            pathList.add(link);
            targetNode = link.to;
            treeNodes.add(targetNode);
        }
    }
    
    Link getMinWeightLink() {
        int minWeight = Integer.MAX_VALUE;
        Link minLink = null;
        for (Node from: treeNodes) {
            ArrayList<Node> list = getAdjacentNodes(from);
            for (Node to: list) {
                Link a = getLink(from, to); 
                if (a == null) {
                    continue;
                }   
                if (isContainsInTreeNodes(to)) {
                    continue;
                }
                if (a.weight < minWeight) {
                    minWeight = a.weight;
                    minLink = a;
                }            
            }
        }
        return minLink;
    }
 
    boolean isContainsInTreeNodes(Node node) {
        for (Node t: treeNodes) {
            if (node.equals(t)) {
                return true;
            }
        }
        return false;
    }

    boolean isContainsInPathList(Link link) {
        for (Link p: pathList) {
            if (link.equals(p)) {
                return true;
            }
        }
        return false;
    }

    void addAdjacentNodes(Node node) {
        ArrayList<Node> list = getAdjacentNodes(node);
        for (Node t: list) {
            if (!treeNodes.contains(t)) {
                treeNodes.add(t);
            }
        }
    }

    ArrayList<Node> getAdjacentNodes(Node node) {
        int row = node.row;
        int column = node.column;
        Node[] candidateList = new Node[4];
        Node up = getNode(row - 1, column);
        Node dn = getNode(row + 1, column);
        Node right = getNode(row, column + 1);
        Node left = getNode(row, column - 1);
        candidateList[0] = up;
        candidateList[1] = dn;
        candidateList[2] = right;
        candidateList[3] = left; 

        ArrayList<Node> list = new ArrayList<>();
        for (Node t: candidateList) {
            if (t != null) {
                list.add(t);
            }
        }
        return list; 
    }

    Node getNode(int row, int column) {
        if (row < 0 || column < 0 || row >= n || column >= m) {
            return null;
        }
        return new Node(row, column);
    }

    Link getLink(Node from, Node to) {
        for (Link a: linkList) {
            if (a.from.equals(from) && a.to.equals(to)) {
                return a;
            }
        }
        return null;
    }

    int getWeight(Node from, Node to) {
        for (Link a: linkList) {
            if (a.from == from && a.to == to) {
                return a.weight;
            }
        }
        return Integer.MAX_VALUE;
    }

    void kruskalSpanningTree() {

    }

    void displayEscape() {
        char[][] img = createImg();
        addEscape(img);
        displayImg(img);
    }

    void addEscape(char[][] img) {
        Node prev = new Node(exit.row + 1, exit.column);
        for (Node p: escPathList) {
            int i = graphToImg(p.row);
            int j = graphToImg(p.column);
            img[i][j] = '+';
            if (prev.row == p.row) {
                img[i][j + prev.column - p.column] = '+';
            } else {
                img[i + prev.row - p.row][j] = '+';    
            }
            prev = p;
        }
        img[graphToImg(entrance.row) - 1][graphToImg(entrance.column)] = '+';
    }

    void display() {
        char[][] img = createImg();
        displayImg(img);
/*
        for (Link p: pathList) {
            System.out.println(String.format("(%d %d)-(%d %d) %d", p.from.row, p.from.column, p.to.row, p.to.column, p.weight));
        } 
*/
    }

    char[][] createImg() {
        int n1 = inputN;
        char[][] img = new char[n1][n1];

        for (Link p: pathList) {
            int i;
            int j;
            if (p.from.row == p.to.row) {
                i = graphToImg(p.from.row);
                j = graphToImg(p.from.column);
                img[i][j++] = '*';
                img[i][j++] = '*';
                img[i][j] = '*';
            } else {
                i = graphToImg(p.from.row);
                j = graphToImg(p.from.column);
                img[i++][j] = '*';
                img[i++][j] = '*';
                img[i][j] = '*';
            }
        }

        img[0][graphToImg(entrance.column)] = '*';
        img[inputN - 1][graphToImg(exit.column)] = '*';

        return img;
    }

    void displayImg(char[][] img) {
        int n1 = inputN;

        for (int i = 0; i < n1; i++) {
            String line = "";
            for (int j = 0; j < n1; j++) {
                switch (img[i][j]) {
                    case 0:
                        line += "\u2588\u2588";
                        break;
                    case '+':
                        line += "//";
                        break;
                    default:
                        line += "  ";
                        break;  
                }
            }
            System.out.println(line);
        }
    }

    int graphToImg(int index) {
        return index * 2 + 1;
    }

    int imgToGraph(int index) {
        return (index - 1) / 2; 
    }
 
    void save(Scanner scanner) {
    
        String fileName = scanner.nextLine();
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            writer.println("maze1.0");
            writer.println(inputN);
            writer.println(n);
            writer.println(entrance);
            writer.println(exit);
            for (Link rec: pathList) {
                writer.println(rec.toString());
            }
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }  
    }

    void load(Scanner scanner) {
        String fileName = scanner.nextLine();
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(String.format("The file %s does not exist", fileName));
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String id = reader.readLine();
            if (!"maze1.0".equals(id)) {
                System.out.println("Cannot load the maze. It has an invalid format");
                reader.close();
                return;
            }
            inputN = Integer.parseInt(reader.readLine());
            n = Integer.parseInt(reader.readLine());
            m = n;
            nm = n* m;
            entrance = new Node(reader.readLine());
            exit = new Node(reader.readLine());
            pathList.clear();
            String rec;
            while ((rec = reader.readLine()) != null) {
                pathList.add(new Link(rec));
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}

class Node {
    int row;
    int column;

    Node(int row, int column) {
        this.row = row;
        this.column = column;
    }

    Node(String rec) {
        String[] strs = rec.split(",");
        this.row = Integer.parseInt(strs[0]);
        this.column = Integer.parseInt(strs[1]);
    }

    public boolean equals(Node node) {
        if (node.row == this.row && node.column == this.column) {
            return true;
        }
        return false;
    }

    public Node clone() {
        return new Node(this.row, this.column);
    }

    public String toString() {
        return String.format("%d,%d", row, column);
    }
}

class Link {
    Node from;
    Node to;
    int weight;

    Link(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    Link(String rec) {
        String[] strs = rec.split(",");
        int row = Integer.parseInt(strs[0]);
        int column = Integer.parseInt(strs[1]);
        this.from = new Node(row, column);
        row = Integer.parseInt(strs[2]);
        column = Integer.parseInt(strs[3]);
        this.to = new Node(row, column);
        this.weight = Integer.parseInt(strs[4]);
    }

    public boolean equals(Link link) {
        if (link.from == this.from && link.to == this.to && link.weight == this.weight) {
            return true;
        }
        return false;
    }

    public String toString() {
        return String.format("%d,%d,%d,%d,%d", from.row, from.column, to.row, to.column, weight);
    }
} 

class EscLink {
    Node from;
    Node to;
    Node prev;

    EscLink(Node from, Node to) {
        this.from = from;
        this.to = to;
    }
}

class EscNode extends Node{
    int distance;
    boolean unprocessed = true;
    EscNode previous;

    EscNode(int row, int column, int distance) {
        super(row, column);
        this.distance = distance;
    }
}
