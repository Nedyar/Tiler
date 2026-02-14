import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Main {
    static Map<Tile, Integer> originalTiles;
    static Tile mountainRangeTile;
    static Tile straightRiverTile;
    static Tile curveRiverTile;
    static Tile mountainTile;
    static Tile mountainRiverStraightTile;
    static Tile mountainRiverCurveTile;
    static Tile emptyTile;

    private static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L; // 1 GB
    private static int logFileIndex;
    private static BufferedWriter boardWriter;
    private static int boardWriterPosition;
    private static String logBasePath;

    // Set para deduplicar por rotación
    private static Set<String> boardHashes;

    public static void main(String[] args) {
        initializeVariables();

        Set<Board> dummyBoards = new HashSet<>();
        // Board.createValidBoards(dummyBoards, 4, 4);
        Board.setUpAmonHen(dummyBoards);
        // Board.setUpAmonSun(dummyBoards);

        // TODO: Manage EOF in JSON methods
        try {
            boardWriter.write("]");
            boardWriter.flush();
            boardWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeVariables() {
        originalTiles = new HashMap<>();

        mountainRangeTile = new Tile(Tile.SIDE.MOUNTAIN_RANGE_IN, Tile.SIDE.MOUNTAIN_RANGE_OUT, Tile.SIDE.EMPTY,
                Tile.SIDE.EMPTY);
        originalTiles.put(mountainRangeTile, 4);

        straightRiverTile = new Tile(Tile.SIDE.RIVER, Tile.SIDE.EMPTY, Tile.SIDE.RIVER, Tile.SIDE.EMPTY);
        originalTiles.put(straightRiverTile, 3);

        curveRiverTile = new Tile(Tile.SIDE.RIVER, Tile.SIDE.RIVER, Tile.SIDE.EMPTY, Tile.SIDE.EMPTY);
        originalTiles.put(curveRiverTile, 3);

        mountainTile = new Tile(Tile.SIDE.MOUNTAIN, Tile.SIDE.EMPTY, Tile.SIDE.EMPTY, Tile.SIDE.EMPTY);
        originalTiles.put(mountainTile, 2);

        mountainRiverStraightTile = new Tile(Tile.SIDE.RIVER, Tile.SIDE.MOUNTAIN, Tile.SIDE.RIVER, Tile.SIDE.EMPTY);
        originalTiles.put(mountainRiverStraightTile, 1);

        mountainRiverCurveTile = new Tile(Tile.SIDE.RIVER, Tile.SIDE.RIVER, Tile.SIDE.EMPTY, Tile.SIDE.MOUNTAIN);
        originalTiles.put(mountainRiverCurveTile, 1);

        emptyTile = new Tile(Tile.SIDE.EMPTY, Tile.SIDE.EMPTY, Tile.SIDE.EMPTY, Tile.SIDE.EMPTY);
        originalTiles.put(emptyTile, 2);

        logFileIndex = 0;
        boardWriterPosition = 0;
        logBasePath = "C:\\Users\\Nedyar\\Desktop\\MESBG\\Tiler\\Generator\\logs\\validBoards";
        boardHashes = new HashSet<>();
    }

    public static synchronized void logBoardJSON(Board board) throws IOException {
        // Generar clave única normalizada
        String boardKey = generateCanonicalBoardKey(board);

        if (!boardHashes.add(boardKey)) {
            return; // duplicado incluso por rotación
        }

        rotateBoardLogIfNeededJSON();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"rows\":").append(board.rows).append(",");
        sb.append("\"cols\":").append(board.cols).append(",");
        sb.append("\"tiles\":[");

        for (int r = 0; r < board.rows; r++) {
            sb.append("[");
            for (int c = 0; c < board.cols; c++) {
                Tile tile = board.boardTiles[r][c];
                sb.append("{");
                if (tile != null) {
                    sb.append("\"north\":\"").append(tile.northSide).append("\",");
                    sb.append("\"east\":\"").append(tile.eastSide).append("\",");
                    sb.append("\"south\":\"").append(tile.southSide).append("\",");
                    sb.append("\"west\":\"").append(tile.westSide).append("\"");
                } else {
                    sb.append("\"north\":null,\"east\":null,\"south\":null,\"west\":null");
                }
                sb.append("}");
                if (c != board.cols - 1)
                    sb.append(",");
            }
            sb.append("]");
            if (r != board.rows - 1)
                sb.append(",");
        }

        sb.append("]}");

        if (boardWriterPosition > 0) {
            boardWriter.write(","); // separar del tablero anterior
            boardWriter.newLine();
        }
        boardWriterPosition++;

        boardWriter.write(sb.toString());
        boardWriter.newLine();
        boardWriter.flush();
    }

    /** ================== Manejo de archivos JSON grandes ================== **/

    private static void rotateBoardLogIfNeededJSON() throws IOException {
        if (boardWriter == null) {
            openNewBoardLogJSON();
            return;
        }

        java.io.File currentFile = new java.io.File(logBasePath + "_" + logFileIndex + ".json");
        if (currentFile.length() >= MAX_FILE_SIZE) {
            boardWriter.write("]");
            boardWriter.flush();
            boardWriter.close();
            logFileIndex++;
            boardWriterPosition = 0;
            openNewBoardLogJSON();
        }
    }

    private static void openNewBoardLogJSON() throws IOException {
        String path = logBasePath + "_" + logFileIndex + ".json";
        boardWriter = new BufferedWriter(new FileWriter(path, false));
        boardWriter.write("[");
        boardWriter.newLine();
        boardWriter.flush();
    }

    /** ================== Rotaciones y clave canónica ================== **/

    private static String generateBoardKey(Board board) {
        StringBuilder key = new StringBuilder();
        for (int r = 0; r < board.rows; r++) {
            for (int c = 0; c < board.cols; c++) {
                Tile t = board.boardTiles[r][c];
                if (t != null) {
                    key.append(t.northSide.ordinal()).append("-");
                    key.append(t.eastSide.ordinal()).append("-");
                    key.append(t.southSide.ordinal()).append("-");
                    key.append(t.westSide.ordinal()).append("-");
                } else
                    key.append("null");
                key.append(";");
            }
        }
        return key.toString();
    }

    private static String generateCanonicalBoardKey(Board board) {
        String minKey = generateBoardKey(board);

        // Cuadrado: todas las rotaciones
        if (board.rows == board.cols) {
            Board rotated = board.clone();
            for (int i = 1; i < 4; i++) {
                rotated = rotateBoard90(rotated);
                String key = generateBoardKey(rotated);
                if (key.compareTo(minKey) < 0)
                    minKey = key;
            }
        } else { // Rectangular: solo considerar 180°
            Board rotated180 = rotateBoard90(rotateBoard90(board));
            String key180 = generateBoardKey(rotated180);
            if (key180.compareTo(minKey) < 0)
                minKey = key180;
        }
        return minKey;
    }

    private static Board rotateBoard90(Board board) {
        int rows = board.rows;
        int cols = board.cols;
        Board rotated = new Board(cols, rows);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile t = board.boardTiles[r][c];
                if (t != null) {
                    Tile rt = t.clone();
                    rt.clockWiseRotate(1);
                    rotated.boardTiles[c][rows - 1 - r] = rt;
                    rotated.originalTiles[c][rows - 1 - r] = board.originalTiles[r][c];
                    rotated.tileRotations[c][rows - 1 - r] = (board.tileRotations[r][c] + 1) % 4;
                }
            }
        }

        return rotated;
    }
}
