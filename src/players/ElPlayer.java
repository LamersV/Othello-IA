package players;

import game.*;

import java.util.*;

public class ElPlayer extends AbstractPlayer {
    Move bestMoveMain = null;
    int heuristic = 0;
    int turnCount = 0;
    BoardSquare especialista;

    int[][] weight = {
            {10, 4, 9, 7, 7, 9, 4, 10},
            {4,  3, 5, 5, 5, 5, 3, 4 },
            {9,  5, 8, 6, 6, 8, 5, 9 },
            {7,  5, 6, 6, 6, 6, 5, 7 },
            {7,  5, 6, 6, 6, 6, 5, 7 },
            {9,  5, 8, 6, 6, 8, 5, 9 },
            {4,  3, 5, 5, 5, 5, 3, 4 },
            {10, 4, 9, 7, 7, 9, 4, 10}
    };

    public ElPlayer(int depth, int heuristic) {
        super(depth);
        this.heuristic = heuristic;
    }

    public ElPlayer(int depth) {
        super(depth);
        this.heuristic = 5;
    }

    Random rand = new Random();

    @Override
    public BoardSquare play(int[][] tab) {
        verifyEspecialistas(tab);
        if(especialista != null){
            return especialista;
        }
        return switch (heuristic) {
            case 0 -> getMinMax(tab); //Não funfa
            case 1 -> heuristicV1(tab); //Pior escolha
            case 2 -> heuristicV2(tab); //Melhor escolha
            case 3 -> heuristicV3(tab); //Obter mais peças
            case 4 -> heuristicV4(tab); //Diferenca entre peças minhas e do adversario
            case 5 -> heuristicV5(tab); //Rival com menos jogadas validas
            case 6 -> SearchForMuchPieces(tab); //Busca por mais peças
            default -> getGame().getValidMoves(tab, getMyBoardMark()).get(0).getBardPlace(); //Padrão, sempre o 1 movimento possivel
        };
    }

