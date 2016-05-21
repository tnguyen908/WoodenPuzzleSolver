
package algorithmsproject;

import java.util.Queue;
import java.util.Stack;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.ListIterator;

/**
 *
 * @author Tri Nguyen and Marcelo Lozoya
 */
class AlgorithmsProject {
    
    //Rows and columns are zero-based
    private static final int maxRows = 3;
    private static final int maxColumns = 4;
    private static final int totalPieces = 12; //includes empty spaces
    
    //In java, an int's most significant bit is the 31st bit(zero based).
    //The 32nd bit toggles positive and negative.
    private static final int mostSigBit = 30;
    
    //List that contains all of the previously seen board configurations
    private static LinkedList<ConfigNode> configList = new LinkedList<>();
    
    //Queue that holds all of the next possible moves
    private static Queue<ConfigNode> moveQueue = new LinkedList<>();
    
    //Stack that holds strings of the moves to get to the winning configuration
    private static Stack<String> moveStack = new Stack();

    //This method is the starting point for the puzzle solver
    public static void startSolver(){
        char[][] initialConfigArray = getUserInitialConfig();
        System.out.println("\nThank You...Currently solving puzzle...\n");
        //int initialConfiguration = 1456891232;
        int initialConfiguration = arrayToHuffCode(initialConfigArray);
        int winningConfig = 0;
        ConfigNode initial = new ConfigNode(initialConfiguration, -1);
        ConfigNode currentNode;
        
        //Add the node for the initial configuration to the list of previously
        //seen configurations. This node's parent configuration is set to -1,
        //which we will use as the stopping condition as we follow the path 
        //from the winning configuration to the initial configuration
        if(!configList.add(initial)){
            System.out.println("Error adding to configList...exiting...");
            System.exit(0);
        }
        
        //Get the first round of moves to prime the queue for the while loop
        getMoves(initialConfiguration);
        
        while(!moveQueue.isEmpty()){
            
            currentNode = moveQueue.poll();
            if(isWin(currentNode.config)){
                winningConfig = currentNode.config;
                break;
            }
            getMoves(currentNode.config);
            
        }
        traceToInitialConfig(winningConfig);
        int totalNumMoves = moveStack.size();
        printStack();
        System.out.println("\nTotal Number of Moves: "+totalNumMoves+"\n");
    }
    
