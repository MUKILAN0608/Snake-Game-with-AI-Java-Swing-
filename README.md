# 🐍 Snake Game with AI (Java Swing)

A classic Snake game built in Java using Swing—featuring an optional A* pathfinding AI that takes over gameplay and visualizes its decision-making!

## 🎮 Features
- Classic snake gameplay
- **AI-Controlled Mode** (toggle with `A` key)
- **A* Pathfinding Visualization** (toggle with `V` key)
- Pause/Resume Game (`SPACE` or `P` key)
- Dynamic Score & High Score Tracking
- Clean, grid-based rendering with optional path highlights

## 🖥️ Demo


https://github.com/user-attachments/assets/09b67bfe-6ede-45ef-8a7d-475077d63616




## 📦 Requirements
- Java Development Kit (JDK) 8 or higher
- Any Java IDE (like IntelliJ IDEA, Eclipse, or NetBeans)

## 🚀 How to Run
Clone this repository:
```bash
git clone https://github.com/MUKILAN0608/snake-ai-java.git
```
```bash
cd snake-ai-java
```

# Controls

Arrow Keys / WASD → Move the snake (in manual mode)

A → Toggle AI control

V → Toggle AI path visualization

SPACE or P → Pause/Resume

ENTER → Restart after game over

# 📖 Code Overview
SnakeGame.java → Main game logic, rendering, controls, and AI integration

Uses A* Pathfinding to find the shortest path to the food avoiding obstacles

AI recalculates path dynamically after food is eaten or if path is obstructed

# 🤖 AI Logic (A* Pathfinding)
Heuristic: Manhattan Distance

Obstacles: Snake body parts

Supports optional visual representation of AI’s chosen path (in light blue)

# 📜 License
This project is open-source under the MIT License.

# 📌 Future Improvements
Sound effects

Customizable grid size and speed

Multiple difficulty levels

Leaderboard persistence

# 👨‍💻 Author
A.M. Mukilan
MUKILAN0608
