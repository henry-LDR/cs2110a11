package ui;

import java.beans.PropertyChangeListener;
import util.MazeGenerator;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.JPanel;
import model.Actor;
import model.Actor.DPair;
import model.GameModel;
import model.Ghost;
import model.Ghost.GhostState;
import graph.MazeGraph.Direction;
import graph.MazeGraph.MazeEdge;
import model.PacMann;
import util.GameMap;
import ui.Tile.TileType;

public class GameBoard extends JPanel {

    /**
     * Tile size (width and height) used to compute the preferred display size of a game board
     * [pixels].
     */
    public static final int PREFERRED_TILE_SIZE = 20;

    /**
     * 2D array of Tile objects that comprise the game's background.
     */
    private Tile[][] tileGrid;

    /**
     * The model that this board is currently visualizing.
     */
    private GameModel model;

    /**
     * Whether to render actors' "guidance paths" for debugging purposes.
     */
    private final boolean showPaths;

    /**
     * Listener to register with model to respond to board state changes.
     */
    private final PropertyChangeListener boardListener;

    /**
     * Construct a new GameBoard panel.
     */
    public GameBoard(boolean showPaths) {
        setBackground(Color.BLACK);

        // Allow clicking to restore focus
        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        boardListener = e -> repaint();

        this.showPaths = showPaths;
    }

    /**
     * Reassigns this board to visualize the given `newModel`.
     */
    public void setModel(GameModel newModel) {
        if (model != null) {
            model.removePropertyChangeListener("board_state", boardListener);
        }

        this.model = newModel;
        if (model != null) {
            model.addPropertyChangeListener("board_state", boardListener);

            int width = model.width();
            int height = model.height();
            GameMap map = newModel.map();
            tileGrid = new Tile[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (map.types()[i][j] == MazeGenerator.TileType.WALL) {
                        tileGrid[i][j] = new Tile(getWallType(map.types(), i, j), i, j,
                                map.elevations()[i][j]);
                    } else {
                        tileGrid[i][j] = new Tile(new TileType(0, 0), i, j, map.elevations()[i][j]);
                    }
                }
            }
            setPreferredSize(new Dimension(PREFERRED_TILE_SIZE * tileGrid.length,
                    PREFERRED_TILE_SIZE * tileGrid[0].length));
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Dimension size = getSize();
        int gridWidth = tileGrid.length;
        int gridHeight = tileGrid[0].length;

        // Determine the largest fitting tile size, then shift coordinates so that board will be
        //  centered.  Scaling allows shapes to be defined using normalized coordinates.
        int tileDim = Math.min(size.width / gridWidth, size.height / gridHeight);
        int hOffset = size.width - tileDim * gridWidth;
        int vOffset = size.height - tileDim * gridHeight;
        g2.translate(hOffset / 2, vOffset / 2);
        g2.scale(tileDim, tileDim);

        g2.setStroke(new BasicStroke(3.0f / tileDim));
        // background tiles
        for (Tile[] row : tileGrid) {
            for (Tile tile : row) {
                tile.paint(g2);
            }
        }

        // TODO 1: Add code to visualize the items (dots and pellets) on the board. Each item
        //  should be drawn centered on the tile corresponding to the vertex that contains it.
        //  Look through the `GameModel` class to see how to access this information. Both pellets
        //  and dots are drawn as white circles. Dots have a diameter of 0.3 units and pellets have
        //  a diameter of 0.7 units, both in `g2`'s coordinates. You can use an `Ellipse2D.Double`
        //  to model these shapes. You could add this code all here but it will likely be more
        //  maintainable to extract it into a separate helper method.

        // actors
        for (Actor actor : model.actors()) {
            if (actor instanceof PacMann player) {
                paintPacMann(player, g2);

                g2.setColor(Color.YELLOW);
            } else if (actor instanceof Ghost ghost) {
                paintGhost(ghost, g2);

                g2.setColor(ghost.color());
            } else {
                throw new RuntimeException("Cannot paint unknown actor " + actor);
            }
            if (showPaths) {
                paintPath(g2, actor.guidancePath());
            }
        }
    }

