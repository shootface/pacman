import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Written by Ryan D'souza Brown University CS 015 Final Project Main Class of Pacman game
 */

public class Pacman extends JPanel {
  
  private Mode gameMode;
  private long modeStart;
  
  private final byte board[][] = getBoard();
  private final TheGhost[] theGhosts = new TheGhost[4];
  
  private static final String SPACE = "     ";
  
  private static final byte SCALE = 20;
  private static final byte PACMAN_SIZE = 15;
  private static final byte GHOST_SIZE = 20;
  private static final byte DOT_SIZE = 5;
  private static final byte ENERGIZER_SIZE = DOT_SIZE * 2;
  
  private static final int SPEED_CHASE = 20; //Seconds
  private static final int SPEED_SCATTER = 7;
  private static final int SPEED_FRIGHTENED = 10; 
  private static final byte GHOST_RELEASE = 5;
  
  private static final byte FRIGHTENED = 100; // SHOULD BE 7 SECONDS
  private static final byte CHASE = 20; // 20 Seconds
  private static final byte SCATTER = 7; // 7 Seconds
  
  public static final byte WALL = 0;
  public static final byte FREE = 1;
  public static final byte DOT = 2;
  public static final byte ENERGIZER = 3;
  public static final byte PACMAN = 4;
  public static final byte GHOST = 5;
  public static final byte OUT = 6;
  
  private final Queue<TheGhost> ghostPenQ = new LinkedList<TheGhost>();
  
  private TheGhost redGhost, pinkGhost, blueGhost, orangeGhost;
  private ThePacman pacman;
  
  private Graphics2D theG;
  
  private Point ghostReleasePoint;
  private Point ghostSpawnPoint;
  
  private long pacmanScore = 0;
  private byte pacmanLives = 3;
  
  private JLabel pacmanScoreLabel;
  private JLabel pacmanLivesLabel;
  private JLabel ghostModeLabel;
  private JLabel nextGhostReleaseLabel;
  
  private boolean controlTouch = false;
  private boolean isChaseMode;
  
  private long ghostModeStart;
  private long hitEnergizerAt;
  private long ghostReleasedAt;
  
  /** Constructor, initializes JPanel and board */
  public Pacman() {
    super();
    setSize(new Dimension(400, 400));
    setMinimumSize(new Dimension(400, 400));
    setFocusable(true);
    requestFocusInWindow();
    
    pacmanScoreLabel = new JLabel("Score: " + pacmanScore, JLabel.RIGHT);
    pacmanScoreLabel.setForeground(Color.white);
    add(pacmanScoreLabel);
    
    pacmanLivesLabel = new JLabel(SPACE + "Lives: " + pacmanLives, JLabel.LEFT);
    pacmanLivesLabel.setForeground(Color.WHITE);
    add(pacmanLivesLabel);
    
    ghostModeLabel = new JLabel(SPACE + "Normal", JLabel.LEFT);
    ghostModeLabel.setForeground(Color.WHITE);
    add(ghostModeLabel);
    
    nextGhostReleaseLabel = new JLabel(SPACE + "Ghost Release", JLabel.LEFT);
    nextGhostReleaseLabel.setForeground(Color.WHITE);
    add(nextGhostReleaseLabel);
    
    gameMode = Mode.CHASE;
    modeStart = System.currentTimeMillis();
    
    
    initializeVariables();
    addKeyListener(new ControlListener());
    start();
  }
  
  /** Start the other threads */
  public void start() { 
    new Thread(new GameLogic()).start();
    new javax.swing.Timer(0, theListener).start();
  }
  
