package server;

import java.io.Serializable;

import environment.Cell;

public class ServerMessage implements Serializable{
	
	private Cell[][] board;
	private boolean isPlayerDead;
	private boolean isGameOver;
	
	public ServerMessage(Cell[][] board, boolean isPlayerDead, boolean isGameOver) {
		this.board = board;
		this.isPlayerDead = isPlayerDead;
		this.isGameOver = isGameOver;
	}
		
	public Cell[][] getBoardGame(){
		return board;
	}
	
	public boolean isPlayerDead() {
		return isPlayerDead;
	}
	
	public boolean isGameOver() {
		return isGameOver;
	}

}
