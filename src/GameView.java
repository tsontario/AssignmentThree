import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

/**
 * The class <b>GameView</b> provides the current view of the entire Game. It extends
 * <b>JFrame</b> and lays out an instance of  <b>BoardView</b> (the actual game) and 
 * two instances of JButton. The action listener for the buttons is the controller.
 *
 * @author Guy-Vincent Jourdan, University of Ottawa
 */

public class GameView extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Reference to the view of the board
     */
    private BoardView board;
    private GameModel gameModel;
 
  
    /**
     * Constructor used for initializing the Frame
     * 
     * @param model
     *            the model of the game (already initialized)
     * @param gameController
     *            the controller
     */

    public GameView(GameModel model, GameController gameController) {
        super("Circle the Dot");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	setBackground(Color.WHITE);

        gameModel = model;
    	board = new BoardView(model, gameController);
    	add(board, BorderLayout.CENTER);

 
        JButton buttonReset = new JButton("Reset");
        buttonReset.setFocusPainted(false);
        buttonReset.addActionListener(gameController);

        JButton buttonExit = new JButton("Quit");
        buttonExit.setFocusPainted(false);
        buttonExit.addActionListener(gameController);

        JButton buttonUndo = new JButton("Undo");
        buttonUndo.setFocusPainted(false);
        buttonUndo.addActionListener(gameController);

        JButton buttonRedo = new JButton("Redo");
        buttonRedo.setFocusPainted(false);
        buttonRedo.addActionListener(gameController);


        // TODO make layout nicer.
    	JPanel control = new JPanel();
    	control.setBackground(Color.WHITE);
        control.add(buttonReset);
        control.add(buttonExit);
        control.add(buttonUndo);
        control.add(buttonRedo);
    	add(control, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                saveOnClose();
            }
        });

    	pack();
    	setResizable(false);
    	setVisible(true);

    }


    public void update() {
        updateUndoRedoButtons();
        board.update();
  
    }

    public void setModel(GameModel model) {
        gameModel = model;
        board.setModel(gameModel);

    }

    public void updateUndoRedoButtons() {

    }

    private void saveOnClose() {
        gameModel.writeObject();
    }

}
