import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Main {

    public static final int GREEN = new Color(51, 255, 51).getRGB();
    public static final int BLUE = new Color(255, 0, 0).getRGB();
    private static BufferedImage mazeImgRevised = null;
    private static List<List<int[]>> mPathsTruncated = new LinkedList<>();
    public static int[] startCoord = new int[2];
    //TODO: remove hardcoded endCoord by finding the path that has a space on the outer wall
    public static int[] endCoord = new int[]{10, 14};

    public static List<List<int[]>> getPathsTruncated() {
        return mPathsTruncated;
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


//        Stack<List<int[]>> stackTest = new Stack<>();
//        List<List<int[]>> pathsTest = new LinkedList<>();
        List<int[]> pathTest = new LinkedList<>();
        int[] l1a = new int[]{8, 0};
        int[] l1b = new int[]{8, 1};
        int[] l1c = new int[]{8, 2};
        pathTest.add(l1a);
        pathTest.add(l1b);
        pathTest.add(l1c);

        BufferedImage mazeImg = null;

        File f1 = new File("/home/christopher/Documents/Maze/src/maze.png");
        try {
            mazeImg = ImageIO.read(f1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f2 = new File("/home/christopher/Documents/Maze/src/mazeRevised.png");
        try {
            setMazeImgRevised(ImageIO.read(f2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<List<int[]>> paths = generatePaths(mazeImg, getMazeImgRevised());

        // TODO:
        //  check to see which paths contain path 0
        //  if multiple values
        //  add left, right, and/or down values to stack
        //  explore other paths
        //  return back to path 0 and

        //start path added to Stack
        Stack<List<int[]>> pathStack = new Stack<>();

        for (List<int[]> l : paths) {
            getPathsTruncated().add(l);
        }
        int[] storeMe = new int[2];
        int xCoord, yCoord;
        boolean startPathFound = false;
        for (List<int[]> path : paths) {
            if (startPathFound == true) {
                break;
            }
            for (int i = 0; i < path.size(); i++) {
                if (Arrays.equals(path.get(i), startCoord)) {
                    pathStack.push(path);

                    //color maze
                    for (int j = 0; j < path.size(); j++) {
                        storeMe = path.get(j);
                        xCoord = storeMe[0];
                        yCoord = storeMe[1];
                        getMazeImgRevised().setRGB(xCoord, yCoord, BLUE);
                    }
                    getPathsTruncated().remove(path);
                    startPathFound = true;
                    break;
                }
            }
        }

        List<int[]> pathToIterate;
        boolean pathIsFound;
        while (!pathStack.isEmpty()) {
            pathIsFound = false;
            pathToIterate = pathStack.peek();
            for (List<int[]> path : getPathsTruncated()) {
                for (int i = 0; i < path.size(); i++) {
                    for (int j = 0; j < pathToIterate.size(); j++) {
                        if (Arrays.equals(path.get(i), pathToIterate.get(j))) {
                            pathIsFound = true;
                            pathStack.push(path);
                            //color maze
                            for (int k = 0; k < path.size(); k++) {
                                storeMe = path.get(k);
                                //check to see if end point has been reached.
                                if (storeMe == endCoord) {
                                    System.out.println("The end has been reached!");
                                }
                                xCoord = storeMe[0];
                                yCoord = storeMe[1];
                                getMazeImgRevised().setRGB(xCoord, yCoord, BLUE);
                            }
                            break;
                        }
                    }
                }
//                if (pathIsFound == true) {
//                    break;
//                }
            }
            //once all paths have been checked to find the connecting paths,
            //pop the stack.
            pathStack.pop();
        }
    }

    private static List<List<int[]>> generatePaths(BufferedImage mazeImg, BufferedImage mazeImgRevised) {
        List<List<int[]>> paths = new LinkedList<>();
        boolean foundStart = false;
        for (int y = 0; y < mazeImg.getHeight(); y++) {
            for (int x = 0; x < mazeImg.getWidth(); x++) {

                //if the current node is "white"
                if (mazeImg.getRGB(x, y) == -1) {
                    if (!foundStart) {
                        startCoord[0] = x;
                        startCoord[1] = y;
                        foundStart = true;
                        //int[] pathStartCoord = {x, y};
                        //save the paths start coordinate to the current path.
                        //currPath.add(pathStartCoord);
                        findPath(mazeImg, x, y, paths, new LinkedList<>(), "down");
                    } else {
                        //if current node has bottom node or right node, execute find path.
                        // bottom path first, then right path.
                        if (!isTravelled(paths, x, y)) {
                            try {
                                if (mazeImg.getRGB(x + 1, y) == -1) {
                                    //int[] pathStartCoord = {x, y};
                                    //save the paths start coordinate to the current path.
                                    //currPath.add(pathStartCoord);
                                    findPath(mazeImg, x, y, paths, new LinkedList<>(), "right");
                                }
                                if (mazeImg.getRGB(x, y + 1) == -1) {
                                    //int[] pathStartCoord = {x, y};
                                    //save the paths start coordinate to the current path.
                                    //currPath.add(pathStartCoord);
                                    findPath(mazeImg, x, y, paths, new LinkedList<>(), "down");
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

    private static void findPath(BufferedImage mazeImg, int x, int y, List<List<int[]>> paths, List<int[]> currPath, String direction) {

        //TODO: change color of map where currentPath has been added.
        currPath.add(new int[]{x, y});
        getMazeImgRevised().setRGB(x, y, GREEN);
        try {
            if (mazeImg.getRGB(x, y) == -1) {
                //if bottom node is white space
                if (direction == "down") {
                    if (mazeImg.getRGB(x, y + 1) == -1) {
                        //if bottom node has left path, end traversal and add
                        //the end coordinate.
                        if (mazeImg.getRGB(x - 1, y + 1) == -1) {
                            System.out.println("has left path");
                            //save the paths end coordinate to the current path.
                            currPath.add(new int[]{x, y + 1});
                            getMazeImgRevised().setRGB(x, y + 1, GREEN);
                            //add the current path with its start and end coordinate
                            //to the collection of paths.
                            paths.add(currPath);
                            //if bottom node has right path, end traversal and add
                            //the end coordinate.
                        } else if (mazeImg.getRGB(x + 1, y + 1) == -1) {
                            System.out.println("has right path");
                            //save the paths end coordinate to the current path.
                            currPath.add(new int[]{x, y + 1});
                            getMazeImgRevised().setRGB(x, y + 1, GREEN);
                            //add the current path with its start and end coordinate
                            //to the collection of paths.
                            paths.add(currPath);
                        } else {
                            findPath(mazeImg, x, y + 1, paths, currPath, direction);
                        }
                    }
                } else if (direction == "right") {
                    if (mazeImg.getRGB(x + 1, y) == -1) {
                        //if right node has downward path, end traversal and add
                        //the end coordinate.
                        if (mazeImg.getRGB(x + 1, y + 1) == -1) {
                            System.out.println("has bottom path");
                            currPath.add(new int[]{x + 1, y});
                            getMazeImgRevised().setRGB(x + 1, y, GREEN);
                            paths.add(currPath);
                        } else {
                            findPath(mazeImg, x + 1, y, paths, currPath, direction);
                        }
                    }

                }

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