  /** Initalizes pacman and ghosts start locations */
  public void initializeVariables() {
    Point ghostStart = null;
    
    for (byte i = 0; i < board.length; i++) {
      for (byte y = 0; y < board[i].length; y++) {
        // Pacman starting location
        if (board[i][y] == PACMAN)
          pacman = new ThePacman(y, i, Color.YELLOW);
        
        // Ghost starting location
        else if (board[i][y] == GHOST)
          ghostStart = new Point(y, i);
      }
    }
    
    final byte x = (byte) ghostStart.getX();
    final byte y = (byte) ghostStart.getY();
    
    // Left Inside
    redGhost = new TheGhost(Color.RED, x - 2, y, board);
    board[redGhost.getY()][redGhost.getX()] = GHOST;
    theGhosts[0] = redGhost;
    ghostPenQ.add(redGhost);
    
    // Middle inside
    blueGhost = new TheGhost(Color.CYAN, x, y, board);
    board[blueGhost.getY()][blueGhost.getX()] = GHOST;
    theGhosts[1] = blueGhost;
    ghostPenQ.add(blueGhost);
    ghostSpawnPoint = new Point(blueGhost.getX(), blueGhost.getY());
    
    // Right inside
    orangeGhost = new TheGhost(Color.ORANGE, x + 2, y, board);
    board[orangeGhost.getY()][orangeGhost.getX()] = GHOST;
    theGhosts[2] = orangeGhost;
    ghostPenQ.add(orangeGhost);
    
    // Outside
    pinkGhost = new TheGhost(Color.PINK, x, y - 2, board);
    // pinkGhost.setY(y - 2);
    board[pinkGhost.getY()][pinkGhost.getX()] = GHOST;
    theGhosts[3] = pinkGhost;
    ghostReleasePoint = new Point(pinkGhost.getX(), pinkGhost.getY());
    ghostReleasedAt = System.currentTimeMillis();
    pinkGhost.release();
    
    // for(int i = 0; i < theGhosts.length; i++)
    // System.out.println(theGhosts[i]);
    
    isChaseMode = true;
    ghostModeStart = System.currentTimeMillis();
  }
  
  /**
   * Returns a byte representing the item that the parameter's item will hit based on the parameter item's direction
   */
  private byte getItemInNextMove(final PacmanItem movingItem, final PacmanItem.Direction theDirection) {
    try {
      switch (theDirection) {
        case UP:
          return board[movingItem.getY() - 1][movingItem.getX()];
          
        case DOWN:
          return board[movingItem.getY() + 1][movingItem.getX()];
          
        case LEFT:
          return board[movingItem.getY()][movingItem.getX() - 1];
          
        case RIGHT:
          return board[movingItem.getY()][movingItem.getX() + 1];
          
        default:
          return Byte.MAX_VALUE;
      }
    } catch (Exception e) {
      return OUT;
    }
  }
  
  /** Return byte of item in that Point */
  public byte getItemAtPoint(final Point thePoint) {
    return board[(byte) thePoint.getY()][(byte) thePoint.getX()];
  }
  