    public void paintDots (Graphics2D g2){
        g2.setColor(Color.WHITE);

        for



    }
    /**
     * Draws Pac-Mann at his current location facing in his current direction on the game board.
     */
    public void paintPacMann(PacMann player, Graphics2D g2) {
        g2.setColor(Color.YELLOW);

        DPair bb = player.getBoundingBoxUL();

        int startAngle = switch (player.currentEdge().direction()) {
            case Direction.LEFT -> 210;
            case Direction.RIGHT -> 30;
            case Direction.UP -> 120;
            case Direction.DOWN -> 300;
        };

        g2.fill(new Arc2D.Double(bb.i(), bb.j(), 1.5, 1.5, startAngle, 300, Arc2D.PIE));
    }

    /**
     * Draws the given `ghost` at its current location in its current state on the game board.
     */
    public void paintGhost(Ghost ghost, Graphics2D g2) {

        // body
        g2.setColor(ghostColor(ghost));
        DPair bb = ghost.getBoundingBoxUL();
        double i = bb.i();
        double j = bb.j();
        g2.fill(new Arc2D.Double(i + 0.1, j, 1.3, 1.3, 0, 180, Arc2D.PIE));
        Path2D.Double ghostBottom = new Path2D.Double(Path2D.WIND_NON_ZERO);
        ghostBottom.moveTo(i + 0.1, j + 0.65);
        ghostBottom.lineTo(i + 1.4, j + 0.65);
        ghostBottom.lineTo(i + 1.4, j + 1.5);
        ghostBottom.lineTo(i + 1.15, j + 1.25);
        ghostBottom.lineTo(i + 0.95, j + 1.5);
        ghostBottom.lineTo(i + 0.75, j + 1.25);
        ghostBottom.lineTo(i + 0.55, j + 1.5);
        ghostBottom.lineTo(i + 0.35, j + 1.25);
        ghostBottom.lineTo(i + 0.1, j + 1.5);
        ghostBottom.closePath();
        g2.fill(ghostBottom);

        // eye whites
        g2.setColor(ghost.state() == GhostState.FLEE ? ghost.color() : Color.WHITE);
        double eyeX = i + switch (ghost.currentEdge().direction()) {
            case Direction.LEFT -> 0.2;
            case Direction.RIGHT -> 0.4;
            default -> 0.3;
        };
        double eyeY = j + switch (ghost.currentEdge().direction()) {
            case Direction.UP -> 0.2;
            case Direction.DOWN -> 0.4;
            default -> 0.3;
        };

        g2.fill(new Ellipse2D.Double(eyeX, eyeY, 0.4, 0.5));
        g2.fill(new Ellipse2D.Double(eyeX + 0.5, eyeY, 0.4, 0.5));

        // pupils
        g2.setColor(Color.BLUE);
        double pupilX = i + switch (ghost.currentEdge().direction()) {
            case Direction.LEFT -> 0.2;
            case Direction.RIGHT -> 0.55;
            default -> 0.375;
        };
        double pupilY = j + switch (ghost.currentEdge().direction()) {
            case Direction.UP -> 0.2;
            case Direction.DOWN -> 0.65;
            default -> 0.425;
        };

        g2.fill(new Ellipse2D.Double(pupilX, pupilY, 0.25, 0.25));
        g2.fill(new Ellipse2D.Double(pupilX + 0.5, pupilY, 0.25, 0.25));

        // scared mouth (during FLEE)
        if (ghost.state() == GhostState.FLEE) {
            g2.setColor(ghost.color());
            Path2D.Double mouth = new Path2D.Double();
            mouth.moveTo(i + 0.25, j + 1.075);
            mouth.lineTo(i + 0.35, j + 1.15);
            mouth.lineTo(i + 0.55, j + 1);
            mouth.lineTo(i + 0.75, j + 1.15);
            mouth.lineTo(i + 0.95, j + 1);
            mouth.lineTo(i + 1.15, j + 1.15);
            mouth.lineTo(i + 1.25, j + 1.075);
            g2.draw(mouth);
        }
    }