    //------Heuristica 1------
    //Menor valor - Pior Escolha
    protected BoardSquare heuristicV1(int[][] tab){
        BoardSquare worstMove = null;
        var worstValue = Integer.MAX_VALUE;
        var game = getGame();
        List<Move> moves = game.getValidMoves(tab, getMyBoardMark());

        if (moves.isEmpty()){
            return new BoardSquare(-1, -1);
        }else{
            for (var move : moves){
                var currentMove = move.getBardPlace();
                var newBoard = game.do_move(genTempTab(tab), currentMove, this);

                var currentValue =  validateHeuristicV1(newBoard, getMyBoardMark());

                if(currentValue < worstValue){
                    worstValue = currentValue;
                    worstMove = currentMove;
                }
            }
            return worstMove;
        }
    }
    protected static int validateHeuristicV1(int[][] board, int mark){
        var playerMark = 0;
        var rivalMark = 0;

        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board.length; j++){
                if(board[i][j] != 0 && board[i][j] != mark){
                    rivalMark++;
                }
                if(board[i][j] == mark){
                    playerMark++;
                }
            }
        }
        return playerMark - rivalMark;
    }

    //------Heuristica 2------ @matheus
    //Maior valor - Melhor(Pior) Escolha
    protected BoardSquare heuristicV2(int[][] tab){
        BoardSquare bestMove = null;
        var bestValue = Integer.MIN_VALUE;
        var game = getGame();
        List<Move> moves = game.getValidMoves(tab, getMyBoardMark());

        if (moves.isEmpty()){
            return new BoardSquare(-1, -1);
        }else{
            for (var move : moves){
                var currentMove = move.getBardPlace();
                var newBoard = game.do_move(genTempTab(tab), currentMove, this);

                var currentValue =  validateHeuristicV2(newBoard, getMyBoardMark());

                if(currentValue > bestValue){
                    bestValue = currentValue;
                    bestMove = currentMove;
                }
            }
            return bestMove;
        }
    }
    protected static int validateHeuristicV2(int[][] board, int mark){
        var playerMark = 0;
        var rivalMark = 0;

        for (int i = 0; i < board.length; i++){
            for (int j = 0; j < board.length; j++){
                if(board[i][j] != 0 && board[i][j] != mark){
                    rivalMark++;
                }
                if(board[i][j] == mark){
                    playerMark++;
                }
            }
        }
        return playerMark - rivalMark;
    }

    //------Heuristica 3------
    //Vai tentar sempre obter o maior número de peças possiveis
    protected BoardSquare heuristicV3(int[][] tab){
        BoardSquare bestMove;
        var game = getGame();
        List<Move> moves = game.getValidMoves(tab, getMyBoardMark());

        if (moves.isEmpty()){
            return new BoardSquare(-1, -1);
        }else{
            bestMove = validateHeuristicV3(tab, moves);
            return bestMove;
        }
    }
    protected BoardSquare validateHeuristicV3(int[][] board, List<Move> moves){

        BoardSquare bestMove = null;
        var bestValue = Integer.MIN_VALUE;
        for (Move move : moves) {
            var count = 0;
            var newBoard = getGame().do_move(genTempTab(board), move.getBardPlace(), this);
            for (int[] ints : newBoard) {
                for (int j = 0; j < newBoard.length; j++) {
                    if (ints[j] == getMyBoardMark()) {
                        count++;
                    }
                }
            }
            //System.out.print(count + " - ");
            if(count > bestValue){
                bestValue = count;
                bestMove = move.getBardPlace();
            }else if(count == bestValue){
                if (rand.nextInt(2) == 0){
                    bestMove = move.getBardPlace();
                }
            }
        }
        //System.out.println("\nSelected: " + bestValue + "\n");
        return bestMove;
    }

    //------Heuristica 4------
    //Vai fazer um calculo entre numero de peças menos o numero de peças do rival para determinar qual movimento fazer
    protected BoardSquare heuristicV4(int[][] tab){
        BoardSquare bestMove = null;
        var game = getGame();
        List<Move> moves = game.getValidMoves(tab, getMyBoardMark());

        if (moves.isEmpty()){
            return new BoardSquare(-1, -1);
        }else{
            bestMove = validateHeuristicV4(tab, moves);
            return bestMove;
        }
    }
    protected BoardSquare validateHeuristicV4(int[][] board, List<Move> moves){

        BoardSquare bestMove = moves.get(0).getBardPlace();
        var bestValue = Integer.MIN_VALUE;
        if(moves.size() == 1){ return bestMove; }
        for (Move move : moves) {
            var myCount = 0;
            var opponentCount = 0;
            var newBoard = getGame().do_move(genTempTab(board), move.getBardPlace(), this);
            for (int[] ints : newBoard) {
                for (int j = 0; j < newBoard.length; j++) {
                    if (ints[j] == getMyBoardMark()) {
                        myCount++;
                    }else if(ints[j] == getOpponentBoardMark()){
                        opponentCount++;
                    }
                }
            }
            var count = myCount - opponentCount;
            if(count > bestValue){
                bestValue = count;
                bestMove = move.getBardPlace();
            }else if(count == bestValue){
                if (rand.nextInt(2) == 0){
                    bestMove = move.getBardPlace();
                }
            }

        }
        return bestMove;
    }

    //------Heuristica 5------
    //Faz a jogada em que o rival terá menos jogadas validas
    protected BoardSquare heuristicV5(int[][] tab){
        var game = getGame();
        var myPlays = game.getValidMoves(tab, getMyBoardMark());
        var bestValue= Integer.MAX_VALUE;
        var bestMove = myPlays.get(0);
        for (var move: myPlays) {
            var newBoard = game.do_move(genTempTab(tab), move.getBardPlace(), this);
            var opponentPlays = game.getValidMoves(newBoard, getOpponentBoardMark()).size();
            if(opponentPlays < bestValue){
                bestValue = opponentPlays;
                bestMove = move;
            }
        }
        return bestMove.getBardPlace();
    }

    //------Search for Much Pieces------
    private BoardSquare SearchForMuchPieces(int[][] tab){
        var game = getGame();
        var moves = game.getValidMoves(tab, getMyBoardMark());
        Move bestMove = moves.get(0);
        if(moves.size() == 1){ return bestMove.getBardPlace(); }

        List<Integer> ChildValues = new ArrayList<>(moves.size());
        var bestMoveValue = Integer.MIN_VALUE;

        for (Move value : moves) {
            var bestChildValue = Integer.MIN_VALUE;
            int myCount = 0, rivalCount = 0;
            var newBoard = game.do_move(genTempTab(tab), value.getBardPlace(), this);
            for (int[] ints : newBoard) {
                for (var j = 0; j < newBoard.length; j++) {
                    if (ints[j] == getMyBoardMark()) {
                        myCount++;
                    } else if (ints[j] == getOpponentBoardMark()) {
                        rivalCount++;
                    }
                }
            }
            for (var move : game.getValidMoves(newBoard, getMyBoardMark())) {
                int my2Count = 0, rival2Count = 0;
                newBoard = game.do_move(genTempTab(tab), value.getBardPlace(), this);
                for (int[] ints : newBoard) {
                    for (var j = 0; j < newBoard.length; j++) {
                        if (ints[j] == getMyBoardMark()) {
                            my2Count++;
                        } else if (ints[j] == getOpponentBoardMark()) {
                            rival2Count++;
                        }
                    }
                }
                var secCount = my2Count - rival2Count;
                if (secCount > bestChildValue) {
                    bestChildValue = secCount;
                }
            }
            var count = myCount - rivalCount;
            if ((count + bestChildValue) > bestMoveValue) {
                bestMoveValue = count;
                bestMove = value;
            }
        }
        return bestMove.getBardPlace();
    }

    //Sistemas Especialistas

    protected void verifyEspecialistas(int[][] tab){
        if(Game.getRival() instanceof OthelloPlayer){
            especialista = DestroyOthelloPlayer();
        }else if(verifyQuina(tab)){
            especialista = EspecialistQuina(tab);
        }
        else {
            especialista = null;
        }
    }

    //Especialista 1
    //Destroi o OthelloPlayer em 6 rodadas
    private BoardSquare DestroyOthelloPlayer(){
        List<BoardSquare> movements = Arrays.asList(
                new BoardSquare(4, 5),
                new BoardSquare(2, 5),
                new BoardSquare(2, 3),
                new BoardSquare(3, 7),
                new BoardSquare(0, 3),
                new BoardSquare(4, 1),
                new BoardSquare(6, 3)
        );
        var move = movements.get(turnCount);
        turnCount++;
        return move;
    }

    //Especialista 2
    //Verifica o peso das peças focando nas quinas
    private boolean verifyQuina(int[][] tab){
        List<Move> moves = getGame().getValidMoves(tab, getMyBoardMark());
        if(moves.isEmpty()){ return false; }
        for (var move : moves) {
            if (weight[move.getBardPlace().getCol()][move.getBardPlace().getRow()] >= 9){
                return true;
            }
        }
        return false;
    }
    private BoardSquare EspecialistQuina(int[][] tab){
        List<Move> moves = getGame().getValidMoves(tab, getMyBoardMark());
        var bestMove = moves.get(0);
        var bestValue = Integer.MIN_VALUE;
        for (var move : moves) {
            var currentValue = weight[move.getBardPlace().getCol()][move.getBardPlace().getRow()];

            if(currentValue > bestValue){
                bestValue = currentValue;
                bestMove = move;
            }
        }
        return bestMove.getBardPlace();
    }

    //Especialista 3
    //

    //------MinMax------
    //Não ta funcionando - tentativa antiga
    private BoardSquare getMinMax(int[][] tab){
        bestMoveMain = getGame().getValidMoves(tab, getMyBoardMark()).get(0);
        //System.out.println(bestMove.getBardPlace());
        minmax(tab, getDepth(), true);
        //System.out.println(bestMove.getBardPlace());
        return bestMoveMain.getBardPlace();
    }
    private int minmax(int[][] tab, int depth, boolean maximized){
        if (depth < 1){
            return 0;
        }

        var game = getGame();
        int bestValue = 0;

        if (maximized){
            bestValue = Integer.MIN_VALUE;
            var moves = game.getValidMoves(tab, getMyBoardMark());
            for (var move : moves){
                var newBoard = game.do_move(genTempTab(tab), move.getBardPlace(), this);
                var v = minmax(newBoard, depth - 1, false);
                if (v > bestValue){
                    bestMoveMain = move;
                    bestValue = v;
                }
            }
        }
        else{
            bestValue = Integer.MAX_VALUE;
            var moves = game.getValidMoves(tab, getMyBoardMark());
            for (var move : moves){
                var newBoard = game.do_move(genTempTab(tab), move.getBardPlace(), this);
                var v = minmax(newBoard, depth - 1, true);
                if (v < bestValue){
                    bestMoveMain = move;
                    bestValue = v;
                }
            }
        }
        System.out.println(bestMoveMain.getBardPlace());
        return bestValue;
    }

    protected static int[][] genTempTab(int[][] tab){
        var tempTab = new int[tab.length][tab.length];
        for (int i = 0; i < tempTab.length; i++){
            System.arraycopy(tab[i], 0, tempTab[i], 0, tab.length);
        }
        return tempTab;
    }
}
