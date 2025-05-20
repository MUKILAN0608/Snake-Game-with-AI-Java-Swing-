import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

/**
 * Snake Game with AI Implementation
 * Features:
 * - Classic snake gameplay
 * - A* pathfinding AI
 * - AI assist mode
 * - AI visualization
 */
public class SnakeGame extends JFrame {
    // Game panel dimensions
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int UNIT_SIZE = 20;
    private static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int BOARD_WIDTH = WIDTH / UNIT_SIZE;
    private static final int BOARD_HEIGHT = HEIGHT / UNIT_SIZE;

    // Game constants
    private static final int DELAY = 100;

    // Game state
    private boolean running = false;
    private boolean paused = false;
    private javax.swing.Timer timer;
    private int score = 0;
    private int highScore = 0;

    // Direction states
    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;

    // Snake and food data
    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int foodX;
    private int foodY;

    // AI components
    private boolean aiControlled = false;
    private boolean showPathfinding = false;
    private List<Point> aiPath = new ArrayList<>();

    // Random generator
    private final Random random = new Random();

    // Game panel
    private GamePanel gamePanel;
    private enum Direction { UP, DOWN, LEFT, RIGHT }

    /**
     * Main constructor for the Snake Game
     */
    public SnakeGame() {
        setTitle("Snake Game with AI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setLocationRelativeTo(null);

        // Create and add game panel
        gamePanel = new GamePanel();
        add(gamePanel);

        // Set up key listener
        gamePanel.addKeyListener(new GameKeyAdapter());
        gamePanel.setFocusable(true);

        // Start new game
        startGame();

        setVisible(true);
    }

    /**
     * Main game panel for rendering
     */
    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw(g);
        }
    }

    /**
     * Key adapter for game controls
     */
    class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            // Toggle AI control with 'A' key
            if (key == KeyEvent.VK_A) {
                aiControlled = !aiControlled;
                return;
            }

            // Toggle path visualization with 'V' key
            if (key == KeyEvent.VK_V) {
                showPathfinding = !showPathfinding;
                return;
            }

            // Pause game with space or P
            if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_P) {
                paused = !paused;
                return;
            }

            // Manual movement controls (only when AI is not controlling)
            if (!aiControlled) {
                if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && !rightDirection) {
                    leftDirection = true;
                    upDirection = false;
                    downDirection = false;
                }
                if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && !leftDirection) {
                    rightDirection = true;
                    upDirection = false;
                    downDirection = false;
                }
                if ((key == KeyEvent.VK_UP || key == KeyEvent.VK_W) && !downDirection) {
                    upDirection = true;
                    leftDirection = false;
                    rightDirection = false;
                }
                if ((key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) && !upDirection) {
                    downDirection = true;
                    leftDirection = false;
                    rightDirection = false;
                }
            }

            // Start new game if game over
            if (key == KeyEvent.VK_ENTER && !running) {
                startGame();
            }
        }
    }

    /**
     * Start a new game
     */
    private void startGame() {
        // Initialize snake position
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }

        // Reset directions
        leftDirection = false;
        rightDirection = true;
        upDirection = false;
        downDirection = false;

        // Reset score
        score = 0;

        // Generate first food
        generateFood();

        // Start running
        running = true;

        // Start game timer
        timer = new javax.swing.Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (running && !paused) {
                    if (aiControlled) {
                        moveWithAI();
                    }
                    move();
                    checkCollision();
                    checkFood();
                }
                gamePanel.repaint();
            }
        });
        timer.start();
    }

    /**
     * Draw the game elements
     */
    private void draw(Graphics g) {
        if (running) {
            // Draw grid (optional)
            g.setColor(new Color(20, 20, 20));
            for (int i = 0; i < HEIGHT / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
            }

            // Draw AI path if enabled
            if (showPathfinding && aiControlled && !aiPath.isEmpty()) {
                g.setColor(new Color(100, 200, 255, 100));
                for (Point p : aiPath) {
                    g.fillRect(p.x * UNIT_SIZE, p.y * UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Draw food
            g.setColor(Color.RED);
            g.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

            // Draw snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    // Snake head
                    g.setColor(new Color(0, 200, 0));
                } else {
                    // Snake body
                    g.setColor(new Color(0, 150, 0));
                }
                g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            // Draw score
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 25);
            g.drawString("High Score: " + highScore, 10, 50);

            // Show AI status
            if (aiControlled) {
                g.drawString("AI CONTROL ON (Press 'A' to toggle)", WIDTH - 300, 25);
            } else {
                g.drawString("MANUAL CONTROL (Press 'A' for AI)", WIDTH - 300, 25);
            }

            // Show visualization status
            if (showPathfinding) {
                g.drawString("PATH VISIBLE (Press 'V' to toggle)", WIDTH - 300, 50);
            }

            // Show paused status
            if (paused) {
                g.setColor(new Color(200, 200, 200, 150));
                g.fillRect(0, 0, WIDTH, HEIGHT);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("PAUSED", WIDTH / 2 - 80, HEIGHT / 2);
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.drawString("Press SPACE to continue", WIDTH / 2 - 120, HEIGHT / 2 + 40);
            }
        } else {
            // Game Over screen
            gameOver(g);
        }
    }

    /**
     * Move the snake
     */
    private void move() {
        // Move body parts
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move head based on direction
        if (leftDirection) {
            x[0] = x[0] - UNIT_SIZE;
        }
        if (rightDirection) {
            x[0] = x[0] + UNIT_SIZE;
        }
        if (upDirection) {
            y[0] = y[0] - UNIT_SIZE;
        }
        if (downDirection) {
            y[0] = y[0] + UNIT_SIZE;
        }
    }

    /**
     * Generate new food at random location
     */
    private void generateFood() {
        boolean validPosition = false;

        while (!validPosition) {
            // Generate random position
            foodX = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            foodY = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

            // Check if food is not on snake
            validPosition = true;
            for (int i = 0; i < bodyParts; i++) {
                if (foodX == x[i] && foodY == y[i]) {
                    validPosition = false;
                    break;
                }
            }
        }
    }

    /**
     * Check if snake has eaten food
     */
    private void checkFood() {
        if (x[0] == foodX && y[0] == foodY) {
            bodyParts++;
            score++;
            if (score > highScore) {
                highScore = score;
            }
            generateFood();
            // Recalculate AI path after food is eaten
            if (aiControlled) {
                calculateAIPath();
            }
        }
    }

    /**
     * Check collision with walls or self
     */
    private void checkCollision() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        // Check if head touches left border
        if (x[0] < 0) {
            running = false;
        }

        // Check if head touches right border
        if (x[0] >= WIDTH) {
            running = false;
        }

        // Check if head touches top border
        if (y[0] < 0) {
            running = false;
        }

        // Check if head touches bottom border
        if (y[0] >= HEIGHT) {
            running = false;
        }

        // Stop timer if game over
        if (!running) {
            timer.stop();
        }
    }

    /**
     * Game over screen
     */
    private void gameOver(Graphics g) {
        // Game Over text
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 75));
        FontMetrics metrics = g.getFontMetrics();
        g.drawString("Game Over", (WIDTH - metrics.stringWidth("Game Over")) / 2, HEIGHT / 2 - 50);

        // Score display
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        metrics = g.getFontMetrics();
        g.drawString("Score: " + score, (WIDTH - metrics.stringWidth("Score: " + score)) / 2, HEIGHT / 2 + 20);

        // Restart instructions
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Press ENTER to restart", (WIDTH - metrics.stringWidth("Press ENTER to restart")) / 2, HEIGHT / 2 + 70);
    }

    /**
     * Move the snake using AI pathfinding
     */
    private void moveWithAI() {
        // Calculate path if none exists or food has moved
        if (aiPath.isEmpty()) {
            calculateAIPath();
        }

        // If we have a path, follow it
        if (!aiPath.isEmpty()) {
            // Get next position from path
            Point nextPosition = aiPath.remove(0);

            // Convert to grid coordinates
            int gridHeadX = x[0] / UNIT_SIZE;
            int gridHeadY = y[0] / UNIT_SIZE;

            // Determine direction to move
            if (nextPosition.x > gridHeadX) {
                // Move right
                leftDirection = false;
                rightDirection = true;
                upDirection = false;
                downDirection = false;
            } else if (nextPosition.x < gridHeadX) {
                // Move left
                leftDirection = true;
                rightDirection = false;
                upDirection = false;
                downDirection = false;
            } else if (nextPosition.y < gridHeadY) {
                // Move up
                leftDirection = false;
                rightDirection = false;
                upDirection = true;
                downDirection = false;
            } else if (nextPosition.y > gridHeadY) {
                // Move down
                leftDirection = false;
                rightDirection = false;
                upDirection = false;
                downDirection = true;
            }
        } else {
            // No valid path found, try to avoid immediate death
            avoidObstacles();
        }
    }

    /**
     * Calculate path to food using A* algorithm
     */
    private void calculateAIPath() {
        // Clear previous path
        aiPath.clear();

        // Current position in grid coordinates
        int startX = x[0] / UNIT_SIZE;
        int startY = y[0] / UNIT_SIZE;

        // Food position in grid coordinates
        int targetX = foodX / UNIT_SIZE;
        int targetY = foodY / UNIT_SIZE;

        // Use A* to find path
        aiPath = findPath(startX, startY, targetX, targetY);
    }

    /**
     * A* pathfinding algorithm to find path to food
     */
    private List<Point> findPath(int startX, int startY, int targetX, int targetY) {
        // Create open and closed lists
        PriorityQueue<Node> openList = new PriorityQueue<>();
        Set<String> closedList = new HashSet<>();

        // Create start node
        Node startNode = new Node(startX, startY, null);
        startNode.g = 0;
        startNode.h = calculateHeuristic(startX, startY, targetX, targetY);
        startNode.f = startNode.g + startNode.h;

        // Add start node to open list
        openList.add(startNode);

        // Loop until we find path or exhaust all possibilities
        while (!openList.isEmpty()) {
            // Get node with lowest f value
            Node current = openList.poll();

            // Add to closed list
            closedList.add(current.x + "," + current.y);

            // If we reached target, build and return path
            if (current.x == targetX && current.y == targetY) {
                return buildPath(current);
            }

            // Check all four directions
            int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // Up, Right, Down, Left

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];

                // Skip if out of bounds
                if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
                    continue;
                }

                // Skip if in closed list
                if (closedList.contains(newX + "," + newY)) {
                    continue;
                }

                // Skip if collides with snake body
                boolean collides = false;
                for (int i = 0; i < bodyParts; i++) {
                    if (x[i] / UNIT_SIZE == newX && y[i] / UNIT_SIZE == newY) {
                        // Allow tail position since it will move
                        if (i == bodyParts - 1 && i > 0) {
                            continue;
                        }
                        collides = true;
                        break;
                    }
                }
                if (collides) {
                    continue;
                }

                // Create new node
                Node neighbor = new Node(newX, newY, current);
                neighbor.g = current.g + 1;
                neighbor.h = calculateHeuristic(newX, newY, targetX, targetY);
                neighbor.f = neighbor.g + neighbor.h;

                // Check if this is a better path than any existing one
                boolean inOpenList = false;
                for (Node openNode : openList) {
                    if (openNode.x == newX && openNode.y == newY) {
                        inOpenList = true;
                        if (neighbor.g < openNode.g) {
                            openList.remove(openNode);
                            openList.add(neighbor);
                        }
                        break;
                    }
                }

                // Add to open list if not already there
                if (!inOpenList) {
                    openList.add(neighbor);
                }
            }
        }

        // No path found, return empty list
        return new ArrayList<>();
    }

    /**
     * Calculate Manhattan distance heuristic
     */
    private int calculateHeuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    /**
     * Build path from goal node back to start
     */
    private List<Point> buildPath(Node node) {
        List<Point> path = new ArrayList<>();

        // Build path from target back to start (excluding start position)
        while (node.parent != null) {
            path.add(0, new Point(node.x, node.y));
            node = node.parent;
        }

        return path;
    }

    /**
     * Emergency strategy to avoid immediate death when no path is found
     */
    private void avoidObstacles() {
        // Get head position
        int headX = x[0] / UNIT_SIZE;
        int headY = y[0] / UNIT_SIZE;

        // Try to find safe direction
        boolean[] safeDirections = new boolean[4]; // Up, Right, Down, Left
        Arrays.fill(safeDirections, true);

        // Check for walls
        if (headY - 1 < 0) safeDirections[0] = false;  // Up
        if (headX + 1 >= BOARD_WIDTH) safeDirections[1] = false;  // Right
        if (headY + 1 >= BOARD_HEIGHT) safeDirections[2] = false;  // Down
        if (headX - 1 < 0) safeDirections[3] = false;  // Left

        // Check for snake body
        for (int i = 0; i < bodyParts; i++) {
            int partX = x[i] / UNIT_SIZE;
            int partY = y[i] / UNIT_SIZE;

            // Up
            if (headX == partX && headY - 1 == partY) {
                safeDirections[0] = false;
            }
            // Right
            if (headX + 1 == partX && headY == partY) {
                safeDirections[1] = false;
            }
            // Down
            if (headX == partX && headY + 1 == partY) {
                safeDirections[2] = false;
            }
            // Left
            if (headX - 1 == partX && headY == partY) {
                safeDirections[3] = false;
            }
        }

        // Don't go in opposite direction of current movement
        if (upDirection) safeDirections[2] = false;  // Don't go down
        if (rightDirection) safeDirections[3] = false;  // Don't go left
        if (downDirection) safeDirections[0] = false;  // Don't go up
        if (leftDirection) safeDirections[1] = false;  // Don't go right

        // Choose random safe direction
        List<Integer> safeIndices = new ArrayList<>();
        for (int i = 0; i < safeDirections.length; i++) {
            if (safeDirections[i]) {
                safeIndices.add(i);
            }
        }

        if (!safeIndices.isEmpty()) {
            int directionIndex = safeIndices.get(random.nextInt(safeIndices.size()));

            // Set direction
            leftDirection = false;
            rightDirection = false;
            upDirection = false;
            downDirection = false;

            switch (directionIndex) {
                case 0: upDirection = true; break;      // Up
                case 1: rightDirection = true; break;   // Right
                case 2: downDirection = true; break;    // Down
                case 3: leftDirection = true; break;    // Left
            }
        }
        // If no safe direction, continue in current direction
    }

    /**
     * Node class for A* pathfinding
     */
    private class Node implements Comparable<Node> {
        int x, y;
        int g; // Cost from start to this node
        int h; // Heuristic (estimated cost from this node to goal)
        int f; // Total cost (g + h)
        Node parent;

        public Node(int x, int y, Node parent) {
            this.x = x;
            this.y = y;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGame());
    }
}