    /**
     * Returns the color that the given `ghost` should be drawn, based on its current state.
     */
    private Color ghostColor(Ghost ghost) {
        if (ghost.state() != GhostState.FLEE) {
            return ghost.color();
        }
        if (ghost.fleeTimeRemaining() > 2000) {
            return Color.BLUE;
        }
        int blinks = (int) ghost.fleeTimeRemaining() / 500;
        return (blinks % 2 == 0) ? Color.BLUE : Color.WHITE;
    }

    /**
     * Visualizes the given chase `path` on the game board.
     */
    public void paintPath(Graphics2D g2, List<MazeEdge> path) {
        if (path.isEmpty()) {
            return;
        }
        Path2D.Double shape = new Path2D.Double();
        var prev = path.getFirst().tail().loc();
        shape.moveTo(prev.i() + 0.5, prev.j() + 0.5);
        for (var e : path) {
            var pt = e.head().loc();
            if (Math.abs(pt.i() - prev.i()) + Math.abs(pt.j() - prev.j()) > 1) {
                shape.moveTo(pt.i() + 0.5, pt.j() + 0.5);
            } else {
                shape.lineTo(pt.i() + 0.5, pt.j() + 0.5);
            }
            prev = pt;
        }
        g2.draw(shape);
        g2.fill(new Ellipse2D.Double(prev.i() + 0.25, prev.j() + 0.25, 0.5, 0.5));
    }

    /**
     * Return the type of wall tile that should be drawn in a particular location. This is
     * determined based on whether the surrounding tiles are paths or walls.
     */
    private TileType getWallType(MazeGenerator.TileType[][] types, int i, int j) {
        // left tunnel edges
        if (i == 0 && j > 1 && j < model.height() - 2) {
            if (types[i][j - 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 3);
            } else if (types[i][j - 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 2);
            } else if (types[i][j + 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 0);
            } else if (types[i][j + 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 0);
            }
        }

        // right tunnel edges
        if (i == model.width() - 1 && j > 1 && j < model.height() - 2) {
            if (types[i][j - 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 2);
            } else if (types[i][j - 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 2);
            } else if (types[i][j + 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 0);
            } else if (types[i][j + 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 1);
            }
        }

        // top tunnel edges
        if (j == 0 && i > 1 && i < model.width() - 2) {
            if (types[i - 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 1);
            } else if (types[i - 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 1);
            } else if (types[i + 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 3);
            } else if (types[i + 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 0);
            }
        }

        // bottom tunnel edges
        if (j == model.height() - 1 && i > 1 && i < model.width() - 2) {
            if (types[i - 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 2);
            } else if (types[i - 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 1);
            } else if (types[i + 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 3);
            } else if (types[i + 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 3);
            }
        }

        int up = j > 0 && types[i][j - 1] == MazeGenerator.TileType.WALL ? 1 : 0;
        int down = j < model.height() - 1 && types[i][j + 1] == MazeGenerator.TileType.WALL ? 1 : 0;
        int left = i > 0 && types[i - 1][j] == MazeGenerator.TileType.WALL ? 1 : 0;
        int right = i < model.width() - 1 && types[i + 1][j] == MazeGenerator.TileType.WALL ? 1 : 0;
        int numWalls = up + down + left + right;

        if (numWalls == 2) { // wall cap
            return new TileType(2, down + right + 2 * down * left);
        } else if (numWalls == 3) { // wall side
            return new TileType(3, 2 * down + left - right);
        } else { // wall joint
            if (types[i + 1][j - 1] != MazeGenerator.TileType.WALL) { // northeast
                return new TileType(4, 1);
            } else if (types[i - 1][j - 1] != MazeGenerator.TileType.WALL) { // northwest
                return new TileType(4, 0);
            } else if (types[i + 1][j + 1] != MazeGenerator.TileType.WALL) { // southeast
                return new TileType(4, 2);
            } else { // southwest
                return new TileType(4, 3);
            }
        }
    }


}