    //Method to get the starting board configuration from the user
    public static char[][] getUserInitialConfig(){
        char[][] configMap = new char[maxRows+1][maxColumns+1];
        Scanner user = new Scanner(System.in);
        String input;
        int aPieceCounter = 0;
        int bPieceCounter = 0;
        int cPieceCounter = 0;
        int dPieceCounter = 0;
        int ePieceCounter = 0;
        
        System.out.println("\n*****WOODEN PUZZLE SOLVER*****\n");
        String prompt1 = "Please input the piece type at the given coordinates";
        String prompt2 = "--Coordinates are designated by (row, column)--";
        String prompt3 = "--Coordinate (0,0) is the top left square--";
        String prompt4 = "--Coordinate (3,4) is the bottom right square--";
        String prompt5 = "--Your input is not case-sensitive--\n";
        System.out.println(prompt1);
        System.out.println("\nNotes:");
        System.out.println(prompt2);
        System.out.println(prompt3);
        System.out.println(prompt4);
        System.out.println(prompt5);
        
        System.out.println(" -------------------------");
        System.out.println("|         Legend          |");
        System.out.println("|-------------------------|");
        System.out.println("| A = 2-wide piece        |");
        System.out.println("| B = 2-tall piece        |");
        System.out.println("| C = 4-square piece      |");
        System.out.println("| D = single square piece |");
        System.out.println("| E = empty square        |");
        System.out.println(" -------------------------\n");
        
        //Go through each space and fill in the empty ones with pieces that
        //are input from the user
        for(int i=0; i<=maxRows; i++){
            for(int j=0; j<=maxColumns; j++){
                //Find an empty space
                if(configMap[i][j] == '\u0000'){
                    System.out.println("Piece at ("+i+","+j+")? ");
                    input = user.next();
                    input = input.toUpperCase();
                    
                    boolean inputOK = false;
                    
                    //Check if input is usable, if not, ask user again
                    while(!inputOK){
                        //Check if input is empty
                        if(input.isEmpty()){
                            System.out.println("Input is empty, try again");
                            System.out.println("Piece at ("+i+","+j+")? ");
                            input = user.next();
                            input = input.toUpperCase();
                        }
                        //Check if input is longer than a single char
                        else if(input.length() > 1){
                            System.out.println("Input must be 1 character"
                                    + ", try again");
                            System.out.println("Piece at ("+i+","+j+")? ");
                            input = user.next();
                            input = input.toUpperCase();
                        }
                        //Check if input is a valid piece designation
                        else if(!input.equals("A") && !input.equals("B") && 
                                !input.equals("C") && !input.equals("D") &&
                                !input.equals("E")){
                            System.out.println("Input must be a letter from A-E"
                                    + ", try again");
                            System.out.println("Piece at ("+i+","+j+")? ");
                            input = user.next();
                            input = input.toUpperCase();
                        }
                        //Input is OK, place piece into configMap
                        else{
                            char inputChar = input.charAt(0);
                            
                            if(inputChar == 'A'){
                                if(aPieceCounter == 4){
                                    System.out.print("Too many A pieces, ");
                                    System.out.print("please try again\n");
                                    if(j>0) j--;
                                    else if(j==0){i--; j=3;}
                                    break;
                                }
                                configMap[i][j] = inputChar;
                                configMap[i][j+1] = inputChar;
                                aPieceCounter++;
                            }
                            else if(inputChar == 'B'){
                                if(bPieceCounter == 1){
                                    System.out.print("Too many B pieces, ");
                                    System.out.print("please try again\n");
                                    if(j>0) j--;
                                    else if(j==0){i--; j=3;}
                                    break;
                                }
                                configMap[i][j] = inputChar;
                                configMap[i+1][j] = inputChar;
                                bPieceCounter++;
                            }
                            else if(inputChar == 'C'){
                                if(cPieceCounter == 1){
                                    System.out.print("Too many C pieces, ");
                                    System.out.print("please try again\n");
                                    if(j>0) j--;
                                    else if(j==0){i--; j=3;}
                                    break;
                                }
                                configMap[i][j] = inputChar;
                                configMap[i+1][j] = inputChar;
                                configMap[i][j+1] = inputChar;
                                configMap[i+1][j+1] = inputChar;
                                cPieceCounter++;
                            }
                            else if(inputChar == 'D'){
                                if(dPieceCounter == 4){
                                    System.out.print("Too many D pieces, ");
                                    System.out.print("please try again\n");
                                    if(j>0) j--;
                                    else if(j==0){i--; j=3;}
                                    break;
                                }
                                configMap[i][j] = inputChar;
                                dPieceCounter++;
                            }
                            else if(inputChar == 'E'){
                                if(ePieceCounter == 2){
                                    System.out.print("Too many E pieces, ");
                                    System.out.print("please try again\n");
                                    if(j>0) j--;
                                    else if(j==0){i--; j=3;}
                                    break;
                                }
                                configMap[i][j] = inputChar;
                                ePieceCounter++;
                            }
                            inputOK = true;
                        }
                    } //End inputOK while loop
                } //End null char if statement
            } //End column for-loop
        } //End row for-loop
        
        return configMap;
    }
    
    //Method that finds the possible moves, given a configuration. Puts new
    //non-duplicate moves into the configList and moveQueue
    public static void getMoves(int parentConfig){
        char[][] parentArray;
        char[][] parentArrayCopy;
        
        //We need 2 copies of the parentArray configuration. One to pass to
        //getMovesForPieceAtCoordinates(), and another that we can mark up
        //as we visit each piece on the board
        parentArray = huffCodeToArray(parentConfig);
        parentArrayCopy = huffCodeToArray(parentConfig);

        //Were going to visit every piece on the board and find all the 
        //valid moves for the parent configuration. We mark visited pieces
        //by putting X's where the piece was set
        for(int i=0; i<=maxRows; i++){
            for(int j=0; j<=maxColumns; j++){
                if(parentArray[i][j] != 'X'){
                    char currentPiece = parentArray[i][j];
                    getMovesForPieceAtCoordinates(parentArrayCopy, currentPiece, parentConfig, i, j);
                    
                    //Mark visited pieces
                    if(currentPiece == 'A'){
                        parentArray[i][j] = 'X';
                        parentArray[i][j+1] = 'X';
                    }
                    else if(currentPiece == 'B'){
                        parentArray[i][j] = 'X';
                        parentArray[i+1][j] = 'X';
                    }
                    else if(currentPiece == 'C'){
                        parentArray[i][j] = 'X';
                        parentArray[i][j+1] = 'X';
                        parentArray[i+1][j] = 'X';
                        parentArray[i+1][j+1] = 'X';
                    }
                    else if(currentPiece == 'D'){
                        parentArray[i][j] = 'X';
                    }
                    else{
                        parentArray[i][j] = 'X';
                    }
                }
            }
        }
    } //End getMoves()
    
