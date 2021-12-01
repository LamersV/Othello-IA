package JoaoPlayer;

import game.*;

import java.util.List;

/***
 * Todas as heurísticas foram retiradas do vídeo: https://www.youtube.com/watch?v=qmJVM5-p0Lw&t=5s&ab_channel=Fabr%C3%ADcioSilva
 * Aqui ele ensina como jogar o Othello, para seres Humaninhos, mas foi bem util para o nosso trabalho
 */

public class SupremePlayer extends AbstractPlayer {
    double H1, H2, H3, H4, E1, E2, E3;

    public SupremePlayer(int depth) {
        super(depth);

        H1 = 4; //4
        H2 = 8; //8
        H3 = 18; //18
        H4 = 23; //23
        E1 = 3; //3
        E2 = 6; //6
        E3 = 8; //8

    }

    @Override
    public BoardSquare play(int[][] tab) {

        OthelloGame jogo = new OthelloGame();
        List<Move> jogadas = jogo.getValidMoves(tab, getMyBoardMark());

        if (jogadas.size() > 0) {
            int iMax = -1;
            double hMax = Double.NEGATIVE_INFINITY;
            for ( int i=0 ; i<jogadas.size() ; i++ ){
                double h = heuristicas(tab, jogadas.get(i));
                if ( h > hMax ){
                    hMax = h;
                    iMax = i;
                }
            }
            BoardSquare jogadaEscolhida = jogadas.get( iMax ).getBardPlace();
            return jogadaEscolhida;
        } else {
            return new BoardSquare(-1, -1);
        }
    }

    double heuristicas(int[][] tab, Move jogada){

        BoardSquare novaPosicao = jogada.getBardPlace();
        OthelloGame jogo = new OthelloGame();
        int[][] novoTab = jogo.do_move( copyTab(tab), novaPosicao, this);
        double resultado = 0;

        resultado += h1(novoTab, novaPosicao) * H1;
        resultado += h2(novoTab, novaPosicao) * H2;
        resultado += h3(novoTab, novaPosicao) * H3;
        resultado += h4(novoTab, novaPosicao) * H4;
        resultado += especialista1(novoTab, novaPosicao) * E1;
        resultado += especialista2(novoTab, novaPosicao) * E2;
        resultado += especialista3(novoTab, novaPosicao) * E3;

        return resultado;
    }

    double h1(int[][] tab, BoardSquare novaPosicao){
        return countMarkersInTab(tab, getMyBoardMark());
    }

    double h2(int[][]tab, BoardSquare novaPosicao){
        OthelloGame game = new OthelloGame();
        OthelloPlayer opponent = new OthelloPlayer(-1);
        opponent.setBoardMark( getOpponentBoardMark() );

        int max = Integer.MIN_VALUE;
        List<Move> validOpponentMoves = game.getValidMoves(tab, getOpponentBoardMark());
        for ( int n=0 ; n < validOpponentMoves.size() ; n++ ) {
            Move m = validOpponentMoves.get(n);

            int[][] tabAfterOpponentMove = game.do_move( copyTab(tab), m.getBardPlace(), opponent);
            int howManyOpponentCanEat = countMarkersInTab(tabAfterOpponentMove, getOpponentBoardMark());

            if ( howManyOpponentCanEat>max )
                max = howManyOpponentCanEat;
        }
        return -1 * max;
    }

    double h3(int[][]tab, BoardSquare novaPosicao){
        int i = novaPosicao.getCol();
        int j = novaPosicao.getRow();

        int valX = 0;

        switch (i) {
            case 0:
            case 7:
                valX = 2;
                break;
            case 1:
            case 6:
                valX = 3;
                break;
            case 2:
            case 5:
                valX = 4;
                break;
            case 3:
            case 4:
                valX = 5;
                break;
        }

        int valY = 0;

        switch (j){
            case 0:
            case 7:
                valY = 2;
                break;
            case 1:
            case 6:
                valY = 3;
                break;
            case 2:
            case 5:
                valY = 4;
                break;
            case 3:
            case 4:
                valY = 5;
                break;
        }
        return valX + valY;
    }

