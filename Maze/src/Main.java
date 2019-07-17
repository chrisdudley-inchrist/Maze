import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {

    public static final int GREEN = new Color(0, 255, 0).getRGB();
    public static final int RED = new Color(255, 0, 0).getRGB();
    public static final int BLUE = new Color(0, 0, 255).getRGB();
    public static final int YELLOW = new Color(255, 255, 0).getRGB();
    public static final int PATH_COLOR = 0;
    //public static final int PATH_COLOR = -1;
    //public static final int PATH_COLOR_TRANSPARENT = 0;

    public static boolean endReached = false;
    private static BufferedImage mazeImgRevised = null;
    private static Stack<List<int[]>> mStackPaths = new Stack<>();
    private static List<List<int[]>> mPathsTruncated = new LinkedList<>();
    private static List<List<int[]>> mPathSolution = new LinkedList<>();
    public static int[] startCoord = new int[2];

    //TODO: remove hardcoded endCoord by finding the path that has a space on the outer wall
    public static int[] endCoord = new int[2];

    public static List<List<int[]>> getPathsTruncated() {
        return mPathsTruncated;
    }

    public static Stack<List<int[]>> getStackPaths() {
        return mStackPaths;
    }

    public static List<List<int[]>> getPathSolution() {
        return mPathSolution;
    }

    public static void setPathSolution(List<List<int[]>> pathSolution) {
        Main.mPathSolution = pathSolution;
    }

    public static void setStackPaths(Stack<List<int[]>> stackPaths) {
        mStackPaths = stackPaths;
    }

    public static void setPathsTruncated(List<List<int[]>> pathsTruncated) {
        Main.mPathsTruncated = pathsTruncated;
    }

    public static void setMazeImgRevised(BufferedImage bi) {
        mazeImgRevised = bi;
    }

    public static BufferedImage getMazeImgRevised() {
        return mazeImgRevised;
    }

    public static void main(String[] args) {


        BufferedImage mazeImg = null;

        File f1 = new File("/home/christopher/Documents/Maze/src/mazefromweb.png");
        try {
            mazeImg = ImageIO.read(f1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f2 = new File("/home/christopher/Documents/Maze/src/mazefromwebRevised.png");
        try {
            setMazeImgRevised(ImageIO.read(f2));
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<List<int[]>> paths = followPath(mazeImg);

        // TODO:
        //  check to see which paths contain path 0
        //  if multiple values
        //  add left, right, and/or down values to stack
        //  explore other paths
        //  return back to path 0 and
        Stack<List<int[]>> stackPathsTemp = new Stack<>();

        for (List<int[]> path : paths) {
            getPathsTruncated().add(path);
        }
        boolean startPathFound = false;
        //find the start path
        for (List<int[]> path : paths) {
            if (startPathFound == true) {
                break;
            }
            for (int i = 0; i < path.size(); i++) {
                if (Arrays.equals(path.get(i), startCoord)) {
                    getPathSolution().add(path);
                    getStackPaths().push(path);
                    stackPathsTemp.push(path);
                    colorMaze(path, BLUE);
                    getPathsTruncated().remove(path);
                    startPathFound = true;
                    break;
                }
            }
        }

        List<int[]> pathToIterate;
        boolean pathFound = false;
        int pathCounter = 0;
        Stack<List<int[]>> stackPathsToTruncate = new Stack<>();
        while (!endReached) {
            pathToIterate = stackPathsTemp.peek();
            colorMaze(pathToIterate, YELLOW);
            //check each path and compare with the stackPath's top.
            for (List<int[]> path : getPathsTruncated()) {
                //if paths = 3, there are no more outcomes.
                // Break out and move to the stackPath's top.
                if (pathCounter > 2) {
                    break;
                }
                //check the first and last coordinates of path and pathToIterate
                for (int i = 0; i < path.size(); i += path.size() - 1) {
                    for (int j = 0; j < pathToIterate.size(); j += pathToIterate.size() - 1) {
                        if (Arrays.equals(path.get(i), pathToIterate.get(j))) {
                            pathFound = true;
                            //if pathCounter is 0, add to collection.
//                            if (pathCounter <= 0) {
//                                getPathSolution().add(path);
//                                colorMaze(path, BLUE);
//                            } else {
//                                colorMaze(path, RED);
//                            }
                            colorMaze(path, RED);
                            stackPathsTemp.push(path);
                            //add path to removal stack.
                            stackPathsToTruncate.add(path);
                            pathCounter++;
                            break;
                        }
                    }
                    if (pathFound == true) {
                        pathFound = false;
                        break;
                    }
                }
            }
            if (pathCounter < 1) {
                colorMaze(stackPathsTemp.pop(), RED);

            }
            //remove path from pathsTruncated
            while (!stackPathsToTruncate.isEmpty()) {
                getPathsTruncated().remove(stackPathsToTruncate.pop());

            }
            //once all paths have been checked to find the connecting paths,
            //pop the stack.
            pathCounter = 0;
        }
    }

    private static void colorMaze(List<int[]> path, int COLOR) {
        int[] storeMe;
        for (int i = 0; i < path.size(); i++) {
            storeMe = path.get(i);
            //check to see if end point has been reached.
            getMazeImgRevised().setRGB(storeMe[0], storeMe[1], COLOR);
            if (Arrays.equals(storeMe, endCoord)) {
                System.out.println("The end has been reached!");
                endReached = true;
            }
        }
    }

    private static List<List<int[]>> followPath(BufferedImage mazeImg) {
        List<List<int[]>> paths = new LinkedList<>();
        boolean foundStart = false;
        for (int y = 0; y < mazeImg.getHeight(); y++) {
            for (int x = 0; x < mazeImg.getWidth(); x++) {
                //if the current node is "white"
                if (mazeImg.getRGB(x, y) == PATH_COLOR) {
                    if (!foundStart) {
                        startCoord[0] = x;
                        startCoord[1] = y;
                        foundStart = true;
                        generatePaths(mazeImg, x, y, paths, new LinkedList<>(), "down");
                    } else {
                        //if current node has bottom node or right node, execute find path.
                        // bottom path first, then right path.
                        if (!isTravelled(paths, x, y)) {
                            try {
                                if (mazeImg.getRGB(x + 1, y) == PATH_COLOR) {
                                    generatePaths(mazeImg, x, y, paths, new LinkedList<>(), "right");
                                }
                                if (mazeImg.getRGB(x, y + 1) == PATH_COLOR) {
                                    generatePaths(mazeImg, x, y, paths, new LinkedList<>(), "down");
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return paths;
    }

    private static boolean isTravelled(List<List<int[]>> paths, int x, int y) {
        int[] arr = new int[]{x, y};
        for (List<int[]> path : paths) {
            //TODO: check only the middle segment, not the first or last node.
            for (int i = 1; i < path.size() - 1; i++) {
                if (Arrays.equals(path.get(i), arr)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void generatePaths(BufferedImage mazeImg, int x, int y, List<List<int[]>> paths, List<int[]>
            path, String direction) {

        //TODO: change color of map where currentPath has been added.
        path.add(new int[]{x, y});
        colorMaze(path, GREEN);
        try {
            if (mazeImg.getRGB(x, y) == PATH_COLOR) {
                //if bottom node is white space
                if (direction == "down") {
                    if (y == mazeImg.getWidth() - 1 && mazeImg.getRGB(x, y) == PATH_COLOR) {
                        System.out.println("has out path");
                        endCoord[0] = x;
                        endCoord[1] = y;
                        paths.add(path);
                    } else if (mazeImg.getRGB(x, y + 1) == PATH_COLOR) {
                        //if bottom node has left path, end traversal and add
                        //the end coordinate.
                        if (mazeImg.getRGB(x - 1, y + 1) == PATH_COLOR) {
                            System.out.println("has left path");
                            //save the paths end coordinate to the current path.
                            path.add(new int[]{x, y + 1});
                            colorMaze(path, GREEN);
                            //add the current path with its start and end coordinate
                            //to the collection of paths.
                            paths.add(path);

                        } else if (mazeImg.getRGB(x + 1, y + 1) == PATH_COLOR) {
                            System.out.println("has right path");
                            path.add(new int[]{x, y + 1});
                            colorMaze(path, GREEN);
                            paths.add(path);
                        } else {
                            generatePaths(mazeImg, x, y + 1, paths, path, direction);
                        }
                    }
                } else if (direction == "right") {
                    if (mazeImg.getRGB(x + 1, y) == PATH_COLOR) {
                        //if right node has downward path, end traversal and add
                        //the end coordinate.
                        if (mazeImg.getRGB(x + 1, y + 1) == PATH_COLOR) {
                            System.out.println("has downward path");
                            path.add(new int[]{x + 1, y});
                            paths.add(path);
                            //if right node has upward path, end traversal and add
                            //the end coordinate.
                        } else if (mazeImg.getRGB(x + 1, y - 1) == PATH_COLOR) {
                            System.out.println("has upward path");
                            path.add(new int[]{x + 1, y});
                            paths.add(path);
                        } else {
                            generatePaths(mazeImg, x + 1, y, paths, path, direction);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

