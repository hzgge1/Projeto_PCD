package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import environment.Coordinate;
import environment.Direction;
import resolvers.Resolver;

/**
 * Class to demonstrate a player being added to the game.
 * @author luismota
 *
 */
public class PhoneyHumanPlayer extends Player {
	
	private transient Socket socket;
	private transient ObjectOutputStream out;
	private transient BufferedReader in;
	
	public PhoneyHumanPlayer(int id, Game game, CountDownLatch latch, Socket socket) {
		super(id, game, latch);
		this.socket = socket;
		createChannels();
	}
	
	private void createChannels() {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			return;
		}
	}
	
	@Override
	public byte getInitialStrenght() {
		return 5;
	}
	
	public boolean isHumanPlayer() {
		return true;
	}
	
	public ObjectOutputStream getOutputChannel() {
		return out;
	}
	
	@Override
	public Direction getDirection() {
		try {
			Direction d = Direction.getDirection(in.readLine());
			return d;
		} catch (IOException e) {
			kill();
			interrupt();
			return null;
		}
	}

	@Override
	public void realMove(Coordinate newCoordinate) throws InterruptedException {
		game.getCell(newCoordinate).move(this);;
	}
}
