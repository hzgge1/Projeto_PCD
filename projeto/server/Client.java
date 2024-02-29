package server;

import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import environment.Cell;
import environment.Direction;
import game.Game;
import gui.GameGuiMain;

public class Client extends Thread{
	
	private String address;
	private int port;
	
	private int up;
	private int down;
	private int right;
	private int left;
	
	private Game game;
	private GameGuiMain gui;
	
	private Socket socket;
	private ObjectInputStream in;
	private PrintWriter out;
	
	public Client(String address, int port, int up, int down, int right, int left) {
		this.address = address;
		this.port = port;
		this.up = up;
		this.down = down;
		this.right = right;
		this.left = left;
		
	}
	
	private void connectToServer() throws UnknownHostException, IOException {
		socket = new Socket(address, port);
		in = new ObjectInputStream(socket.getInputStream());
		out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	}
	
	
	public int getUpKey() {
		return up;
	}
	
	public int getDownKey() {
		return down;
	}
	
	public int getRightKey() {
		return right;
	}
	
	public int getLeftKey() {
		return left;
	}
	
	public Direction getDirection() throws InterruptedException {
		Direction d = gui.getBoardJComponent().getLastPressedDirection();
		gui.getBoardJComponent().clearLastPressedDirection();
		return d;
	}
	
	public void sendMessage(Direction d) {
		out.println(d.name());
	}
	

	@Override
	public void run() {
			try {
				connectToServer();
				game = new Game();
				gui = new GameGuiMain(game,this,"ClientGui");
				gui.init();
				ClientMessageSender cms = new ClientMessageSender(this);
				cms.start();
				boolean isGameOver = false;
				while(! isGameOver) {
					ServerMessage m = (ServerMessage) in.readObject();
					if(m.isPlayerDead())	cms.interrupt();
					Cell[][] board = m.getBoardGame();
					gui.getGame().setBoard(board);
					gui.getGame().notifyChange();
					isGameOver = m.isGameOver();
				}
				cms.interrupt();
				cms.join();
				socket.close();
				System.out.println("GAME HAS ENDED");
			}catch (ClassNotFoundException | IOException | InterruptedException e) {
				e.printStackTrace();
				return;
			} 
	}
	
	public static void main(String[] args) {
		new Client(null,Server.PORTO, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT).start();
//		new Client(null,Server.PORTO, KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_A).start();
	}
	
	
	
	

}
