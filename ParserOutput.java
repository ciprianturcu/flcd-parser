import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class ParserOutput {
    private LL1 parser;
    private List<Integer> productionsOfSequence;
    private List<Node> nodeList = new ArrayList<>();
    private Node root;
    private String outputPath;
    private Boolean hasConflicts;
    private Integer nodeNumber = 1;

    public ParserOutput(LL1 parser, List<String> sequence, String outputPath) {
        this.parser = parser;
        this.productionsOfSequence = parser.parseSequence(sequence);
        this.hasConflicts = this.productionsOfSequence.contains(-1);
        this.outputPath = outputPath;
        generateParsingTree();
    }

    public void generateParsingTree() {
        if (hasConflicts) return;
        var productionIndex = 0;
        Stack<Node> nodeStack = new Stack<>();
        initializeRootNode(nodeStack);

        //while we still have something to move on
        while (productionIndex < productionsOfSequence.size() && !nodeStack.isEmpty()) {
            Node top = nodeStack.peek();
            if (parser.getGrammar().getTerminals().contains(top.getValue()) || top.getValue().contains("epsilon")) {
                while (!nodeStack.isEmpty() && !nodeStack.peek().getHasRight()) nodeStack.pop();
                if (!nodeStack.isEmpty()) {
                    nodeStack.pop();
                } else {
                    break;
                }
            }

            var production = parser.getProductionRHSByLevel(productionsOfSequence.get(productionIndex));
            nodeNumber += production.size() - 1;
            for (int index = production.size() - 1; index >= 0; index--) {
                Node child = new Node();
                child.setParent(top.getIndex());
                child.setValue(production.get(index));
                child.setIndex(nodeNumber);
                if (index == 0) {
                    child.setSibling(0);
                } else {
                    child.setSibling(nodeNumber - 1);
                }
                child.setHasRight(index != production.size()-1);

                nodeNumber--;
                nodeStack.push(child);
                nodeList.add(child);

            }
            nodeNumber+=production.size()+1;
            productionIndex++;
        }
    }

    public void printTree() {
        try {
            nodeList.sort(Comparator.comparing(Node::getIndex));
            File outputFile = new File(outputPath);
            FileWriter fileWriter = new FileWriter(outputFile, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("Index | Value | Parent | Sibling"+ "\n");
            System.out.println("Index | Value | Parent | Sibling"+ "\n");
            for(Node node : nodeList) {
                bufferedWriter.write(node.getIndex() + " | " + node.getValue() + " | " + node.getParent() + " | " + node.getSibling() + "\n");
                System.out.println(node.getIndex() + " | " + node.getValue() + " | " + node.getParent() + " | " + node.getSibling() + "\n");
            }
            bufferedWriter.close();



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeRootNode(Stack<Node> nodeStack) {
        Node node = new Node();
        node.setParent(0);
        node.setSibling(0);
        node.setHasRight(false);
        node.setIndex(nodeNumber);
        this.nodeNumber++;
        node.setValue(parser.getGrammar().getStartSymbol());
        nodeStack.push(node);
        nodeList.add(node);
        this.root = node;
    }
}