  /** Returns an array of points in 1 step in any direction that the ghost can move */
  public Point[] getValidNeighbors(final TheGhost theGhost) {
    ArrayList<Point> thePoints = new ArrayList<Point>();
    
    if (getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.UP)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.UP));
    if (getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.DOWN)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.DOWN));
    if (getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.LEFT)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.LEFT));
    if (getItemAtPoint(theGhost.getProspectivePoint(PacmanItem.Direction.RIGHT)) != WALL)
      thePoints.add(theGhost.getProspectivePoint(PacmanItem.Direction.RIGHT));
    return thePoints.toArray(new Point[thePoints.size()]);
  }
  
  /** Returns an array of points that the Point can move (anything but a wall) */
  public Point[] getValidNeighbors(final Point thePoint) {
    return getValidNeighbors(new TheGhost(null, (byte) thePoint.getX(), (byte) thePoint.getY(), board));
  }
  
  /** Returns the direction to get from theGhost to the Point */
  public PacmanItem.Direction getDirection(final TheGhost theGhost, final Point thePoint) {
    return getDirections(theGhost, new Point[] { thePoint })[0];
  }
  
  /** Returns an array of directions for getting theGhost to the Points */
  public PacmanItem.Direction[] getDirections(final TheGhost theGhost, final Point[] thePoints) {
    PacmanItem.Direction[] theDirections = new PacmanItem.Direction[thePoints.length];
    
    for (byte i = 0; i < thePoints.length; i++) {
      if ((byte) thePoints[i].getX() == theGhost.getX()) {
        // If y is greater, lower down
        if ((byte) thePoints[i].getY() > theGhost.getY()) {
          theDirections[i] = PacmanItem.Direction.DOWN;
        }
        // If y is less, up
        else if ((byte) thePoints[i].getY() < theGhost.getY()) {
          theDirections[i] = PacmanItem.Direction.UP;
        }
      }
      else if ((byte) thePoints[i].getY() == theGhost.getY()) {
        // If x is greater, further out
        if ((byte) thePoints[i].getX() > theGhost.getX()) {
          theDirections[i] = PacmanItem.Direction.RIGHT;
        }
        // If x is less, further in
        if ((byte) thePoints[i].getX() < theGhost.getX()) {
          theDirections[i] = PacmanItem.Direction.LEFT;
        }
      }
    }
    return theDirections;
  }
  
  /** Moves the item parameter based on the direction parameter */
  public void moveItem(final PacmanItem theItem, final PacmanItem.Direction theDirection) {
    controlTouch = false;
    
    if (theDirection == null) {
      return;
    }
    theItem.setFacingDirection(theDirection);
    final byte itemInNextDirection = getItemInNextMove(pacman, theDirection);
    
    if (itemInNextDirection == OUT) {
      return;
    }
    
    if (itemInNextDirection == GHOST) {
      if (isFrightened()) {
        eatGhost(theDirection);
      }
      else {
        hitGhost();
      }
      return;
    }
    
    if (itemInNextDirection == DOT)
      pacmanScore += 10;
    
    if (itemInNextDirection != WALL) {
      board[pacman.getY()][pacman.getX()] = FREE;
      pacman.move(theDirection);
    }
    
    if (itemInNextDirection == ENERGIZER) {
      hitEnergizerAt = System.currentTimeMillis();
      pacmanScore += 100;
    }
    
    board[pacman.getY()][pacman.getX()] = PACMAN;
    
    updateLabels();
  }
  
  /** Eats the Ghost if it is not frightened mode */
  private void eatGhost() { 
    if(!isFrightened()) { 
      hitGhost();
      return;
    }
    final Point pacmanOnGhostPoint = pacman.getPoint();
    for (TheGhost theGhost : theGhosts) {
      if (theGhost.getPoint().equals(pacmanOnGhostPoint)) {
        System.out.println("EATEN:\t" + theGhost.toString());
        pacmanScore += 200;
        theGhost.returnToStartPosition();
        updateBoard(theGhost.getPoint(), FREE);
        updateBoard(theGhost.getPoint(), GHOST);
        ghostRespawn(theGhost);
      }
    }    
  }
  
  /** If Pacman eats a ghost on frightened mode */
  private void eatGhost(final PacmanItem.Direction theDirection) {
    final Point pacmanOriginalPoint = pacman.getPoint();
    pacman.move(theDirection);
    final Point pacmanOnGhostPoint = pacman.getPoint();
    
    for (byte i = 0; i < theGhosts.length; i++) {
      if (theGhosts[i].getPoint().equals(pacmanOnGhostPoint)) {
        pacmanScore += 200;
        ghostRespawn(theGhosts[i]);
        theGhosts[i].returnToStartPosition();
      }
    }
    
    // Make Pacman's old location free
    updateBoard(pacmanOriginalPoint, FREE);
    
    // Set Pacman's new location
    updateBoard(pacmanOnGhostPoint, PACMAN);
  }
  
  /** Paint method, called by repaint() */
  public void paintComponent(Graphics g) {
    theG = (Graphics2D) g;
    releaseGhosts();
    updateLabels();
    drawSquares();
  }
  
  /** Thread that has most of the game logic
    * Handles updated ghosts' board and BFA
    * and movements */
  private class GameLogic implements Runnable { 
    @Override
    public void run() { 
      while(true)  {
        eatGhost();
        hitGhost();
        moveItem(pacman, pacman.getFacingDirection());
        for(TheGhost theGhost : theGhosts) { 
          if(theGhost.isReleased()) { 
            theGhost.updateBoard(board);
            updateBoard(theGhost.getPoint(), FREE);
            theGhost.startBreadthFirstAlgorithm(theGhost.getPoint());
            updateBoard(theGhost.getPoint(), GHOST);
          }
        }
        eatGhost();
        hitGhost();
        try { 
          Thread.sleep(100);
        }
        catch(Exception e) { 
          e.printStackTrace();
        }
        repaint();
      }
    }
  };
  
  /**
   * If Pacman hits a ghost and it's not on frightened mode Move pacman back to initial position, decrement lives
   */
  public void hitGhost() {
    if(isFrightened()) { 
      return;
    }
    final Point pacmanOnGhostPoint = pacman.getPoint();
    for (TheGhost theGhost : theGhosts) {
      if (theGhost.getPoint().equals(pacmanOnGhostPoint)) {
        updateBoard(pacman.getPoint(), FREE);
        pacman.returnToStartPosition();
        updateBoard(pacman.getPoint(), FREE);
        
        pacmanLives--;
        updateLabels();
        return;
      }
    }   
  }
  
  /** Draws the entire board, including ghosts and pacman */
  public void drawSquares() {
    for (byte i = 0; i < board.length; i++) {
      for (byte y = 0; y < board[i].length; y++) {
        switch (board[i][y]) {
          case WALL:
            theG.setColor(Color.BLUE);
            theG.fillRect(y * SCALE, i * SCALE, SCALE, SCALE);
            break;
            
          case FREE:
            drawBlackSquare(i, y);
            break;
            
          case DOT:
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, DOT_SIZE, DOT_SIZE);
            break;
            
          case ENERGIZER:
            drawBlackSquare(i, y);
            theG.setColor(Color.WHITE);
            theG.fillOval(y * SCALE + 5, i * SCALE + 7, ENERGIZER_SIZE, ENERGIZER_SIZE);
            break;
            
          case PACMAN:
            drawBlackSquare(i, y);
            theG.setColor(Color.YELLOW);
            theG.fillOval(y * SCALE, i * SCALE, PACMAN_SIZE, PACMAN_SIZE);
            break;
            
          case GHOST:
            break;
            
          default:
            drawBlackSquare(i, y);
            break;
        }
      }
    }
    for (byte i = 0; i < theGhosts.length; i++) {
      drawGhost(theGhosts[i]);
    }
  }
  
  /** Translate arrow key presses to pacman movements */
  private ActionListener theListener = new ActionListener() {
    public void actionPerformed(final ActionEvent event) {
      final String arrowDirection = (String) event.getActionCommand();
      
      if (arrowDirection == null)
        return;

      PacmanItem.Direction movingDirection;
      board[pacman.getY()][pacman.getX()] = FREE;
      
      if (arrowDirection.equals("RIGHT"))
        movingDirection = PacmanItem.Direction.RIGHT;
      else if (arrowDirection.equals("LEFT"))
        movingDirection = PacmanItem.Direction.LEFT;
      else if (arrowDirection.equals("UP"))
        movingDirection = PacmanItem.Direction.UP;
      else if (arrowDirection.equals("DOWN"))
        movingDirection = PacmanItem.Direction.DOWN;
      else
        movingDirection = null;
      
      moveItem(pacman, movingDirection);
    }
  };
  
  /**
   * Listens to keyboard events, sets the facing direction based on those events Then moves the item in regards to the
   * facing direction
   */
  private class ControlListener implements KeyListener {
    public void keyPressed(KeyEvent e) {
      
      controlTouch = true;
      
      // Direction item will move in
      PacmanItem.Direction movingDirection;
      
      // Current location becomes nothing for Pacman
      board[pacman.getY()][pacman.getX()] = FREE;
      
      switch (e.getKeyCode()) {
        // LEFT
        case KeyEvent.VK_LEFT:
          movingDirection = PacmanItem.Direction.LEFT;
          break;
          
          // RIGHT
        case KeyEvent.VK_RIGHT:
          movingDirection = PacmanItem.Direction.RIGHT;
          break;
          
          // UP
        case KeyEvent.VK_UP:
          movingDirection = PacmanItem.Direction.UP;
          break;
          
          // DOWN
        case KeyEvent.VK_DOWN:
          movingDirection = PacmanItem.Direction.DOWN;
          break;
          
        default:
          movingDirection = null;
          break;
      }
      
      moveItem(pacman, movingDirection);
    }
    
    public void keyReleased(KeyEvent e) {
    }
    
    public void keyTyped(KeyEvent e) {
    }
  }
  
  /** Main method, creates frame and adds game to it */
  public static void main(String[] ryan) {
    JFrame theFrame = new JFrame("Pacman");
    theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    theFrame.setSize(500, 500);
    
    theFrame.add(new Pacman());
    theFrame.setVisible(true);
  }
  
  /**
   * If it is time, removes next ghost from pen and places ghost at initial ghostReleasePoint
   */
  private void releaseGhosts() {
    if (ghostPenQ.size() != 0) {
      if ((System.currentTimeMillis() - ghostReleasedAt) / 1000 == GHOST_RELEASE)
        ghostLeavePen(ghostPenQ.remove());
    }
  }
  
  /**
   * Removes ghost from its position on the board, updates ghosts coordinates to that of initial point, updates board
   * to that value
   */
  private void ghostLeavePen(final TheGhost theGhost) {
    board[theGhost.getY()][theGhost.getX()] = FREE;
    theGhost.setX((byte) ghostReleasePoint.getX());
    theGhost.setY((byte) ghostReleasePoint.getY());
    board[theGhost.getY()][theGhost.getX()] = GHOST;
    ghostReleasedAt = System.currentTimeMillis();
    theGhost.release();
  }
  
  /** Moves Ghost back to pen */
  public void ghostRespawn(final TheGhost theEaten) {
    if(ghostPenQ.size() == 0) { 
      theEaten.setPoint(ghostSpawnPoint);
    }
    else { 
      theEaten.returnToStartPosition();
    }
    ghostPenQ.add(theEaten);
    updateBoard(theEaten.getPoint(), GHOST);
    ghostReleasedAt = System.currentTimeMillis();
    theEaten.setInPen();
  }
  
  /** Update board location with that Pacman type */
  public void updateBoard(final Point thePoint, final byte theItem) {
    board[(byte) thePoint.getY()][(byte) thePoint.getX()] = theItem;
  }
  
  /** @return true if chase mode */
  public boolean isChaseMode() {
    
    // If it's chaseMode right now
    if (isChaseMode) {
      // if it's still chase mode
      isChaseMode = (((System.currentTimeMillis() - ghostModeStart) / 1000) <= CHASE);
      
      // If ChaseMode is over now, start other mode
      if (!isChaseMode)
        ghostModeStart = System.currentTimeMillis();
      
      return isChaseMode;
    }
    
    // If it's not chaseMode right now
    else if (!isChaseMode) {
      // If it's still not chase mode
      isChaseMode = (((System.currentTimeMillis() - ghostModeStart) / 1000) >= SCATTER);
      
      if (isChaseMode)
        ghostModeStart = System.currentTimeMillis();
      
      return isChaseMode;
    }
    return isChaseMode;
  }
  
  /** Returns true if frightened */
  private boolean isFrightened() { 
    return ((System.currentTimeMillis() - hitEnergizerAt) / 1000) < FRIGHTENED;
  }
  
  /** Draws the ghost in the parameter */
  private void drawGhost(TheGhost theGhost) {
    theG.setColor(theGhost.getColor());
    theG.fillRect(theGhost.getX() * SCALE, theGhost.getY() * SCALE, GHOST_SIZE, GHOST_SIZE);
  }
  
  /** Draws a black square at X and Y */
  private void drawBlackSquare(int x, int y) {
    theG.setColor(Color.BLACK);
    theG.fillRect(y * SCALE, x * SCALE, SCALE, SCALE);
  }
  
  /** Returns the board as a 2D array */
  public static byte[][] getBoard() {
    final int[][] theMap = cs015.fnl.PacmanSupport.SupportMap.getMap();
    final byte[][] theMapByte = new byte[theMap.length][theMap[0].length];
    
    for(int i = 0; i < theMap.length; i++) { 
      for(int y = 0; y < theMap[i].length; y++) { 
        theMapByte[(byte)i][(byte)y] = (byte) theMap[i][y];
      }
    }
    return theMapByte;
  }
  
  /** Prints the board as a 2D array */
  public void printBoard() {
    for (byte y = 0; y < board.length; y++) {
      for (byte i = 0; i < board[y].length; i++) {
        System.out.print(board[y][i] + " ");
      }
      System.out.println();
    }
  }
  
  /** Updates the score, num lives, ghost pen release countdown, and ghost mode labels */
  private void updateLabels() {
    pacmanScoreLabel.setText("Score: " + pacmanScore);
    pacmanLivesLabel.setText(SPACE + "Lives: " + pacmanLives + "     ");
    
    if (ghostPenQ.size() >= 0) {
      int timeToRelease = (int) GHOST_RELEASE - (int) ((System.currentTimeMillis() - ghostReleasedAt) / 1000);
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: " + timeToRelease);
    } else if (ghostPenQ.size() < 0 || (GHOST_RELEASE - ((System.currentTimeMillis() - ghostReleasedAt) / 1000)) < 0) {
      nextGhostReleaseLabel.setText(SPACE + "Ghost Release: N/A");
    }
    
    int timeLeft = (int) ((System.currentTimeMillis() - ghostModeStart) / 1000);
    if (isFrightened())
      ghostModeLabel.setText(SPACE + "Frightened Mode: " + (FRIGHTENED - timeLeft));
    else if (isChaseMode())
      ghostModeLabel.setText(SPACE + "Chase Mode: " + (CHASE - timeLeft));
    else
      ghostModeLabel.setText(SPACE + "Scatter Mode: " + (SCATTER - timeLeft));
  }
}