import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.LinkedList;

import javax.swing.*;


/**
 * The class <b>GameController</b> is the controller of the game. It implements 
 * the interface ActionListener to be called back when the player makes a move. It computes
 * the next step of the game, and then updates model and view.
 *
 * @author Guy-Vincent Jourdan, University of Ottawa
 */


public class GameController implements ActionListener {

    private LinkedStack<GameModel> redoStack;
    private LinkedStack<GameModel> undoStack;

    /**
     * Reference to the view of the game
     */
    private GameView gameView;

    /**
     * Reference to the model of the game
     */
    private GameModel gameModel;
 
    
    /**
     * Constructor used for initializing the controller. It creates the game's view 
     * and the game's model instances
     * 
     * @param size
     *            the size of the board on which the game will be played
     */
    public GameController(int size) {
        gameModel = createOrLoadModel(size);
        gameView = new GameView(gameModel, this);
        redoStack = new LinkedStack<GameModel>();
        undoStack = new LinkedStack<GameModel>();
        gameView.update();
    }


 
    /**
     * resets the game
     */
    public void reset(){
        redoStack = new LinkedStack<>();
        undoStack = new LinkedStack<>();
        gameModel.reset();

        gameView.disableUndoButton();
        gameView.disableRedoButton();
        gameView.update();
    }

