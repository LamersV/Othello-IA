package players;

import game.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElPlayerMM extends ElPlayer{
    private AbstractPlayer rival;

    public ElPlayerMM(int depth, int heuristic) {
        super(depth, heuristic);
    }

    public ElPlayerMM(int depth) {
        super(depth);
    }

    @Override
    public BoardSquare play(int[][] tab){
        rival = Game.getRival();
        verifyEspecialistas(tab);
        if(especialista != null){
            return especialista;
        }else{
            return getMinMax(tab);
        }
    }

    private BoardSquare getMinMax(int[][] tab){
        var game = getGame();
        List<Move> moves = game.getValidMoves(tab, getMyBoardMark());
        int bestValue = Integer.MIN_VALUE;
        Move bestMove = moves.get(0);
        var depth = getDepth();
        for (var move : moves){
            var currentValue = getMinMaxValue(move, depth);
            if(currentValue > bestValue){
                bestValue = currentValue;
                bestMove = move;
            }
        }
        return bestMove.getBardPlace();
    }

    private int getMinMaxValue(Move move, int depth){
        var game = getGame();
        var value = 0;
        AbstractPlayer currentPlayer = this;
        if(depth % 2 != 0){
            currentPlayer = rival;
        }
        var newBoard = game.do_move(genTempTab(move.getBoard()), move.getBardPlace(), currentPlayer);
        if (depth == 0){
            return validateHeuristicV2(newBoard, getMyBoardMark());
        }
        var thisEndGame = game.testing_end_game(genTempTab(newBoard), getMyBoardMark());
        var rivalEndGame = game.testing_end_game(genTempTab(newBoard), getOpponentBoardMark());
        if(thisEndGame != 0){
            if(thisEndGame == 1){
                return 1000;
            }else {
                return 0;
            }
        }
        if (rivalEndGame != 0){
            return -1000;
        }
        List<Move> moves = game.getValidMoves(genTempTab(newBoard), currentPlayer.getMyBoardMark());
        var values = new ArrayList<Integer>();
        for (var currentMove : moves){
            values.add(getMinMaxValue(currentMove, depth - 1));
        }
        if(currentPlayer == this){
            value = Collections.max(values);
        }else{
            value = Collections.min(values);
        }
        return value;
    }
}
