import java.awt.Color;

/** Written by Ryan D'souza
  * Brown University CS 015 Final Project 
  * Represents a Pacman object
  * ie. ghosts or Pacman */

public abstract class PacmanItem {
  protected int x, y;
  protected Direction facingDirection;
  protected int startX, startY;
  protected Color theColor;
  
  /** Constructor */
  public PacmanItem(final int x, final int y, final Color theColor){
    this.x = x;
    this.y = y;
    this.theColor = theColor;
    
    this.startX = x;
    this.startY = y;
    
    facingDirection = Direction.UP;
  }
  
  /** Updates the direction and either the X or Y coordinate of the object
    * depending on the direction it is moving in 
    @param direction to move in */
  public void move(Direction theD){
    switch(theD) {
      case UP:
        this.y--;
        facingDirection = Direction.UP;
        break;
        
      case DOWN:
        this.y++;
        facingDirection = Direction.DOWN;
        break;
        
      case LEFT:
        this.x--;
        facingDirection = Direction.LEFT;
        break;
        
      case RIGHT:
        this.x++;
        facingDirection = Direction.RIGHT;
        break;
        
      default:
        break;
    }
  }
  
  /** @return colorOfItem */  
  public Color getColor() { return this.theColor; }
  
  /** @param colorOfitem */
  public void setColor(Color tC) { this.theColor = tC; }
  
  /** @return startingXPosition */
  public int getStartX() { return this.startX; }
  
  /** @return startingYPosition */
  public int getStartY() { return this.startY; }

  /** Returns the item to initial position by
    * setting X and Y coordinates to the ones first given in the constructor */
  public void returnToStartPosition() {
    this.x = this.startX;
    this.y = this.startY;
    this.facingDirection = Direction.UP;
  }
  
  /** @return direction the item is facing */
  public Direction getFacingDirection() {
    return facingDirection;
  }
  
  /** @param directionToFace */
  public void setFacingDirection(Direction facing) {
    this.facingDirection = facing;
  }
  
  /** Four possible directions to move in */
  public enum Direction {
    UP, DOWN, LEFT, RIGHT; 
  }
  
  /** @return item x coordinate */
  public int getX() {
    return this.x;
  }
  
  /** @return item y coordinate */
  public int getY() {
    return this.y;
  }
  
  /** @param item's new x coordinate */
  public void setX(int x) {
    this.x = x;
  }
  
  /** @param item's new y coordinate */
  public void setY(int y) {
    this.y = y;
  }
}