    /**
     * Callback used when the user clicks a button or one of the dots. 
     * Implements the logic of the game
     *
     * @param e
     *            the ActionEvent
     */

    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() instanceof DotButton) {
            DotButton clicked = (DotButton)(e.getSource());

        	if (gameModel.getCurrentStatus(clicked.getColumn(),clicked.getRow()) ==
                    GameModel.AVAILABLE){
                pushToUndoStack(gameModel);
                gameModel.select(clicked.getColumn(),clicked.getRow());
                oneStep();
            }
        } else if (e.getSource() instanceof JButton) {
            JButton clicked = (JButton)(e.getSource());

            if (clicked.getText().equals("Quit")) {
                if (gameModel.getCurrentDot().getX() != -1) {
                    writeObject();
                }
                 System.exit(0);
            }
            else if (clicked.getText().equals("Reset")){
                reset();
            }
            else if (clicked.getText().equals("Undo")) {
                undo();
            }
            else if (clicked.getText().equals("Redo")) {
                redo();
            }
        } 
    }

    /**
     * Computes the next step of the game. If the player has lost, it 
     * shows a dialog offering to replay.
     * If the user has won, it shows a dialog showing the number of 
     * steps that had been required in order to win. 
     * Else, it finds one of the shortest path for the blue dot to 
     * exit the board and moves it one step in that direction.
     */
    private void oneStep(){
        Point currentDot = gameModel.getCurrentDot();

        // Destroys old redoStack on new move.
        redoStack = new LinkedStack<>();
        gameView.disableRedoButton();

        if(isOnBorder(currentDot)) {
            gameModel.setCurrentDot(-1,-1);
            gameView.update();
 
            Object[] options = {"Play Again",
                    "Quit"};
            int n = JOptionPane.showOptionDialog(gameView,
                    "You lost! Would you like to play again?",
                    "Lost",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if(n == 0){
                reset();
            }
            else{
                System.exit(0);
            }
        }
        else {
            Point direction = findDirection();
            if(direction.getX() == -1){
                gameView.update();
                Object[] options = {"Play Again",
                        "Quit"};
                int n = JOptionPane.showOptionDialog(gameView,
                        "Congratulations, you won in " + gameModel.getNumberOfSteps()
                            +" steps!\n Would you like to play again?",
                        "Won",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if(n == 0) {
                    reset();
                }
                else {
                    System.exit(0);
                }
            }
            else {

                gameModel.setCurrentDot(direction.getX(), direction.getY());
                gameView.update();
            }
        }
    }

    /**
     * Does a ``breadth-first'' search from the current location of the blue dot to find
     * one of the shortest available path to exit the board. 
     *
     * @return the location (as a Point) of the next step for the blue dot toward the exit.
     * If the blue dot is encircled and cannot exit, returns an instance of the class Point 
     * at location (-1,-1)
     */

    private Point findDirection(){
        boolean[][] blocked = new boolean[gameModel.getSize()][gameModel.getSize()];

        for(int i = 0; i < gameModel.getSize(); i ++){
            for (int j = 0; j < gameModel.getSize(); j ++){
                blocked[i][j] = 
                    !(gameModel.getCurrentStatus(i,j) == GameModel.AVAILABLE);
            }
        }

        Queue<Pair<Point>> myQueue = new LinkedQueue<Pair<Point>>();
        
        LinkedList<Point> possibleNeighbours = new LinkedList<Point>();

        // start with neighbours of the current dot
        // (note: we know the current dot isn't on the border)
        Point currentDot = gameModel.getCurrentDot();

        possibleNeighbours = findPossibleNeighbours(currentDot, blocked);

        // adding some non determinism into the game !
        java.util.Collections.shuffle(possibleNeighbours);

        for(int i = 0; i < possibleNeighbours.size() ; i++){
            Point p = possibleNeighbours.get(i);
            if(isOnBorder(p)){
                return p;                
            }
            myQueue.enqueue(new Pair<Point>(p,p));
            blocked[p.getX()][p.getY()] = true;
        }


        // start the search
        while(!myQueue.isEmpty()){
            Pair<Point> pointPair = myQueue.dequeue();
            possibleNeighbours = findPossibleNeighbours(pointPair.getFirst(), blocked);
             
            for(int i = 0; i < possibleNeighbours.size() ; i++){
                Point p = possibleNeighbours.get(i);
                if(isOnBorder(p)){
                    return pointPair.getSecond();                
                }
                myQueue.enqueue(new Pair<Point>(p,pointPair.getSecond()));
                blocked[p.getX()][p.getY()]=true;
            }

       }

        // could not find a way out. Return an outside direction
        return new Point(-1,-1);

    }

   /**
     * Helper method: checks if a point is on the border of the board
     *
     * @param p
     *            the point to check
     *
     * @return true iff p is on the border of the board
     */
     
    private boolean isOnBorder(Point p){
        return (p.getX() == 0 || p.getX() == gameModel.getSize() - 1 ||
                p.getY() == 0 || p.getY() == gameModel.getSize() - 1 );
    }

   /**
     * Helper method: find the list of direct neighbours of a point that are not
     * currenbtly blocked
     *
     * @param point
     *            the point to check
     * @param blocked
     *            a 2 dimentionnal array of booleans specifying the points that 
     *              are currently blocked
     *
     * @return an instance of a LinkedList class, holding a list of instances of 
     *      the class Points representing the neighbours of parameter point that 
     *      are not currently blocked.
     */
    private LinkedList<Point> findPossibleNeighbours(Point point, 
            boolean[][] blocked){

        LinkedList<Point> list = new LinkedList<Point>();
        int delta = (point.getY() %2 == 0) ? 1 : 0;
        if(!blocked[point.getX()-delta][point.getY()-1]){
            list.add(new Point(point.getX()-delta, point.getY()-1));
        }
        if(!blocked[point.getX()-delta+1][point.getY()-1]){
            list.add(new Point(point.getX()-delta+1, point.getY()-1));
        }
        if(!blocked[point.getX()-1][point.getY()]){
            list.add(new Point(point.getX()-1, point.getY()));
        }
        if(!blocked[point.getX()+1][point.getY()]){
            list.add(new Point(point.getX()+1, point.getY()));
        }
        if(!blocked[point.getX()-delta][point.getY()+1]){
            list.add(new Point(point.getX()-delta, point.getY()+1));
        }
        if(!blocked[point.getX()-delta+1][point.getY()+1]){
            list.add(new Point(point.getX()-delta+1, point.getY()+1));
        }
        return list;
    }

    /**
     * Push the current state of the Game Model onto the undo stack
     * @param model The Game Model
     */
    private void pushToUndoStack(GameModel model) {

        try {
            GameModel lastModel = (GameModel) model.clone();
            undoStack.push(lastModel);
            gameView.enableUndoButton();
        }
        catch (CloneNotSupportedException e) {
            JOptionPane.showOptionDialog(gameView,
                    "Error cloning game state in undo method.",
                    "Stack Error",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    null,null);
        }
        catch (NullPointerException e) {
            JOptionPane.showOptionDialog(gameView,
                    "Error cloning game state in undo method.",
                    "Stack Error",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE,
                    null,
                    null,null);
        }
    }

    /**
     * Revert the Game Model back one previous state. the undo function will also push the state being undone onto
     * the redo stack
     */
    public void undo() {
        try {
            redoStack.push(gameModel);
            gameModel = undoStack.pop();
            gameView.setModel(gameModel);
            if (undoStack.isEmpty()) {
                gameView.disableUndoButton();
            }
            gameView.enableRedoButton();
            gameView.update();
        } catch (EmptyStackException e) {
            displayError("No moves to undo.");
            gameView.disableUndoButton();

        }
        catch (NullPointerException e) {
            displayError("No moves to undo.");
            gameView.disableUndoButton();
        }
    }

    /**
     * Revert the Game Model to the previously undone state. Redo will also push the current Game Model onto the
     * undo stack
     */
    public void redo() {
        try {
            undoStack.push(gameModel);
            gameView.enableUndoButton();
            gameModel = redoStack.pop();

            gameView.setModel(gameModel);
            if (redoStack.isEmpty()) {
                gameView.disableRedoButton();
            }
            gameView.update();
        }
        catch (NullPointerException e) {
            displayError("Cannot push null elements to stack");
            gameView.disableRedoButton();
        }
        catch (EmptyStackException e) {
            displayError("Error. Stack is empty");
            gameView.disableRedoButton();
        }
    }

    /**
     * Checks to see if 'savedgame.ser' exists in the program directory and loads it. If the file does not exist,
     * a new game is created
     * @param size the size of the game to be created (ignored if loading from save file)
     * @return
     */
    private GameModel createOrLoadModel(int size) {
        File saveFile = new File("./savedGame.ser");
        if (saveFile.exists()) {
            gameModel = readObj(saveFile);
            saveFile.delete();
            return gameModel;
        }
        else {
            return new GameModel(size);
        }
    }

    /**
     * Read saveFile and return the GameModel object it represents
     * @param saveFile the name of the file to be read
     * @return the saved GameModel object
     */
    private GameModel readObj(File saveFile) {
        try {
            FileInputStream fileIn = new FileInputStream(saveFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            GameModel model = (GameModel) in.readObject();

            fileIn.close();
            in.close();
            return model;
        }
        catch (ClassNotFoundException e) {
            displayError("Error reading file. Data may be corrupted.");
        }
        catch (IOException e) {
            displayError("Error reading file. Please try again.");
        }
        return null;
    }

    /**
     * Write the current Game Model to 'savedgame.ser'
     */
    public void writeObject() {
        try {
            FileOutputStream fileOut = new FileOutputStream("./savedgame.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(gameModel);
            out.close();
            fileOut.close();
        }
        catch (IOException e) {
            displayError("Error saving to file.");
        }
    }


    /**
     * Utility method for displaying errors and exceptions. Creates a dialog with a custom error message
     * @param message the error message to be displayed
     */
    private void displayError(String message) {
        JOptionPane.showOptionDialog(gameView,
                message,
                "Error",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                null,null);
    }
}
