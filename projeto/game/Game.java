package game;


import java.util.Map;
import java.util.Observable;

import environment.Cell;
import environment.Coordinate;
import resolvers.Resolver;
import server.Server;

public class Game extends Observable {

	public static final int DIMY = 30;
	public static final int DIMX = 30;
	private static final int NUM_PLAYERS = 90;
	private static final int NUM_FINISHED_PLAYERS_TO_END_GAME=3;

	public static final long REFRESH_INTERVAL = 400;
	public static final double MAX_INITIAL_STRENGTH = 3;
	public static final long MAX_WAITING_TIME_FOR_MOVE = 2000;
	public static final long INITIAL_WAITING_TIME = 10000;

	protected Cell[][] board;
	
	private boolean isOver = false;
	private int id = 0;
	private CountDownLatch latch;
	private Server server;
	
	public Game() {
		latch = new CountDownLatch(NUM_FINISHED_PLAYERS_TO_END_GAME);
		board = new Cell[Game.DIMX][Game.DIMY];
		server = new Server(this, latch);
	
		for (int x = 0; x < Game.DIMX; x++) 
			for (int y = 0; y < Game.DIMY; y++) 
				board[x][y] = new Cell(new Coordinate(x, y),this);
	}
	
	//Retorna board do jogo utilizada para a construção das mensagens do server
	public Cell[][] getBoard(){
		return board;
	}
	
	public Cell getCell(Coordinate at) {
		return board[at.x][at.y];
	}
	
	public Cell getRandomCell() {
		Cell newCell=getCell(new Coordinate((int)(Math.random()*Game.DIMX),(int)(Math.random()*Game.DIMY)));
		return newCell; 
	}

	//Retorna a cell onde o player dado como argumento está
	public Cell getPlayerCell(Player player) {
		for (int x = 0; x < Game.DIMX; x++) 
			for (int y = 0; y < Game.DIMY; y++) 
				if(player.equals(getCell(new Coordinate(x,y)).getPlayer()))
					return getCell(new Coordinate(x,y));
		return null;
	}
	
	public synchronized int getPlayerId() {
		return id ++;
	}
	
	//Serve para alterar o board do game no caso do ClientGame
	public void	setBoard(Cell[][] board) {
		this.board = board;
	}
	
	//Indica se o jogo já acabou
	public boolean isGameOver() {
		return isOver;
	}
	
	//Verifica se a coordenada dada é válida(faz parte do board)
	public boolean validPosition(Coordinate c) {
		if(c.x >= 0 && c.x < DIMX && c.y >= 0 && c.y < DIMY )
			return true;
		return false;
	}
	
	/** 
	 * @param player 
	 * @throws InterruptedException 
	 */
	public void addPlayerToGame(Player player) throws InterruptedException  {
		Cell initialPos = getRandomCell();
		if(initialPos.setPlayer(player))
			notifyChange();
		else 
			addPlayerToGame(player);
	}

	/**	
	 * Updates GUI. Should be called anytime the game state changes
	 */
	public void notifyChange() {
		setChanged();
		notifyObservers();
	}
		
	//Iniia o jogo e fica à espera para terminar o mesmo
	public void startGame() {
		server.start();
		for(int i = 0; i < Game.NUM_PLAYERS; i ++)
			new AutoPlayer(getPlayerId(),this,latch).start();
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			endGame();
			return;
		}
		endGame();
	}
	
	//Termina o jogo
	private void endGame() {
		isOver = true;
		server.interrupt();
		Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
		for(Thread t : m.keySet()) {
			if(t instanceof AutoPlayer) { 
				((Player)t).end();
				t.interrupt();
			}
			if(t instanceof Resolver)
				t.interrupt();
		}
		System.out.println("GAME HAS ENDED");
	}	
}
