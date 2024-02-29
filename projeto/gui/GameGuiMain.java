package gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import game.Game;
import server.Client;

public class GameGuiMain implements Observer {
	
	private JFrame frame = new JFrame("pcd.io");
	private BoardJComponent boardGui;
	private Game game;

	public GameGuiMain() {
		super();
		game = new Game();
		game.addObserver(this);

		buildGui();

	}
	
	public GameGuiMain(Game game, Client c,String frameName) {
		super();
		frame.setTitle(frameName);
		this.game = game;
		game.addObserver(this);
		
		buildGui(c);
	}
	
	public Game getGame() {
		return game;
	}
	
	public BoardJComponent getBoardJComponent() {
		return boardGui;
	}
	
	private void buildGui(Client c) {
		boardGui = new BoardJComponent(game,c);
		frame.add(boardGui);


		frame.setSize(800,800);
		frame.setLocation(0, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	private void buildGui() {
		boardGui = new BoardJComponent(game);
		frame.add(boardGui);


		frame.setSize(800,800);
		frame.setLocation(0, 150);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void init()  {
		frame.setVisible(true);
	}

	@Override
	public void update(Observable o, Object arg) {
		boardGui.repaint();
	}
	
	public void startGame() {
		game.startGame();
		
	}

	public static void main(String[] args) {
		GameGuiMain game = new GameGuiMain();
		game.init();
		game.startGame();
		
	}
	

}