    //Helper method for getMoves(). Made this method so the code in getMoves()
    //wouldn't get too cluttered
    public static void getMovesForPieceAtCoordinates(char[][] parentArray, char currentPiece, int parentConfig, int row, int col){
        
        //Condition for an empty space (do nothing)
        if(currentPiece == 'E'){
            //do nothing
        }
        //Condition for a 2-wide piece
        else if(currentPiece == 'A'){
            //Look for a move up
            if(row-1>=0 && parentArray[row-1][col]=='E' && 
                    parentArray[row-1][col+1]=='E'){
                
                char[][] newConfigArrayUp = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayUp[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to up
                newConfigArrayUp[row-1][col] = 'A';
                newConfigArrayUp[row-1][col+1] = 'A';
                //Empty the it's previous place
                newConfigArrayUp[row][col] = 'E';
                newConfigArrayUp[row][col+1] = 'E';
                
                int newConfigUp = arrayToHuffCode(newConfigArrayUp);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigUp)){
                    String moveUp = "Move piece at coordinates (";
                    moveUp += row;
                    moveUp += ",";
                    moveUp += col;
                    moveUp += ") one unit up";
                    ConfigNode upMove = new ConfigNode(newConfigUp, parentConfig);
                    upMove.move = moveUp;
                    handleNewValidMove(upMove);
                }
            }
            //Look for a move right
            if(col+2<=maxColumns && parentArray[row][col+2]=='E'){
                
                char[][] newConfigArrayRt = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayRt[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to right
                newConfigArrayRt[row][col+2] = 'A';
                //Empty the it's previous place
                newConfigArrayRt[row][col] = 'E';
                
                int newConfigRt = arrayToHuffCode(newConfigArrayRt);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigRt)){
                    String moveRt = "Move piece at coordinates (";
                    moveRt += row;
                    moveRt += ",";
                    moveRt += col;
                    moveRt += ") one unit right";
                    ConfigNode rightMove = new ConfigNode(newConfigRt, parentConfig);
                    rightMove.move = moveRt;
                    handleNewValidMove(rightMove);
                }
            }
            //Look for a move down
            if(row+1<=maxRows && parentArray[row+1][col]=='E' && 
                    parentArray[row+1][col+1]=='E'){
                
                char[][] newConfigArrayDwn = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayDwn[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to down
                newConfigArrayDwn[row+1][col] = 'A';
                newConfigArrayDwn[row+1][col+1] = 'A';
                //Empty the it's previous place
                newConfigArrayDwn[row][col] = 'E';
                newConfigArrayDwn[row][col+1] = 'E';
                
                int newConfigDwn = arrayToHuffCode(newConfigArrayDwn);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigDwn)){
                    String moveDwn = "Move piece at coordinates (";
                    moveDwn += row;
                    moveDwn += ",";
                    moveDwn += col;
                    moveDwn += ") one unit down";
                    ConfigNode dwnMove = new ConfigNode(newConfigDwn, parentConfig);
                    dwnMove.move = moveDwn;
                    handleNewValidMove(dwnMove);
                }
            }
            //Look for a move left
            if(col-1>=0 && parentArray[row][col-1]=='E'){
                
                char[][] newConfigArrayLft = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayLft[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to left
                newConfigArrayLft[row][col-1] = 'A';
                //Empty the it's previous place
                newConfigArrayLft[row][col+1] = 'E';
                
                int newConfigLft = arrayToHuffCode(newConfigArrayLft);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigLft)){
                    String moveLft = "Move piece at coordinates (";
                    moveLft += row;
                    moveLft += ",";
                    moveLft += col;
                    moveLft += ") one unit left";
                    ConfigNode leftMove = new ConfigNode(newConfigLft, parentConfig);
                    leftMove.move = moveLft;
                    handleNewValidMove(leftMove);
                }
            }
        }
        
        //Condition for the 2-tall piece
        else if(currentPiece == 'B'){
            //Look for a move up
            if(row-1>=0 && parentArray[row-1][col]=='E'){
                
                char[][] newConfigArrayUp = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayUp[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to up
                newConfigArrayUp[row-1][col] = 'B';
                //Empty the it's previous place
                newConfigArrayUp[row+1][col] = 'E';
                
                int newConfigUp = arrayToHuffCode(newConfigArrayUp);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigUp)){
                    String moveUp = "Move piece at coordinates (";
                    moveUp += row;
                    moveUp += ",";
                    moveUp += col;
                    moveUp += ") one unit up";
                    ConfigNode upMove = new ConfigNode(newConfigUp, parentConfig);
                    upMove.move = moveUp;
                    handleNewValidMove(upMove);
                }
            }
            //Look for a move right
            if(col+1<=maxColumns && parentArray[row][col+1]=='E' && 
                    parentArray[row+1][col+1]=='E'){
                
                char[][] newConfigArrayRt = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayRt[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to right
                newConfigArrayRt[row][col+1] = 'B';
                newConfigArrayRt[row+1][col+1] = 'B';
                //Empty the it's previous place
                newConfigArrayRt[row][col] = 'E';
                newConfigArrayRt[row+1][col] = 'E';
                
                int newConfigRt = arrayToHuffCode(newConfigArrayRt);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigRt)){
                    String moveRt = "Move piece at coordinates (";
                    moveRt += row;
                    moveRt += ",";
                    moveRt += col;
                    moveRt += ") one unit right";
                    ConfigNode rightMove = new ConfigNode(newConfigRt, parentConfig);
                    rightMove.move = moveRt;
                    handleNewValidMove(rightMove);
                }
            }
            //Look for a move down
            if(row+2<=maxRows && parentArray[row+2][col]=='E'){
                
                char[][] newConfigArrayDwn = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayDwn[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to down
                newConfigArrayDwn[row+2][col] = 'B';
                //Empty the it's previous place
                newConfigArrayDwn[row][col] = 'E';
                
                int newConfigDwn = arrayToHuffCode(newConfigArrayDwn);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigDwn)){
                    String moveDwn = "Move piece at coordinates (";
                    moveDwn += row;
                    moveDwn += ",";
                    moveDwn += col;
                    moveDwn += ") one unit down";
                    ConfigNode downMove = new ConfigNode(newConfigDwn, parentConfig);
                    downMove.move = moveDwn;
                    handleNewValidMove(downMove);
                }
            }
            //Look for a move left
            if(col-1>=0 && parentArray[row][col-1]=='E' && 
                    parentArray[row+1][col-1]=='E'){
                
                char[][] newConfigArrayLft = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayLft[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to left
                newConfigArrayLft[row][col-1] = 'B';
                newConfigArrayLft[row+1][col-1] = 'B';
                //Empty the it's previous place
                newConfigArrayLft[row][col] = 'E';
                newConfigArrayLft[row+1][col] = 'E';
                
                int newConfigLft = arrayToHuffCode(newConfigArrayLft);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigLft)){
                    String moveLft = "Move piece at coordinates (";
                    moveLft += row;
                    moveLft += ",";
                    moveLft += col;
                    moveLft += ") one unit left";
                    ConfigNode leftMove = new ConfigNode(newConfigLft, parentConfig);
                    leftMove.move = moveLft;
                    handleNewValidMove(leftMove);
                }
            }
        }
        
        //Condition for the 4-square piece
        else if(currentPiece == 'C'){
            //Look for a move up
            if(row-1>=0 && parentArray[row-1][col]=='E' && 
                    parentArray[row-1][col+1]=='E'){
                
                char[][] newConfigArrayUp = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayUp[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to up
                newConfigArrayUp[row-1][col] = 'C';
                newConfigArrayUp[row-1][col+1] = 'C';
                //Empty the it's previous place
                newConfigArrayUp[row+1][col] = 'E';
                newConfigArrayUp[row+1][col+1] = 'E';
                
                int newConfigUp = arrayToHuffCode(newConfigArrayUp);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigUp)){
                    String moveUp = "Move piece at coordinates (";
                    moveUp += row;
                    moveUp += ",";
                    moveUp += col;
                    moveUp += ") one unit up";
                    ConfigNode upMove = new ConfigNode(newConfigUp, parentConfig);
                    upMove.move = moveUp;
                    handleNewValidMove(upMove);
                }
            }
            //Look for a move right
            if(col+2<=maxColumns && parentArray[row][col+2]=='E' && 
                    parentArray[row+1][col+2]=='E'){
                
                char[][] newConfigArrayRt = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayRt[i][j] = parentArray[i][j];
                    }
                }
              
                //Move piece to right
                newConfigArrayRt[row][col+2] = 'C';
                newConfigArrayRt[row+1][col+2] = 'C';
                //Empty the it's previous place
                newConfigArrayRt[row][col] = 'E';
                newConfigArrayRt[row+1][col] = 'E';
                
                int newConfigRt = arrayToHuffCode(newConfigArrayRt);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigRt)){
                    String moveRt = "Move piece at coordinates (";
                    moveRt += row;
                    moveRt += ",";
                    moveRt += col;
                    moveRt += ") one unit right";
                    ConfigNode rightMove = new ConfigNode(newConfigRt, parentConfig);
                    rightMove.move = moveRt;
                    handleNewValidMove(rightMove);
                }
            }
            //Look for a move down
            if(row+2<=maxRows && parentArray[row+2][col]=='E' && 
                    parentArray[row+2][col+1]=='E'){
                
                char[][] newConfigArrayDwn = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayDwn[i][j] = parentArray[i][j];
                    }
                }
              
                //Move piece to down
                newConfigArrayDwn[row+2][col] = 'C';
                newConfigArrayDwn[row+2][col+1] = 'C';
                //Empty the it's previous place
                newConfigArrayDwn[row][col] = 'E';
                newConfigArrayDwn[row][col+1] = 'E';
                
                int newConfigDwn = arrayToHuffCode(newConfigArrayDwn);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigDwn)){
                    String moveDwn = "Move piece at coordinates (";
                    moveDwn += row;
                    moveDwn += ",";
                    moveDwn += col;
                    moveDwn += ") one unit down";
                    ConfigNode downMove = new ConfigNode(newConfigDwn, parentConfig);
                    downMove.move = moveDwn;
                    handleNewValidMove(downMove);
                }
            }
            //Look for a move left
            if(col-1>=0 && parentArray[row][col-1]=='E' && 
                    parentArray[row+1][col-1]=='E'){
                
                char[][] newConfigArrayLft = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayLft[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to left
                newConfigArrayLft[row][col-1] = 'C';
                newConfigArrayLft[row+1][col-1] = 'C';
                //Empty the it's previous place
                newConfigArrayLft[row][col+1] = 'E';
                newConfigArrayLft[row+1][col+1] = 'E';
                
                int newConfigLft = arrayToHuffCode(newConfigArrayLft);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigLft)){
                    String moveLft = "Move piece at coordinates (";
                    moveLft += row;
                    moveLft += ",";
                    moveLft += col;
                    moveLft += ") one unit left";
                    ConfigNode leftMove = new ConfigNode(newConfigLft, parentConfig);
                    leftMove.move = moveLft;
                    handleNewValidMove(leftMove);
                }
            }
        }
        
        //Condition for the single square pieces
        else if(currentPiece == 'D'){
            //Look for a move up
            if(row-1>=0 && parentArray[row-1][col]=='E'){
                
                char[][] newConfigArrayUp = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayUp[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to up
                newConfigArrayUp[row-1][col] = 'D';
                //Empty the it's previous place
                newConfigArrayUp[row][col] = 'E';
                
                int newConfigUp = arrayToHuffCode(newConfigArrayUp);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigUp)){
                    String moveUp = "Move piece at coordinates (";
                    moveUp += row;
                    moveUp += ",";
                    moveUp += col;
                    moveUp += ") one unit up";
                    ConfigNode upMove = new ConfigNode(newConfigUp, parentConfig);
                    upMove.move = moveUp;
                    handleNewValidMove(upMove);
                }
            }
            //Look for a move right
            if(col+1<=maxColumns && parentArray[row][col+1]=='E'){
                
                char[][] newConfigArrayRt = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayRt[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to right
                newConfigArrayRt[row][col+1] = 'D';
                //Empty the it's previous place
                newConfigArrayRt[row][col] = 'E';
                
                int newConfigRt = arrayToHuffCode(newConfigArrayRt);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigRt)){
                    String moveRt = "Move piece at coordinates (";
                    moveRt += row;
                    moveRt += ",";
                    moveRt += col;
                    moveRt += ") one unit right";
                    ConfigNode rightMove = new ConfigNode(newConfigRt, parentConfig);
                    rightMove.move = moveRt;
                    handleNewValidMove(rightMove);
                }
            }
            //Look for a move down
            if(row+1<=maxRows && parentArray[row+1][col]=='E'){
                
                char[][] newConfigArrayDwn = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayDwn[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to down
                newConfigArrayDwn[row+1][col] = 'D';
                //Empty the it's previous place
                newConfigArrayDwn[row][col] = 'E';
                
                int newConfigDwn = arrayToHuffCode(newConfigArrayDwn);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigDwn)){
                    String moveDwn = "Move piece at coordinates (";
                    moveDwn += row;
                    moveDwn += ",";
                    moveDwn += col;
                    moveDwn += ") one unit down";
                    ConfigNode downMove = new ConfigNode(newConfigDwn, parentConfig);
                    downMove.move = moveDwn;
                    handleNewValidMove(downMove);
                }
            }
            //Look for a move left
            if(col-1>=0 && parentArray[row][col-1]=='E'){
                
                char[][] newConfigArrayLft = new char[maxRows+1][maxColumns+1];
                
                //Make copy of parentArray to work with
                for(int i=0; i<=maxRows; i++){
                    for(int j=0; j<=maxColumns; j++){
                        newConfigArrayLft[i][j] = parentArray[i][j];
                    }
                }
                
                //Move piece to left
                newConfigArrayLft[row][col-1] = 'D';
                //Empty the it's previous place
                newConfigArrayLft[row][col] = 'E';
                
                int newConfigLft = arrayToHuffCode(newConfigArrayLft);
                //Check if newConfig is in the linked list
                if(!isConfigInList(newConfigLft)){
                    String moveLft = "Move piece at coordinates (";
                    moveLft += row;
                    moveLft += ",";
                    moveLft += col;
                    moveLft += ") one unit left";
                    ConfigNode leftMove = new ConfigNode(newConfigLft, parentConfig);
                    leftMove.move = moveLft;
                    handleNewValidMove(leftMove);
                }
            }
        }
    } //End getMovesForPieceAtCoordinates()
    
    //Method to check for a winning configuration given huff-coded int
    public static boolean isWin(int config){
        
        char[][] configArray = huffCodeToArray(config);
        
        //Check following coordinate will each have a C in them if it is a
        //winning configuration
        if(configArray[1][3] == 'C' && configArray[1][4] == 'C' &&
                configArray[2][3] == 'C' && configArray[2][4] == 'C'){
            return true;
        }
        return false;
    }
    
    //Method to backtrace from the winning configuration to the initial config
    public static void traceToInitialConfig(int winningConfig){
        ListIterator<ConfigNode> iter = configList.listIterator();
        ConfigNode currentNode = null, previousNode = null;
        
        //Find the winning node in the list
        while(iter.hasNext()){
            currentNode = iter.next();
            if(currentNode.config == winningConfig){
                moveStack.push(currentNode.move);
                break;
            }
        }
        
        //Trace back using the parent property
        while(iter.hasPrevious()){
            previousNode = iter.previous();
            if(previousNode.parentConfig == -1){
                break;
            }
            if(currentNode.parentConfig == previousNode.config){
                currentNode = previousNode;
                moveStack.push(currentNode.move);
            }
        }
    }
    
    //Turns the huff-coded int into an array to check for possible moves
    public static char[][] huffCodeToArray(int config){
        char[][] configMap = new char[maxRows+1][maxColumns+1];
        char piece;
        int pieceCounter = 0;
        int totalBitsChecked = 0;
        int row = 0;
        int col = 0;
        
        while(pieceCounter < totalPieces){
            //breakup huff code
            piece = getNextPieceFromCode(config,totalBitsChecked);
            
            //set the chars into array
            if(piece == 'A'){
                totalBitsChecked += 2;
                configMap[row][col] = 'A';
                configMap[row][col+1] = 'A';
            }
            else if(piece == 'B'){
                totalBitsChecked += 3;
                configMap[row][col] = 'B';
                configMap[row+1][col] = 'B';
            }
            else if(piece == 'C'){
                totalBitsChecked += 3;
                configMap[row][col] = 'C';
                configMap[row][col+1] = 'C';
                configMap[row+1][col] = 'C';
                configMap[row+1][col+1] = 'C';
            }
            else if(piece == 'D'){
                totalBitsChecked += 2;
                configMap[row][col] = 'D';
            }
            else if(piece == 'E'){
                totalBitsChecked += 2;
                configMap[row][col] = 'E';
            }
            
            //Find top-left-most empty spot in the configMap
            outerloop:
            for(int i=0; i<=maxRows; i++){
                for(int j=0; j<=maxColumns; j++){
                    //check for the unicode null character
                    if(configMap[i][j] == '\u0000'){
                        row = i;
                        col = j;
                        break outerloop;
                    }
                }
            }
            
            pieceCounter++;
        }
        return configMap;
    }
    
    //Similar to huffCodeToArray, except it returns a 2D array that is 
    //consistant with the piece-lettering conventions seen on the problem
    //handout (added K and L designations for the empty spaces)
    public static char[][] huffCodeToArrayForDiff(int config){
        char[][] configMap = new char[maxRows+1][maxColumns+1];
        char piece;
        int pieceCounter = 0;
        int totalBitsChecked = 0;
        int row = 0;
        int col = 0;
        int aPieceCounter = 0;
        int dPieceCounter = 0;
        int ePieceCounter = 0;
        
        while(pieceCounter < totalPieces){
            //breakup huff code
            piece = getNextPieceFromCode(config,totalBitsChecked);
            
            //set the chars into array
            if(piece == 'A'){
                totalBitsChecked += 2;
                if(aPieceCounter == 0){
                    configMap[row][col] = 'A';
                    configMap[row][col+1] = 'A';
                }
                else if(aPieceCounter == 1){
                    configMap[row][col] = 'B';
                    configMap[row][col+1] = 'B';
                }
                else if(aPieceCounter == 2){
                    configMap[row][col] = 'C';
                    configMap[row][col+1] = 'C';
                }
                else{
                    configMap[row][col] = 'D';
                    configMap[row][col+1] = 'D';
                }
                aPieceCounter++;
            }
            else if(piece == 'B'){
                totalBitsChecked += 3;
                configMap[row][col] = 'E';
                configMap[row+1][col] = 'E';
            }
            else if(piece == 'C'){
                totalBitsChecked += 3;
                configMap[row][col] = 'J';
                configMap[row][col+1] = 'J';
                configMap[row+1][col] = 'J';
                configMap[row+1][col+1] = 'J';
            }
            else if(piece == 'D'){
                totalBitsChecked += 2;
                if(dPieceCounter == 0){
                    configMap[row][col] = 'F';
                }
                else if(dPieceCounter == 1){
                    configMap[row][col] = 'G';
                }
                else if(dPieceCounter == 2){
                    configMap[row][col] = 'H';
                }
                else{
                    configMap[row][col] = 'I';
                }
                dPieceCounter++;
            }
            else if(piece == 'E'){
                totalBitsChecked += 2;
                if(ePieceCounter == 0){
                    configMap[row][col] = 'K';
                }
                else{
                    configMap[row][col] = 'L';
                }
                ePieceCounter++;
            }
            
            //Find top-left-most empty spot in the configMap
            outerloop:
            for(int i=0; i<=maxRows; i++){
                for(int j=0; j<=maxColumns; j++){
                    //check for the unicode null character
                    if(configMap[i][j] == '\u0000'){
                        row = i;
                        col = j;
                        break outerloop;
                    }
                }
            }
            
            pieceCounter++;
        }
        return configMap;
    }
    
    //Returns an int designation for a board configuration from a 2D array
    //representation of the board
    public static int arrayToHuffCode(char[][] configMap){
        int config = 0;
        int pieceCounter = 0;
        int row = 0;
        int col = 0;
        int totalBitsSet = 0;
        
        while(pieceCounter < totalPieces){
            
            //Find the top-left-most non-empty space
            outerloop:
            for(int i=0; i<=maxRows; i++){
                for(int j=0; j<=maxColumns; j++){
                    if(configMap[i][j] != 'X'){
                        row = i;
                        col = j;
                        break outerloop;
                    }
                }
            }
            
            //Determine the piece-type at that space and set the config bits
            //Also mark the spaces that the piece takes up with X's
            //"A" piece, set 10 into the configuration int
            if(configMap[row][col] == 'A'){
                config |= (1 << (mostSigBit-totalBitsSet));
                totalBitsSet += 2;
                configMap[row][col] = 'X';
                configMap[row][col+1] = 'X';
            }
            //"B" piece, set 010 into the configuration int
            else if(configMap[row][col] == 'B'){
                config |= (1 << (mostSigBit-totalBitsSet-1));
                totalBitsSet += 3;
                configMap[row][col] = 'X';
                configMap[row+1][col] = 'X';
            }
            //"C" piece, set 011 into the configuration int
            else if(configMap[row][col] == 'C'){
                config |= (1 << (mostSigBit-totalBitsSet-1));
                config |= (1 << (mostSigBit-totalBitsSet-2));
                totalBitsSet += 3;
                configMap[row][col] = 'X';
                configMap[row+1][col] = 'X';
                configMap[row][col+1] = 'X';
                configMap[row+1][col+1] = 'X';
            }
            //"D" piece, set 11 into the configuration int
            else if(configMap[row][col] == 'D'){
                config |= (1 << (mostSigBit-totalBitsSet));
                config |= (1 << (mostSigBit-totalBitsSet-1));
                totalBitsSet += 2;
                configMap[row][col] = 'X';
            }
            //"E" piece, set 0 into the configuration int (already set)
            else if(configMap[row][col] == 'E'){
                totalBitsSet +=2;
                configMap[row][col] = 'X';
            }
            pieceCounter++;
        }
        return config;
    }
    
    //Returns the next piece from a huff-coded int given an offset from the MSB
    public static char getNextPieceFromCode(int config, int offset){
        
        int startingBit = mostSigBit-offset;
        
        //The first bit is a 1
        if((config & (1 << startingBit)) != 0){
            //The second bit is a 1
            if((config & (1 << startingBit-1)) != 0){
                //huff code for a "D" piece is 11
                return 'D';
            }
            //The second bit is a 0
            else{
                //Huff code for an "A" piece is 10
                return 'A';
            }
        }
        
        //The first bit is a 0
        else{
            //The second bit is a 1
            if((config & (1 << startingBit-1)) != 0){
                //The third bit is a 1
                if((config & (1 << startingBit-2)) != 0){
                    //Huff code for a "C" piece is 011
                    return 'C';
                }
                
                //The third bit is a 0
                else{
                    //Huff code for a "B" piece is 010
                    return 'B';
                }
            }
            
            //The second bit is a 0
            else{
                //Huff code for an "E" piece (empty piece) is 00
                return 'E';
            }
        }
    }
    
    //Method to see if the current configuration has been seen before
    public static boolean isConfigInList(int currentConfig){
        ListIterator<ConfigNode> iter = configList.listIterator();
        while(iter.hasNext()){
            if(currentConfig == iter.next().config)
                return true;
        }
        return false;
    }
    
    //Method to use when a new valid move is found. Places the new ConfigNode
    //into the configuration list and the move queue in a safe manner
    public static void handleNewValidMove(ConfigNode newMove){
        
        //Add new configuration to the configuration list and the
        //move queue
        if(!configList.add(newMove)){
            System.out.println("Error adding to configList...");
            System.out.println("Exiting...");
            System.exit(0);
        }
        if(!moveQueue.add(newMove)){
            System.out.println("Error adding to moveQueue...");
            System.out.println("Exiting...");
            System.exit(0);
        }
    }
    
    //Method for testing purposes
    public static void printConfig(int config){
        char[][] configArray = new char[maxRows+1][maxColumns+1];
        configArray = huffCodeToArray(config);
        
        for(int i=0; i<=maxRows; i++){
            for(int j=0; j<=maxColumns; j++){
                System.out.print(configArray[i][j]);
            }
            System.out.print('\n');
        }
    }
    
    //Method to print all the moves necessary to get to the winning board
    public static void printStack(){
        System.out.println("***From Starting Configuration***");
        while(!moveStack.isEmpty()){
            System.out.println(moveStack.pop());
        }
        System.out.println("***Arrive at Winning Configuration***");
    }
    
    public static void main(String[] args) {
        startSolver();
    }
    
}

class ConfigNode{
    int config;
    int parentConfig;
    String move;
    
    public ConfigNode(int c, int p){
        this.config = c;
        this.parentConfig = p;
    }
}