    double h4(int[][]tab, BoardSquare novaPosicao){
        int i = novaPosicao.getCol();
        int j = novaPosicao.getRow();

        return ((i==j || i + j ==7) && !(i == 0 || i==7 || j==0||j==7)) ? -10: 0;
    }

    double especialista1(int[][]tab, BoardSquare novaPosicao){
        int i = novaPosicao.getCol();
        int j = novaPosicao.getRow();

        return i==0 || i==7 || j==0 || j==7 ? 100 : 0;
    }

    double especialista2(int[][]tab, BoardSquare novaPosicao){
        int i = novaPosicao.getCol();
        int j = novaPosicao.getRow();

        return (i==0&&j==0) || (i==0&&j==7) || (i==7&&j==0) || (i==7&&j==7) ? 100 : 0;
    }

    double especialista3(int[][]tab, BoardSquare novaPosicao){
        int i = novaPosicao.getCol();
        int j = novaPosicao.getRow();

        OthelloGame game = new OthelloGame();
        OthelloPlayer opponent = new OthelloPlayer(-1);
        opponent.setBoardMark( getOpponentBoardMark() );

        if ( i==1 || i==6 ) {
            int opponentX = i==1 ? 0 : 7;

            // Linha acima
            if ( j-1 >= 0 )
                if ( game.validate_moviment(tab, new BoardSquare(j-1, opponentX), opponent )==0 )
                    return -100;

            // Mesma linha
            if ( game.validate_moviment(tab, new BoardSquare(j, opponentX), opponent )==0 )
                return -100;

            // Linha abaixo
            if ( j+1 <= 7 )
                if ( game.validate_moviment(tab, new BoardSquare(j+1, opponentX), opponent )==0 )
                    return -100;
        }
        else if ( j==1 || j==6 ) {
            int opponentY = j==1 ? 0 : 7;

            // Coluna à esquerda
            if ( i-1 >= 0 ){
                if ( game.validate_moviment(tab, new BoardSquare(opponentY, i-1), opponent )==0 )
                    return -100;
            }

            // Mesma coluna
            if ( game.validate_moviment(tab, new BoardSquare(opponentY, i), opponent )==0 )
                return -100;

            // Coluna à direita
            if ( i+1 <= 7 )
                if ( game.validate_moviment(tab, new BoardSquare(opponentY, i+1), opponent )==0 )
                    return -100;
        }

        return 0;
    }

    int [][] copyTab(int[][] tab) {
        int x = getGame().size;

        int[][] novoTab = new int[x][x];

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < x; j++) {
                novoTab[i][j] = tab[i][j] * 1;
            }
        }
        return novoTab;
    }

    int countMarkersInTab(int[][] tab, int mark){
        int quantos = 0;

        for (int i = 0; i < getGame().size; i++) {
            for (int j = 0; j < getGame().size; j++) {
                if ( tab[i][j] == mark)
                    quantos++;
            }
        }
        return quantos;
    }
}

/***    Tabuleiro
 *      [0,0][0,1][0,2][0,3][0,4][0,5][0,6][0,7]
 *      [1,0][1,1][1,2][1,3][1,4][1,5][1,6][1,7]
 *      [2,0][2,1][2,2][2,3][2,4][2,5][2,6][2,7]
 *      [3,0][3,1][3,2][3,3][3,4][3,5][3,6][3,7]
 *      [4,0][4,1][4,2][4,3][4,4][4,5][4,6][4,7]
 *      [5,0][5,1][5,2][5,3][5,4][5,5][5,6][5,7]
 *      [6,0][6,1][6,2][6,3][6,4][6,5][6,6][6,7]
 *      [7,0][7,1][7,2][7,3][7,4][7,5][7,6][7,7]
 */
